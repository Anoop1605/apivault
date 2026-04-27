package com.sentinel.policy.scorer.signals;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.sentinel.shared.context.RequestContext;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Signal #4 — Endpoint Frequency (Endpoint Hammering Detector)
 *
 * Detects when a single user hits the SAME endpoint an abnormal number of
 * times.
 * A strong indicator of brute-force login attempts, credential stuffing,
 * or targeted scraping of a specific resource.
 *
 * ───────────────────────────────────────────────────────────────────
 * ANALOGY — The "Elevator CCTV"
 * ───────────────────────────────────────────────────────────────────
 * Imagine a building elevator with a security camera. If a person rides
 * to DIFFERENT floors throughout the day — that's normal behavior
 * (browsing different API endpoints). But if someone rides to the SAME
 * floor 50 times in one hour? That's suspicious — maybe they're trying
 * every apartment door on that floor (brute-forcing /api/auth/login).
 *
 * That's exactly what this signal does:
 * - User hits 5 different endpoints 3 times each → normal (score low)
 * - User hits /api/auth/login 30+ times → very suspicious (score 1.0)
 *
 * WE SCORE ON THE WORST ENDPOINT, not the current one.
 * Why? Because a user who hammered /login 25 times and is now hitting
 * /dashboard should still be flagged — the damage signal is /login.
 *
 * ───────────────────────────────────────────────────────────────────
 * Data Structure — Nested ConcurrentHashMaps
 * ───────────────────────────────────────────────────────────────────
 * Outer map: userId → inner map
 * Inner map: endpoint → hit count (AtomicInteger)
 *
 * DOUBLE OOM PROTECTION (The "Double-Bounded Cache"):
 * 1. Outer Cache is bounded (max 50,000 users) -> Stops millions of fake user
 * IDs.
 * 2. Inner Cache is bounded (max 100 endpoints per user) -> Stops a single user
 * from scanning thousands of random URLs and blowing up their inner map.
 *
 * TIME DECAY ALGORITHM:
 * Employs the Sliding Window Counter to age out hits automatically. A user who
 * hits an endpoint 30 times over an entire month will score 0.0. A user who
 * hits it 30 times in 60 seconds scores 1.0.
 */
@Component
public class EndpointFreqSignal implements RiskSignal {

    // ────────────────────────────────────────────────────────────────
    // Constants
    // ────────────────────────────────────────────────────────────────

    /**
     * If a user hits the same endpoint this many times (or more), score = 1.0.
     *
     * Why 30?
     * - Normal browsing: a user might hit the same endpoint 2–5 times
     * (page refresh, pagination, polling).
     * - Automated tools start at ~20+ rapid hits to a single endpoint.
     * - 30 gives some headroom for legitimate power users while catching
     * actual brute-force or scraping patterns.
     */
    private static final int MAX_ENDPOINT_HITS = 30;

    /** Signal weight — equal share among 4 signals (0.25 each). */
    private static final double SIGNAL_WEIGHT = 0.25;

    // ────────────────────────────────────────────────────────────────
    // State — The Double-Bounded Cache
    // ────────────────────────────────────────────────────────────────

    /**
     * Outer Cache: userId → UserEndpointTracker (which contains the inner cache)
     */
    private final Cache<String, UserEndpointTracker> userTrackers = Caffeine.newBuilder()
            .maximumSize(50_000)
            .expireAfterAccess(Duration.ofMinutes(5))
            .build();

    // ────────────────────────────────────────────────────────────────
    // RiskSignal contract
    // ────────────────────────────────────────────────────────────────

    /**
     * Evaluates how suspicious this user's endpoint access pattern is.
     *
     * Algorithm (3 steps):
     * 1. RECORD — increment counter for (userId, endpoint)
     * 2. FIND MAX — scan all endpoints for this user, find the highest count
     * 3. SCORE — normalize: min(1.0, maxCount / MAX_ENDPOINT_HITS)
     *
     * Example walkthrough:
     * - User "alice" has: {"/login": 20, "/dashboard": 3, "/api/users": 5}
     * - Max count = 20 (from /login)
     * - Score = min(1.0, 20 / 30) = 0.667 (elevated but not maxed)
     * - If she hits /login 10 more times → 30/30 = 1.0 (maximum risk)
     *
     * WHY score on the max and not the current endpoint?
     * If we only scored the CURRENT endpoint, a user who hammered /login
     * 25 times and then opened /dashboard would suddenly score 1/30 = 0.03.
     * That's a false-negative — the suspicious behavior already happened.
     * Scoring on the max ensures the signal stays "hot" until state resets.
     *
     * @param ctx the incoming request context (we extract userId and endpoint)
     * @return risk score between 0.0 (normal pattern) and 1.0 (endpoint hammering)
     */
    @Override
    public double evaluate(RequestContext ctx) {
        String userId = ctx.getUserId();
        String endpoint = ctx.getEndpoint();

        // 1. Get or create the tracker for this user
        UserEndpointTracker tracker = userTrackers.get(userId, k -> new UserEndpointTracker());

        // 2. Record the hit and immediately calculate the max score across all their
        // endpoints
        return tracker.recordAndScore(endpoint, MAX_ENDPOINT_HITS);
    }

    /** Unique identifier — appears as the key in RiskScoreResult.signalBreakdown */
    @Override
    public String name() {
        return "endpoint_freq";
    }

    /** Weight for weighted average in BehavioralRiskScorer */
    @Override
    public double weight() {
        return SIGNAL_WEIGHT;
    }

    // ────────────────────────────────────────────────────────────────
    // Inner Classes — Tracker and Counter
    // ────────────────────────────────────────────────────────────────

    /**
     * Manages the inner cache of endpoints for a single user.
     */
    private static class UserEndpointTracker {
        // Inner bounded cache: limits a single user to 100 distinct endpoints
        private final Cache<String, SlidingWindowCounter> endpointCounts = Caffeine.newBuilder()
                .maximumSize(100)
                .expireAfterWrite(Duration.ofMinutes(2))
                .build();

        public double recordAndScore(String endpoint, int maxAllowed) {
            // Record the hit
            endpointCounts.get(endpoint, k -> new SlidingWindowCounter()).increment();

            // Find the maximum hit count across all endpoints for this user
            double maxScore = 0.0;
            for (SlidingWindowCounter counter : endpointCounts.asMap().values()) {
                double est = counter.estimateCount();
                if (est > maxScore) {
                    maxScore = est;
                }
            }
            return Math.min(1.0, maxScore / maxAllowed);
        }
    }

    /**
     * O(1) Memory Time-Decay Algorithm (Sliding Window Counter)
     * Exact same logic as IpReputationSignal.
     */
    private static class SlidingWindowCounter {
        private volatile long currentMinuteEpoch;
        private int currentCount;
        private int previousCount;

        public SlidingWindowCounter() {
            this.currentMinuteEpoch = System.currentTimeMillis() / 60000;
        }

        public synchronized void increment() {
            tick();
            currentCount++;
        }

        public synchronized double estimateCount() {
            tick();
            long now = System.currentTimeMillis();

            int secondsIntoMinute = (int) ((now / 1000) % 60);
            double previousMinuteWeight = 1.0 - (secondsIntoMinute / 60.0);

            return currentCount + (previousCount * previousMinuteWeight);
        }

        private void tick() {
            long currentMinuteNow = System.currentTimeMillis() / 60000;

            if (currentMinuteNow > currentMinuteEpoch) {
                if (currentMinuteNow - currentMinuteEpoch == 1) {
                    previousCount = currentCount;
                } else {
                    previousCount = 0;
                }
                currentCount = 0;
                currentMinuteEpoch = currentMinuteNow;
            }
        }
    }
}
