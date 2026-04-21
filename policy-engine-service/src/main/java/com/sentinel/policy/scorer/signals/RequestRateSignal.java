package com.sentinel.policy.scorer.signals;

import com.sentinel.shared.context.RequestContext;
import org.springframework.stereotype.Component;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.concurrent.TimeUnit;

/**
 * Signal #2 — Request Rate (Token Bucket)
 *
 * Detects request flooding: if a user sends too many requests in a short
 * time window, that's suspicious (brute-force attacks, credential stuffing,
 * scraping).
 *
 * ───────────────────────────────────────────────────────────────────
 * ANALOGY — The "Arcade Token Dispenser"
 * ───────────────────────────────────────────────────────────────────
 * Imagine each user gets a bucket that holds exactly 50 tokens. Every time
 * they make a request, they take 1 token out. An attendant drops a new
 * token into the bucket at a constant rate (e.g. 50 tokens / 60 seconds).
 *
 * - User plays slowly → bucket mostly full, score ≈ 0.0
 * - User plays fast → bucket empties, score = 1.0 (max risk)
 *
 * ───────────────────────────────────────────────────────────────────
 * WHY Token Bucket instead of Sliding Window?
 * ───────────────────────────────────────────────────────────────────
 * A Sliding Window requires storing every single timestamp. An attacker sending
 * 10,000 requests forces us to store 10,000 Longs in memory (O(N) space).
 * Token Bucket stores exactly TWO numbers per user: current tokens, and last
 * refill timestamp. It is mathematically O(1) in both time and space.
 *
 * ───────────────────────────────────────────────────────────────────
 * Performance
 * ───────────────────────────────────────────────────────────────────
 * All state is in-process (Caffeine Cache + TokenBucket Object).
 * No DB calls, no network calls.
 * Well within the ≤ 5ms p99 budget.
 *
 * STATE EXHAUSTION PROTECTION: Unbounded maps are replaced with Caffeine.
 */
@Component
public class RequestRateSignal implements RiskSignal {

    // ────────────────────────────────────────────────────────────────
    // Constants
    // ────────────────────────────────────────────────────────────────

    /**
     * Capacity: maximum burst allowed before bucket hits 0 (max risk).
     */
    private static final double MAX_TOKENS = 50.0;

    /**
     * Refill rate: 50 tokens per 60,000 milliseconds (~0.833 per ms).
     */
    private static final double REFILL_RATE_PER_MS = MAX_TOKENS / 60_000.0;

    /** Signal weight — equal share among 4 signals (0.25 each). */
    private static final double SIGNAL_WEIGHT = 0.25;

    // ────────────────────────────────────────────────────────────────
    // State — bounded cache of TokenBuckets
    // ────────────────────────────────────────────────────────────────

    /**
     * Bounded Cache: userId → TokenBucket
     * Protects against Memory Exhaustion DDoS by capping the cache size.
     */
    private final Cache<String, TokenBucket> userBuckets = Caffeine.newBuilder()
            .maximumSize(100_000)
            .expireAfterAccess(5, TimeUnit.MINUTES)
            .build();

    // ────────────────────────────────────────────────────────────────
    // RiskSignal contract
    // ────────────────────────────────────────────────────────────────

    @Override
    public double evaluate(RequestContext ctx) {
        String userId = ctx.getUserId();

        TokenBucket bucket = userBuckets.get(userId, k -> new TokenBucket(MAX_TOKENS, REFILL_RATE_PER_MS));

        return bucket.consumeAndScore();
    }

    /** Unique identifier — appears as the key in RiskScoreResult.signalBreakdown */
    @Override
    public String name() {
        return "request_rate";
    }

    /** Weight for weighted average in BehavioralRiskScorer */
    @Override
    public double weight() {
        return SIGNAL_WEIGHT;
    }

    // ────────────────────────────────────────────────────────────────
    // Inner Class — TokenBucket
    // ────────────────────────────────────────────────────────────────

    private static class TokenBucket {
        private final double capacity;
        private final double refillRate;

        private double tokens;
        private long lastRefillTimestamp;

        public TokenBucket(double capacity, double refillRate) {
            this.capacity = capacity;
            this.refillRate = refillRate;
            this.tokens = capacity; // Start full
            this.lastRefillTimestamp = System.currentTimeMillis();
        }

        /**
         * Synchronized prevents race conditions if the same user fires
         * multiple concurrent requests. Locking is per-user bucket, so
         * it will not bottleneck the global thread pool.
         */
        public synchronized double consumeAndScore() {
            long now = System.currentTimeMillis();
            long elapsed = now - lastRefillTimestamp;

            // 1. Refill the bucket based on time passed
            tokens += elapsed * refillRate;
            if (tokens > capacity) {
                tokens = capacity;
            }
            lastRefillTimestamp = now;

            // 2. Consume a token for the current request
            if (tokens >= 1.0) {
                tokens -= 1.0;
            } else {
                tokens = 0.0; // Empty bucket
            }

            // 3. Score calculation (0.0 means full bucket, 1.0 means empty bucket)
            return (capacity - tokens) / capacity;
        }
    }
}
