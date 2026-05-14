package com.sentinel.policy.scorer.signals;

import java.util.Map;

/**
 * RiskScoreResult — Immutable value object containing the final weighted risk score
 * and the per-signal breakdown that produced it.
 *
 * Used by the BehavioralRiskScorer to return structured results to the
 * PolicyEvaluationController, which then injects the score into the
 * RequestContext for ABAC evaluation and emits it in the SecurityEvent.
 */
public record RiskScoreResult(
        /** Final weighted-average risk score in the range [0.0, 1.0]. */
        double finalScore,

        /** Per-signal contribution: signal name → individual raw score. */
        Map<String, Double> signalBreakdown
) {
}
