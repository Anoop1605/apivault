package com.sentinel.policy.scorer.signals;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.sentinel.shared.context.RequestContext;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Signal #1 — IP Reputation Score
 *
 * Tracks how many times each source IP has been involved in DENIED requests.
 * The more denials from a given IP, the higher the risk score.
 *
 * Think of it as a "bouncer's memory":
 * - Every denied request increments the IP's denial count
 * - When scoring a NEW request, we check: "Have we denied this IP before? How
 * often?"
 * - 0 denials → 0.0 risk, 10+ denials → 1.0 (max risk)
 *
 * WHY in-process state?
 * A database lookup would add ~5–50ms of latency per request.
 * Our budget is ≤ 5ms p99 for the ENTIRE scorer (all 4 signals combined).
 * In-memory Cache reads are effectively O(1) and sub-microsecond.
 * 
 * MEMORY EXHAUSTION PROTECTION:
 * We use Caffeine Cache with a maximumSize. If an attacker spoofs 10 million
 * IPs, the cache evicts the oldest ones instead of crashing the JVM with OOM.
 *
 * TIME DECAY ALGORITHM:
 * We use a "Sliding Window Counter" to track failures strictly within the
 * last 60 seconds, using O(1) memory (just two integers per IP).
 *
 * Design pattern: Strategy — this is one of 4 interchangeable RiskSignal
 * implementations.
 */
@Component // Spring manages the lifecycle — single instance shared across all request
           // threads
public class IpReputationSignal implements RiskSignal {

    // ────────────────────────────────────────────────────────────────
    // Constants
    // ────────────────────────────────────────────────────────────────

    /**
     * An IP with this many (or more) past denials gets the maximum risk score of
     * 1.0.
     *
     * Why 10? It's a tunable knob. Too low (e.g. 2) → too many false positives.
     * Too high (e.g. 100) → attacker gets 99 free probes before being flagged.
     * 10 is a reasonable starting point for a 200 RPS system.
     */
    private static final int MAX_DENIAL_THRESHOLD = 10;

    /**
     * Signal weight — how much this signal contributes to the final
     * BehavioralRiskScorer total.
     * 4 signals × 0.25 each = 1.0 total weight (all signals equally weighted by
     * default).
     */
    private static final double SIGNAL_WEIGHT = 0.25;

    // ────────────────────────────────────────────────────────────────
    // State — The bounded LRU Cache
    // ────────────────────────────────────────────────────────────────

    /**
     * A bounded cache mapping Source IP -> SlidingWindowCounter.
     * maximumSize(50_000): Prevents Memory Exhaustion DDoS.
     * expireAfterWrite(2m): Automatically cleans up inactive IPs.
     */
    private final Cache<String, SlidingWindowCounter> denialCounts = Caffeine.newBuilder()
            .maximumSize(50_000)
            .expireAfterWrite(Duration.ofMinutes(2))
            .build();

    // ────────────────────────────────────────────────────────────────
    // RiskSignal contract
    // ────────────────────────────────────────────────────────────────

    /**
     * Evaluates how suspicious this request's source IP is.
     *
     * Formula: score = min(1.0, denialCount / MAX_DENIAL_THRESHOLD)
     *
     * Examples:
     * - IP never denied before → 0 / 10 = 0.0 (clean)
     * - IP denied 3 times → 3 / 10 = 0.3 (some suspicion)
     * - IP denied 10+ times → capped at 1.0 (maximum risk)
     *
     * WHY Math.min()?
     * Without the cap, an IP denied 50 times would score 5.0 — but our contract
     * says scores must be 0.0–1.0. Math.min clamps it to the valid range.
     *
     * @param ctx the incoming request context (we extract sourceIp from it)
     * @return risk score between 0.0 (safe) and 1.0 (maximum risk)
     */
    @Override
    public double evaluate(RequestContext ctx) {
        String ip = ctx.getSourceIp();

        SlidingWindowCounter counter = denialCounts.getIfPresent(ip);

        // If the IP has no history, score is 0.0
        if (counter == null) {
            return 0.0;
        }

        double estimatedCount = counter.estimateCount();

        return Math.min(1.0, estimatedCount / MAX_DENIAL_THRESHOLD);
    }

    /** Unique identifier — appears as the key in RiskScoreResult.signalBreakdown */
    @Override
    public String name() {
        return "ip_reputation";
    }

    /** Weight for weighted average in BehavioralRiskScorer */
    @Override
    public double weight() {
        return SIGNAL_WEIGHT;
    }

    // ────────────────────────────────────────────────────────────────
    // Mutation method — called by the Policy Engine AFTER a denial
    // ────────────────────────────────────────────────────────────────

    /**
     * Records that a request from this IP was denied.
     * Called by PolicyEngine (or a post-evaluation hook) whenever a request
     * gets a DENY decision.
     *
     * WHY is this a separate method and not inside evaluate()?
     * evaluate() runs on EVERY request (even ones that will be allowed).
     * recordDenial() runs ONLY after a denial is confirmed.
     * Separation of concerns: evaluate = read (pure query), recordDenial = write
     * (side effect).
     *
     * @param ip the source IP address whose denial count should increase
     */
    public void recordDenial(String ip) {
        // get() creates a new counter if one doesn't exist atomically
        denialCounts.get(ip, k -> new SlidingWindowCounter()).increment();
    }

    // ────────────────────────────────────────────────────────────────
    // O(1) Time-Decay Algorithm
    // ────────────────────────────────────────────────────────────────

    /**
     * Sliding Window Counter Algorithm
     * 
     * Calculates requests in the last rolling 60 seconds without storing
     * individual timestamps. Uses two buckets: current minute and previous minute.
     * 
     * Memory footprint: O(1) - Just primitive variables.
     * Thread safety: synchronized methods per IP instance (extremely low
     * contention).
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

            // How many seconds into the current minute are we? (0 to 59)
            int secondsIntoMinute = (int) ((now / 1000) % 60);

            // The weight of the previous minute decays as we move forward in the current
            // minute
            double previousMinuteWeight = 1.0 - (secondsIntoMinute / 60.0);

            return currentCount + (previousCount * previousMinuteWeight);
        }

        private void tick() {
            long currentMinuteNow = System.currentTimeMillis() / 60000;

            if (currentMinuteNow > currentMinuteEpoch) {
                // If exactly 1 minute passed, shift current to previous.
                // If > 1 minute passed, there was no activity, so previous is 0.
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

// Let's say our security threshold is 100 failures per minute.

// 10:00:00 to 10:01:00: An IP fails 80 times. (previousMinuteCount = 80)

// 10:01:00 to 10:01:15: The IP fails 30 times. (currentMinuteCount = 30)

// It is now exactly 10:01:15. A new request comes in from that IP. Do we block
// it?

// Let's run the algorithm:

// We are 15 seconds into the current minute.

// How much of the previous minute is in our 60-second sliding window? 60
// seconds - 15 seconds = 45 seconds.

// What percentage of the previous minute is that? 45 / 60 = 0.75 (or 75%).

// How many requests from the previous minute do we "count"? 80 * 0.75 = 60
// requests.

// What is our total sliding window count? 60 (from previous) + 30 (from
// current) = 90 requests.

// Result: 90 is less than 100. We ALLOW the request.

// Now, imagine the IP fires 15 more requests in the next 5 seconds. It is now
// 10:01:20.

// Current second = 20.

// Weight of previous minute = (60 - 20) / 60 = 0.66 (or 66%).

// Previous minute contribution = 80 * 0.66 = ~53 requests.

// Current minute count is now 30 + 15 = 45 requests.̥

// Total sliding window count = 53 + 45 = 98 requests.

// We are dangerously close to the limit, but memory usage remained exactly two
// integers throughout this entire barrage. O(1) memory.
