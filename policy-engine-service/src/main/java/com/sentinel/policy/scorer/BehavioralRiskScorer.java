package com.sentinel.policy.scorer;

import com.sentinel.policy.scorer.signals.RiskSignal;
import com.sentinel.shared.context.RequestContext;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Aggregates all Risk Signals to produce a final behavioral risk score.
 */
@Component
public class BehavioralRiskScorer {

    private final List<RiskSignal> signals;

    // Spring automatically injects all beans implementing RiskSignal
    public BehavioralRiskScorer(List<RiskSignal> signals) {
        this.signals = signals;
    }

    /**
     * Runs purely in-process using the injected signals.
     * Target: <= 5ms p99 latency.
     */
    public RiskScoreResult evaluateRisk(RequestContext context) {
        double totalWeightedScore = 0.0;
        double totalWeight = 0.0;
        Map<String, Double> breakdown = new HashMap<>();

        for (RiskSignal signal : signals) {
            double rawScore = signal.evaluate(context);
            double weight = signal.weight();

            breakdown.put(signal.name(), rawScore);
            totalWeightedScore += (rawScore * weight);
            totalWeight += weight;
        }

        double finalScore = totalWeight > 0 ? (totalWeightedScore / totalWeight) : 0.0;

        return new RiskScoreResult(finalScore, breakdown);
    }

    // DTO to hold the final score and the breakdown of why it scored that way
    public record RiskScoreResult(double finalScore, Map<String, Double> signalBreakdown) {
    }
}