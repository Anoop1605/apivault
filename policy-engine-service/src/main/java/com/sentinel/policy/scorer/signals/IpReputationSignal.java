package com.sentinel.policy.scorer.signals;

import com.sentinel.shared.context.RequestContext;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Signal #1 — IP Reputation Score
 *
 * Tracks how many times each source IP has been involved in DENIED requests.
 * The more denials from a given IP, the higher the risk score.
 *
 * Think of it as a "bouncer's memory":
 *   - Every denied request increments the IP's denial count
 *   - When scoring a NEW request, we check: "Have we denied this IP before? How often?"
 *   - 0 denials → 0.0 risk, 10+ denials → 1.0 (max risk)
 *
 * WHY in-process state?
 *   A database lookup would add ~5–50ms of latency per request.
 *   Our budget is ≤ 5ms p99 for the ENTIRE scorer (all 4 signals combined).
 *   ConcurrentHashMap reads are effectively O(1) and sub-microsecond.
 *
 * TRADEOFF:
 *   State resets on JVM restart — acceptable because the scorer recalibrates
 *   within seconds as denied requests rebuild the map.
 *
 * Design pattern: Strategy — this is one of 4 interchangeable RiskSignal implementations.
 */
@Component   // Spring manages the lifecycle — single instance shared across all request threads
public class IpReputationSignal implements RiskSignal {

    // ────────────────────────────────────────────────────────────────
    //  Constants
    // ────────────────────────────────────────────────────────────────

    /**
     * An IP with this many (or more) past denials gets the maximum risk score of 1.0.
     *
     * Why 10? It's a tunable knob. Too low (e.g. 2) → too many false positives.
     * Too high (e.g. 100) → attacker gets 99 free probes before being flagged.
     * 10 is a reasonable starting point for a 200 RPS system.
     */
    private static final int MAX_DENIAL_THRESHOLD = 10;

    /**
     * Signal weight — how much this signal contributes to the final BehavioralRiskScorer total.
     * 4 signals × 0.25 each = 1.0 total weight (all signals equally weighted by default).
     */
    private static final double SIGNAL_WEIGHT = 0.25;

    // ────────────────────────────────────────────────────────────────
    //  State — the "bouncer's notebook"
    // ────────────────────────────────────────────────────────────────

    /**
     * Maps: source IP → number of times that IP's requests have been denied.
     *
     * WHY ConcurrentHashMap<String, AtomicInteger> instead of HashMap<String, Integer>?
     *
     *   1. ConcurrentHashMap — multiple request-handler threads can read/write simultaneously
     *      without locking the entire map. It uses lock striping (per-bucket locking) under
     *      the hood, so threads operating on different IPs never block each other.
     *
     *   2. AtomicInteger — the denial count for a single IP might be incremented by
     *      multiple threads at the exact same time (two denied requests from the same IP
     *      arriving concurrently). AtomicInteger.incrementAndGet() uses CAS (Compare-And-Swap)
     *      at the CPU level — a single atomic hardware instruction, no locks needed.
     *
     *   Together: thread-safe, lock-free (at the counter level), O(1) reads and writes.
     */
    private final ConcurrentHashMap<String, AtomicInteger> denialCounts = new ConcurrentHashMap<>();

    // ────────────────────────────────────────────────────────────────
    //  RiskSignal contract
    // ────────────────────────────────────────────────────────────────

    /**
     * Evaluates how suspicious this request's source IP is.
     *
     * Formula:  score = min(1.0, denialCount / MAX_DENIAL_THRESHOLD)
     *
     * Examples:
     *   - IP never denied before → 0 / 10 = 0.0 (clean)
     *   - IP denied 3 times      → 3 / 10 = 0.3 (some suspicion)
     *   - IP denied 10+ times    → capped at 1.0 (maximum risk)
     *
     * WHY Math.min()?
     *   Without the cap, an IP denied 50 times would score 5.0 — but our contract
     *   says scores must be 0.0–1.0. Math.min clamps it to the valid range.
     *
     * @param ctx the incoming request context (we extract sourceIp from it)
     * @return risk score between 0.0 (safe) and 1.0 (maximum risk)
     */
    @Override
    public double evaluate(RequestContext ctx) {
        String ip = ctx.getSourceIp();

        // If the IP has no denial history, getOrDefault returns 0 → score is 0.0
        int count = denialCounts.getOrDefault(ip, new AtomicInteger(0)).get();

        return Math.min(1.0, (double) count / MAX_DENIAL_THRESHOLD);
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
    //  Mutation method — called by the Policy Engine AFTER a denial
    // ────────────────────────────────────────────────────────────────

    /**
     * Records that a request from this IP was denied.
     * Called by PolicyEngine (or a post-evaluation hook) whenever a request
     * gets a DENY decision.
     *
     * WHY is this a separate method and not inside evaluate()?
     *   evaluate() runs on EVERY request (even ones that will be allowed).
     *   recordDenial() runs ONLY after a denial is confirmed.
     *   Separation of concerns: evaluate = read (pure query), recordDenial = write (side effect).
     *
     * Thread safety walkthrough:
     *   1. computeIfAbsent() — atomically checks "does this IP exist?" and
     *      creates a new AtomicInteger(0) if not. Even if 10 threads call this
     *      for the same new IP simultaneously, only ONE AtomicInteger is created.
     *   2. incrementAndGet() — atomically adds 1 using CPU-level CAS.
     *      No locks, no race conditions, sub-microsecond.
     *
     * @param ip the source IP address whose denial count should increase
     */
    public void recordDenial(String ip) {
        denialCounts.computeIfAbsent(ip, k -> new AtomicInteger(0))
                    .incrementAndGet();
    }
}
