package com.sentinel.policy.scorer.signals;

import com.sentinel.shared.context.RequestContext;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Signal #3 — JWT Anomaly Detection (Session Hijacking Detector)
 *
 * Detects when a single session/token is being used from multiple distinct
 * IP addresses — a strong indicator of token theft or session sharing.
 *
 * ───────────────────────────────────────────────────────────────────
 * ANALOGY — The "Hotel Key Card"
 * ───────────────────────────────────────────────────────────────────
 * Imagine you check into a hotel and get a key card (JWT token).
 * Normally, that key card is used at ONE door — your room.
 * If security sees that same key card being swiped at 5 different floors
 * within minutes, something is wrong — either you're cloning cards or
 * someone stole yours.
 *
 * That's exactly what this signal does:
 *   - 1 IP per session → totally normal (score 0.0)
 *   - 2 IPs → maybe WiFi + cellular on mobile (score 0.25)
 *   - 3 IPs → getting suspicious (score 0.5)
 *   - 5+ IPs → very likely compromised (score 1.0)
 *
 * ───────────────────────────────────────────────────────────────────
 * WHY is this forensically valuable?
 * ───────────────────────────────────────────────────────────────────
 * Session hijacking (stolen JWT) is one of the top OWASP attack vectors.
 * If an attacker steals a token via XSS, CSRF, or network sniffing,
 * they'll use it from THEIR IP — not the original user's IP.
 * This signal catches that geographic anomaly.
 *
 * ───────────────────────────────────────────────────────────────────
 * Performance
 * ───────────────────────────────────────────────────────────────────
 * All state is in-process (ConcurrentHashMap + ConcurrentHashMap.newKeySet).
 * No DB calls, no network calls. Reads and writes are O(1).
 * Well within the ≤ 5ms p99 budget.
 *
 * TRADEOFF: state resets on JVM restart — same as the other signals.
 * Acceptable because a legitimate user will quickly re-establish a
 * clean single-IP pattern on their new session.
 */
@Component
public class JwtAnomalySignal implements RiskSignal {

    // ────────────────────────────────────────────────────────────────
    //  Constants
    // ────────────────────────────────────────────────────────────────

    /**
     * If a single session is seen from this many IPs or more, score = 1.0.
     *
     * Why 5?
     *   - 1 IP is normal (home WiFi).
     *   - 2 IPs is common (mobile user switches WiFi ↔ cellular).
     *   - 3–4 IPs is unusual and warrants increasing suspicion.
     *   - 5+ IPs in a session is a very strong indicator of token theft
     *     or credential sharing — score should be maxed out.
     */
    private static final int MAX_DISTINCT_IPS = 5;

    /** Signal weight — equal share among 4 signals (0.25 each). */
    private static final double SIGNAL_WEIGHT = 0.25;

    // ────────────────────────────────────────────────────────────────
    //  State — the "hotel security log"
    // ────────────────────────────────────────────────────────────────

    /**
     * Maps: sessionId → set of distinct source IPs seen for that session.
     *
     * WHY ConcurrentHashMap<String, Set<String>>?
     *
     *   1. The outer ConcurrentHashMap gives us thread-safe per-session lookups.
     *      Multiple threads handling requests for different sessions never
     *      block each other (lock striping / CAS under the hood).
     *
     *   2. For the inner Set, we use ConcurrentHashMap.newKeySet() which returns
     *      a Set backed by a ConcurrentHashMap — thread-safe adds and reads
     *      without external synchronization.
     *
     *   3. WHY not a regular HashSet? HashSet is NOT thread-safe. If two
     *      requests from the same session arrive on different threads and
     *      both try to add() to the same HashSet simultaneously, we get
     *      data corruption (lost entries, infinite loops in Java 7, weird
     *      exceptions in Java 8+).
     *
     * Together: fully thread-safe, O(1) per operation, no locks visible to us.
     */
    private final ConcurrentHashMap<String, Set<String>> sessionIps =
            new ConcurrentHashMap<>();

    // ────────────────────────────────────────────────────────────────
    //  RiskSignal contract
    // ────────────────────────────────────────────────────────────────

    /**
     * Evaluates how suspicious this session's IP pattern is.
     *
     * Algorithm (2 steps):
     *   1. RECORD — add the current source IP to this session's IP set
     *   2. SCORE  — count distinct IPs, normalize to 0.0–1.0
     *
     * Score formula: Math.min(1.0, (distinctIPs - 1) / (MAX_DISTINCT_IPS - 1))
     *
     *   - 1 distinct IP  → (1-1) / 4 = 0.0  (perfectly normal)
     *   - 2 distinct IPs → (2-1) / 4 = 0.25 (maybe mobile switching)
     *   - 3 distinct IPs → (3-1) / 4 = 0.5  (suspicious)
     *   - 5+ distinct IPs → capped at 1.0   (very likely compromised)
     *
     * WHY subtract 1?
     *   Because 1 IP is the normal baseline — every session starts from at
     *   least 1 IP. Risk only begins when a SECOND IP appears.
     *
     * @param ctx the incoming request context (we extract sessionId and sourceIp)
     * @return risk score between 0.0 (single IP) and 1.0 (many IPs)
     */
    @Override
    public double evaluate(RequestContext ctx) {
        String sessionId = ctx.getSessionId();
        String sourceIp  = ctx.getSourceIp();

        // Step 1: RECORD — get or create this session's IP set, add current IP
        // computeIfAbsent is atomic: even if 10 threads hit this for a new session,
        // only one Set is created. ConcurrentHashMap.newKeySet() returns a thread-safe Set.
        Set<String> ips = sessionIps.computeIfAbsent(
                sessionId, k -> ConcurrentHashMap.newKeySet()
        );

        ips.add(sourceIp);

        // Step 2: SCORE — how many distinct IPs have used this session?
        int distinctIPs = ips.size();

        // 1 IP is baseline (normal), so we subtract 1 before normalizing
        // Denominator is (MAX_DISTINCT_IPS - 1) = 4, giving us a 0-to-1 scale
        return Math.min(1.0, (double) (distinctIPs - 1) / (MAX_DISTINCT_IPS - 1));
    }

    /** Unique identifier — appears as the key in RiskScoreResult.signalBreakdown */
    @Override
    public String name() {
        return "jwt_anomaly";
    }

    /** Weight for weighted average in BehavioralRiskScorer */
    @Override
    public double weight() {
        return SIGNAL_WEIGHT;
    }
}
