package com.sentinel.policy.scorer.signals;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.sentinel.shared.context.RequestContext;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Signal #3 — JWT/Session Anomaly (Concurrent IP Usage)
 *
 * Detects "The Teleporting User" — when a single identity (User ID or Token)
 * is used from multiple distinct IP addresses within a short timeframe.
 * This is a massive red flag for Session Hijacking or Leaked Tokens.
 *
 * ───────────────────────────────────────────────────────────────────
 * DOUBLE OOM PROTECTION
 * ───────────────────────────────────────────────────────────────────
 * We map UserId -> Cache<IP, Boolean>.
 * Outer cache bounds the number of concurrent users tracked.
 * Inner cache bounds the number of distinct IPs tracked per user (stops
 * X-Forwarded-For header spoofing attacks from blowing up the inner set).
 */
@Component
public class JwtAnomalySignal implements RiskSignal {

    // ────────────────────────────────────────────────────────────────
    // Constants
    // ────────────────────────────────────────────────────────────────

    /**
     * If a single user is seen from 3 DISTINCT IPs within 5 minutes, max risk.
     * 1 IP = Score 0.0 (Normal)
     * 2 IPs = Score 0.5 (Suspicious — could be VPN toggle or mobile/wifi swap)
     * 3 IPs = Score 1.0 (Highly likely compromised)
     */
    private static final int MAX_DISTINCT_IPS = 3;

    /** Signal weight — equal share among 4 signals (0.25 each). */
    private static final double SIGNAL_WEIGHT = 0.25;

    // ────────────────────────────────────────────────────────────────
    // State — The Double-Bounded Cache
    // ────────────────────────────────────────────────────────────────

    /**
     * Outer Cache: userId → UserIpTracker (contains the inner cache)
     */
    private final Cache<String, UserIpTracker> sessionTrackers = Caffeine.newBuilder()
            .maximumSize(50_000)
            .expireAfterAccess(Duration.ofMinutes(5))
            .build();

    // ────────────────────────────────────────────────────────────────
    // RiskSignal contract
    // ────────────────────────────────────────────────────────────────

    @Override
    public double evaluate(RequestContext ctx) {
        String userId = ctx.getUserId();
        String sourceIp = ctx.getSourceIp();

        // If the context is missing identifying info, we can't score it safely.
        if (userId == null || sourceIp == null || userId.isEmpty() || sourceIp.isEmpty()) {
            return 0.0;
        }

        // 1. Get or create the tracker for this user
        UserIpTracker tracker = sessionTrackers.get(userId, k -> new UserIpTracker());

        // 2. Record the IP and score
        return tracker.recordAndScore(sourceIp, MAX_DISTINCT_IPS);
    }

    @Override
    public String name() {
        return "jwt_anomaly";
    }

    @Override
    public double weight() {
        return SIGNAL_WEIGHT;
    }

    // ────────────────────────────────────────────────────────────────
    // Inner Class — Tracker
    // ────────────────────────────────────────────────────────────────

    private static class UserIpTracker {
        // Inner bounded cache: limits tracking to 10 IPs to prevent spoofing OOM
        // We just use a Cache<String, Boolean> as a makeshift bounded Set.
        private final Cache<String, Boolean> activeIps = Caffeine.newBuilder()
                .maximumSize(10)
                .expireAfterWrite(Duration.ofMinutes(5))
                .build();

        public double recordAndScore(String ip, int maxAllowed) {
            activeIps.put(ip, Boolean.TRUE);

            // Clean up expired entries to get an accurate size
            activeIps.cleanUp();
            long distinctIps = activeIps.estimatedSize();

            // Base calculation: subtract 1 because 1 IP is totally normal behavior
            long suspiciousIps = Math.max(0, distinctIps - 1);
            double maxSuspicious = Math.max(1, maxAllowed - 1);

            return Math.min(1.0, (double) suspiciousIps / maxSuspicious);
        }
    }
}