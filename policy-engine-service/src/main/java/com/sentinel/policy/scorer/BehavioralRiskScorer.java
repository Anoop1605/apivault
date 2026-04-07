package com.sentinel.policy.scorer;

import com.sentinel.policy.scorer.signals.RiskScoreResult;
import com.sentinel.policy.scorer.signals.RiskSignal;
import com.sentinel.shared.context.RequestContext;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Behavioral Risk Scorer — the orchestrator that combines all RiskSignal
 * implementations into a single weighted risk score per request.
 *
 * ───────────────────────────────────────────────────────────────────
 * ANALOGY — The "Judge's Panel"
 * ───────────────────────────────────────────────────────────────────
 * Think of a gymnastics competition. Each judge (signal) watches the
 * same performance (request) and gives an independent score from 0 to 10.
 * The final score is a weighted average of all judges.
 *
 * Here, our 4 "judges" are:
 *   1. IpReputationSignal  — "Has this IP been in trouble before?"
 *   2. RequestRateSignal   — "Is this user sending too many requests?"
 *   3. JwtAnomalySignal    — "Is this token being used from multiple IPs?"
 *   4. EndpointFreqSignal  — "Is this user hammering a single endpoint?"
 *
 * Each judge scores 0.0 (safe) to 1.0 (max risk), with a weight of 0.25.
 * The final score is the weighted average, and if it exceeds 0.7 → FLAGGED.
 *
 * ───────────────────────────────────────────────────────────────────
 * Design Pattern: Strategy + Dependency Injection
 * ───────────────────────────────────────────────────────────────────
 * All 4 signals implement the RiskSignal interface (Strategy pattern).
 * Spring auto-discovers them because they're annotated with @Component
 * and injects them as a List<RiskSignal> (Dependency Injection).
 *
 * WHY is this powerful?
 *   - To add a 5th signal, just create a new @Component class that
 *     implements RiskSignal. The scorer picks it up automatically.
 *     Zero changes needed in this class. Open/Closed Principle in action.
 *   - To remove a signal, just delete new @Component (or remove @Component).
 *   - To change weights, adjust weight() in the individual signal.
 *   - In tests, you can inject mock signals to test the scorer in isolation.
 *
 * INTERVIEW CONNECTION:
 *   "How does Strategy pattern differ from Template Method?"
 *   Strategy: composition — the scorer HAS signals, each independently replaceable.
 *   Template Method: inheritance — subclass overrides steps of a parent algorithm.
 *   Strategy is more flexible because signals are independent objects.
 *
 * ───────────────────────────────────────────────────────────────────
 * Performance
 * ───────────────────────────────────────────────────────────────────
 * The scorer itself does zero I/O. It just:
 *   1. Loops through 4 signals (each O(1) or O(k) in-process)
 *   2. Computes a weighted sum (O(n) where n = 4, so effectively O(1))
 *   3. Builds a small HashMap for breakdown (O(n) = O(4))
 *   4. Returns an immutable RiskScoreResult
 *
 * Total: well within the ≤ 5ms p99 budget.
 */
@Service  // @Service (not @Component) — semantic: this IS the business service for risk scoring
public class BehavioralRiskScorer {

    // ────────────────────────────────────────────────────────────────
    //  Dependencies — injected by Spring
    // ────────────────────────────────────────────────────────────────

    /**
     * All RiskSignal implementations, auto-discovered by Spring's component scan.
     *
     * WHY List<RiskSignal> and not individual fields?
     *   - If we had: IpReputationSignal ipSignal; RequestRateSignal rateSignal; ...
     *     then adding a 5th signal means editing this class (violates Open/Closed).
     *   - With List<RiskSignal>, Spring injects ALL @Component beans that implement
     *     RiskSignal. Adding a new signal = zero changes here.
     *
     * WHY final? The list reference is set once at construction and never changes.
     * The list contents are also fixed (Spring resolves all beans at startup).
     */
    private final List<RiskSignal> signals;

    /**
     * Constructor injection — the recommended way in Spring.
     *
     * WHY constructor injection over @Autowired on a field?
     *   1. Makes dependencies explicit — you can see what this class needs
     *   2. Enables immutability (final fields)
     *   3. Classes are testable without Spring — just pass mocks in the constructor
     *   4. Spring guarantees all dependencies exist before this object is created
     *
     * @param signals all RiskSignal beans discovered by component scanning
     */
    public BehavioralRiskScorer(List<RiskSignal> signals) {
        this.signals = signals;
    }

    // ────────────────────────────────────────────────────────────────
    //  Core scoring method
    // ────────────────────────────────────────────────────────────────

    /**
     * Legacy method for backwards compatibility with older PolicyEngine.
     * Uses double return to match the previous interface.
     * 
     * @param ctx the request context to score
     * @return the overall risk score (0.0–1.0)
     */
    public double computeRiskScore(RequestContext ctx) {
        return score(ctx).score();
    }

    /**
     * Evaluates a request against all risk signals and produces a combined score.
     *
     * Algorithm:
     *   1. For each signal: evaluate → get raw score, multiply by weight
     *   2. Sum all (score × weight), sum all weights
     *   3. Divide to get the weighted average
     *   4. Build the signal breakdown map for forensics
     *   5. Return RiskScoreResult (which auto-determines if flagged)
     *
     * Weighted average formula:
     *   finalScore = Σ(signalScore_i × weight_i) / Σ(weight_i)
     *
     * With 4 equal-weight signals (0.25 each):
     *   totalWeight = 0.25 × 4 = 1.0
     *   finalScore = (s1 × 0.25 + s2 × 0.25 + s3 × 0.25 + s4 × 0.25) / 1.0
     *              = average of all 4 scores
     *
     * Example:
     *   ip_reputation = 0.3, request_rate = 0.6, jwt_anomaly = 0.0, endpoint_freq = 0.8
     *   finalScore = (0.3 + 0.6 + 0.0 + 0.8) × 0.25 / 1.0 = 0.425
     *   flagged? 0.425 < 0.7 → false
     *
     * WHY weighted average and not a simple sum?
     *   - Sums can exceed 1.0 if multiple signals fire simultaneously.
     *   - Weighted average always stays in [0.0, 1.0] — consistent with our contract.
     *   - Weights let us make some signals more important than others later
     *     (e.g. jwt_anomaly → 0.4 if we decide session hijacking is more critical).
     *
     * @param ctx the request context to score
     * @return a RiskScoreResult with the overall score, flagged status, and breakdown
     */
    public RiskScoreResult score(RequestContext ctx) {

        double weightedSum = 0.0;
        double totalWeight = 0.0;
        Map<String, Double> breakdown = new HashMap<>();

        for (RiskSignal signal : signals) {

            // Each signal evaluates independently — no signal knows about the others
            double rawScore = signal.evaluate(ctx);
            double weight   = signal.weight();

            // Accumulate for weighted average
            weightedSum += rawScore * weight;
            totalWeight += weight;

            // Record for forensics — P4 needs to know WHY a request was flagged
            breakdown.put(signal.name(), rawScore);
        }

        // Guard against division by zero (shouldn't happen with 4 signals,
        // but defensive coding is a good habit — especially in interviews)
        double finalScore = (totalWeight > 0.0)
                ? weightedSum / totalWeight
                : 0.0;

        // Clamp to [0.0, 1.0] — belt-and-suspenders safety
        finalScore = Math.min(1.0, Math.max(0.0, finalScore));

        // RiskScoreResult.of() handles the flagged check (score >= 0.7)
        return RiskScoreResult.of(finalScore, breakdown);
    }
}
