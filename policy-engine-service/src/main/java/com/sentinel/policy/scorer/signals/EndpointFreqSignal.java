package com.sentinel.policy.scorer.signals;

import com.sentinel.shared.context.RequestContext;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

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
 * WHY a nested map instead of a composite key like "userId:endpoint"?
 * - We need to iterate over ALL endpoints for a user to find the max.
 * With a composite key, we'd need to scan the entire outer map and
 * filter by prefix — O(n) where n is ALL user-endpoint pairs.
 * - With a nested map, we only iterate over THIS user's endpoints — O(k)
 * where k is the number of distinct endpoints this user has hit.
 * For a normal user, k is 5–20. Much cheaper.
 *
 * ───────────────────────────────────────────────────────────────────
 * Performance
 * ───────────────────────────────────────────────────────────────────
 * All state is in-process (ConcurrentHashMap + AtomicInteger).
 * No DB calls, no network calls.
 * Increment is O(1), max-scan is O(k) where k = distinct endpoints per user.
 * Well within the ≤ 5ms p99 budget.
 *
 * TRADEOFF: state resets on JVM restart — same as the other signals.
 * Acceptable because the scorer recalibrates within seconds of traffic
 * resuming.
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
    // State — the "elevator CCTV log"
    // ────────────────────────────────────────────────────────────────

    /**
     * Maps: userId → { endpoint → hit count }.
     *
     * WHY ConcurrentHashMap<String, ConcurrentHashMap<String, AtomicInteger>>?
     *
     * 1. Outer ConcurrentHashMap — thread-safe per-user lookups.
     * Different users' requests never block each other.
     *
     * 2. Inner ConcurrentHashMap — thread-safe per-endpoint counters
     * within a single user's map. Two requests from the same user
     * hitting different endpoints can update concurrently.
     *
     * 3. AtomicInteger — lock-free atomic increment via CAS.
     * Two requests from the same user hitting the SAME endpoint
     * will both safely increment without losing a count.
     *
     * All three layers are thread-safe independently — no external
     * synchronization needed anywhere.
     */
    private final ConcurrentHashMap<String, ConcurrentHashMap<String, AtomicInteger>> endpointCounts = new ConcurrentHashMap<>();

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

        // Step 1: RECORD — get or create this user's endpoint map, then increment
        // Both computeIfAbsent calls are atomic — safe under concurrent access
        ConcurrentHashMap<String, AtomicInteger> userEndpoints = endpointCounts.computeIfAbsent(userId,
                k -> new ConcurrentHashMap<>());

        userEndpoints.computeIfAbsent(endpoint, k -> new AtomicInteger(0))
                .incrementAndGet();

        // Step 2: FIND MAX — scan all endpoints for this user
        // values() on ConcurrentHashMap returns a weakly-consistent view —
        // fine for scoring, we don't need a perfect snapshot
        int maxCount = 0;
        for (AtomicInteger count : userEndpoints.values()) {
            int c = count.get();
            if (c > maxCount) {
                maxCount = c;
            }
        }

        // Step 3: SCORE — normalize to 0.0–1.0
        return Math.min(1.0, (double) maxCount / MAX_ENDPOINT_HITS);
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
}
