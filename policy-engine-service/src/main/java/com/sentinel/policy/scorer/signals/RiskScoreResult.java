package com.sentinel.policy.scorer.signals;

import java.util.Map;

/**
 * Immutable result produced by BehavioralRiskScorer for a single request.
 *
 * Consumed by:
 *   - RiskCondition  (in rules/) — to evaluate risk-based policy rules
 *   - PolicyEngine   (in engine/) — included in PolicyDecision metadata
 *   - Event emitter  — serialised into RISK_FLAGGED events for forensics
 *
 * This is a Java Record — a pure data carrier with no behavior.
 * The compiler auto-generates: constructor, getters, equals(), hashCode(), toString().
 *
 * @param score           Overall weighted risk score: 0.0 (safe) → 1.0 (maximum risk)
 * @param flagged         true if score >= RISK_THRESHOLD (0.7). Triggers DENY path.
 * @param signalBreakdown Individual score per signal — e.g. {"ip_reputation": 0.3, "request_rate": 0.8}
 *                        Forensics (P4) uses this to reconstruct WHY a request was flagged.
 */
public record RiskScoreResult(
        double score,
        boolean flagged,
        Map<String, Double> signalBreakdown
) {
    /**
     * Risk threshold — requests scoring at or above this are flagged.
     *
     * 0.7 means: if 70% of maximum possible risk weight is exceeded, deny.
     * Kept as a constant so it's easy to find and tune — never bury magic numbers.
     */
    public static final double RISK_THRESHOLD = 0.7;

    /**
     * Factory method: a safe result with a score of 0.0.
     * Used when scoring cannot be performed (e.g. null context in tests).
     */
    /**
     * Primary factory — BehavioralRiskScorer uses this.
     * Computes `flagged` internally so no caller ever repeats the threshold logic.
     */
    public static RiskScoreResult of(double score, Map<String, Double> signalBreakdown) {
        return new RiskScoreResult(score, score >= RISK_THRESHOLD, signalBreakdown);
    }

    /**
     * Factory method: a safe result with a score of 0.0.
     * Used when scoring cannot be performed (e.g. null context in tests).
     */
    public static RiskScoreResult safe() {
        return new RiskScoreResult(0.0, false, Map.of());
    }

    /**
     * Compact canonical constructor — validates invariants on construction.
     *
     * Java records support this: same name as the record, no parameter list.
     * The compiler fills in the assignments (this.score = score, etc.) after your code runs.
     */
    public RiskScoreResult {
        if (score < 0.0 || score > 1.0) {
            throw new IllegalArgumentException(
                "Risk score must be between 0.0 and 1.0, got: " + score
            );
        }
    }
}
