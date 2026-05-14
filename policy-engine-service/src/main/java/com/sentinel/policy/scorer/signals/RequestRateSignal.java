package com.sentinel.policy.scorer.signals;

import com.sentinel.shared.context.RequestContext;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * RequestRateSignal — Detects anomalous request rates per source IP.
 *
 * Uses a sliding-window counter to track requests per IP within a
 * configurable time window. Elevated request rates produce higher risk
 * scores, catching brute-force and enumeration attacks.
 *
 * This is a lightweight in-process implementation. Production systems
 * would use Redis or a distributed rate limiter.
 */
@Component
public class RequestRateSignal implements RiskSignal {

    /** Sliding window size in seconds. */
    private static final long WINDOW_SECONDS = 60;

    /** Threshold above which requests are considered anomalous. */
    private static final int NORMAL_THRESHOLD = 30;
    private static final int HIGH_THRESHOLD = 100;

    /** Per-IP request counters with window start time. */
    private final Map<String, WindowCounter> counters = new ConcurrentHashMap<>();

    @Override
    public double evaluate(RequestContext ctx) {
        String ip = ctx.getSourceIp();
        if (ip == null || ip.isEmpty()) {
            return 0.1;
        }

        long now = Instant.now().getEpochSecond();

        WindowCounter counter = counters.compute(ip, (key, existing) -> {
            if (existing == null || (now - existing.windowStart) > WINDOW_SECONDS) {
                return new WindowCounter(now);
            }
            existing.count.incrementAndGet();
            return existing;
        });

        int requestCount = counter.count.get();

        if (requestCount > HIGH_THRESHOLD) {
            return 0.95; // Very high risk — likely attack
        } else if (requestCount > NORMAL_THRESHOLD) {
            // Linear interpolation between normal and high thresholds
            double ratio = (double) (requestCount - NORMAL_THRESHOLD) / (HIGH_THRESHOLD - NORMAL_THRESHOLD);
            return 0.3 + (0.65 * ratio);
        }

        return 0.05; // Normal traffic
    }

    @Override
    public String name() {
        return "request_rate";
    }

    @Override
    public double weight() {
        return 2.0;
    }

    /**
     * Internal sliding window counter.
     */
    private static class WindowCounter {
        final long windowStart;
        final AtomicInteger count;

        WindowCounter(long windowStart) {
            this.windowStart = windowStart;
            this.count = new AtomicInteger(1);
        }
    }
}
