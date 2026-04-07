package com.sentinel.policy.scorer.signals;

import com.sentinel.shared.context.RequestContext;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Signal #2 — Request Rate (Sliding Window)
 *
 * Detects request flooding: if a user sends too many requests in a short
 * time window, that's suspicious (brute-force attacks, credential stuffing,
 * scraping).
 *
 * ───────────────────────────────────────────────────────────────────
 * ANALOGY — The "Ticket Counter" at a Theme Park
 * ───────────────────────────────────────────────────────────────────
 * Imagine each visitor (user) drops a timestamped ticket into their personal
 * bucket every time they enter a ride. The attendant (this signal) counts
 * how many tickets from the LAST 60 seconds are in the bucket.
 *
 *   - 5 tickets in 60s → normal visitor, score ≈ 0.1
 *   - 50+ tickets in 60s → either a bot or something sus, score = 1.0
 *
 * Before counting, the attendant sweeps away any ticket older than 60 seconds
 * — that's the "sliding" part of the sliding window.
 *
 * ───────────────────────────────────────────────────────────────────
 * WHY a sliding window and not a fixed window?
 * ───────────────────────────────────────────────────────────────────
 * Fixed window: count resets at the boundary (e.g. every minute at :00).
 * Problem: a user sends 49 requests at second :59 and 49 at second :01 —
 * that's 98 requests in 2 seconds, but each individual window sees only 49.
 *
 * Sliding window: always looks at the last 60s relative to NOW.
 * The 98-in-2-seconds scenario above would correctly show ~98 in the window.
 *
 * This is a VERY common system design interview topic.
 *
 * ───────────────────────────────────────────────────────────────────
 * Performance
 * ───────────────────────────────────────────────────────────────────
 * All state is in-process (ConcurrentHashMap + ConcurrentLinkedDeque).
 * No DB calls, no network calls. Cleanup is O(expired) amortised.
 * Well within the ≤ 5ms p99 budget.
 *
 * TRADEOFF: state resets on JVM restart — same as IpReputationSignal.
 * Acceptable because the window is only 60 seconds anyway.
 */
@Component
public class RequestRateSignal implements RiskSignal {

    // ────────────────────────────────────────────────────────────────
    //  Constants
    // ────────────────────────────────────────────────────────────────

    /**
     * How far back we look — 60 seconds.
     * Stored in milliseconds because System.currentTimeMillis() returns ms.
     */
    private static final long WINDOW_SIZE_MS = 60_000L;

    /**
     * If a user sends this many (or more) requests within the window → max risk.
     *
     * 50 requests/minute is a reasonable threshold:
     *   - Normal browsing: 5–15 requests/min
     *   - Automated tool / bot: 100+ requests/min
     *   - 50 gives some headroom before flagging
     */
    private static final int MAX_REQUEST_THRESHOLD = 50;

    /** Signal weight — equal share among 4 signals (0.25 each). */
    private static final double SIGNAL_WEIGHT = 0.25;

    // ────────────────────────────────────────────────────────────────
    //  State — the "ticket buckets"
    // ────────────────────────────────────────────────────────────────

    /**
     * Maps: userId → deque of request timestamps (epoch millis).
     *
     * WHY ConcurrentLinkedDeque instead of ArrayList?
     *
     *   1. We always add to the TAIL (newest timestamp) and remove from the HEAD
     *      (oldest expired timestamps). That's exactly what a Deque (double-ended queue)
     *      is designed for — O(1) add/remove at both ends.
     *
     *   2. ConcurrentLinkedDeque is thread-safe without external synchronization.
     *      Multiple threads can add timestamps for the same user concurrently.
     *
     *   3. ArrayList would require shifting elements on remove(0) — O(n) per cleanup.
     *      Deque.pollFirst() is O(1).
     */
    private final ConcurrentHashMap<String, ConcurrentLinkedDeque<Long>> requestTimestamps =
            new ConcurrentHashMap<>();

    // ────────────────────────────────────────────────────────────────
    //  RiskSignal contract
    // ────────────────────────────────────────────────────────────────

    /**
     * Evaluates how suspicious this user's request rate is.
     *
     * Algorithm (3 steps):
     *   1. RECORD — add the current timestamp to the user's deque
     *   2. CLEAN  — remove all timestamps older than 60 seconds
     *   3. SCORE  — count remaining timestamps, divide by threshold, cap at 1.0
     *
     * Example walkthrough:
     *   - User "alice" has made 30 requests in the last 60 seconds
     *   - Score = min(1.0, 30 / 50) = 0.6 — elevated but not flagged
     *   - If she hits 50 → score = 1.0 (max risk)
     *
     * @param ctx the incoming request context (we extract userId and timestamp from it)
     * @return risk score between 0.0 (normal rate) and 1.0 (flooding)
     */
    @Override
    public double evaluate(RequestContext ctx) {
        String userId = ctx.getUserId();
        long now = System.currentTimeMillis();

        // Step 1: RECORD — get or create this user's timestamp deque, add current time
        // computeIfAbsent is atomic: even if 10 threads hit this for a new user,
        // only one deque is created
        ConcurrentLinkedDeque<Long> timestamps =
                requestTimestamps.computeIfAbsent(userId, k -> new ConcurrentLinkedDeque<>());

        timestamps.addLast(now);

        // Step 2: CLEAN — evict timestamps outside the 60-second window
        // peekFirst() is O(1), pollFirst() is O(1) — we only remove expired entries
        // In the worst case this loops through all expired entries, but amortised
        // across requests, each timestamp is removed exactly once → O(1) amortised
        long cutoff = now - WINDOW_SIZE_MS;
        while (!timestamps.isEmpty() && timestamps.peekFirst() < cutoff) {
            timestamps.pollFirst();
        }

        // Step 3: SCORE — how many requests remain in the window?
        int count = timestamps.size();
        return Math.min(1.0, (double) count / MAX_REQUEST_THRESHOLD);
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
}
