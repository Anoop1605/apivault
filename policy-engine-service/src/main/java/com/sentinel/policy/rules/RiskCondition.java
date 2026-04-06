package com.sentinel.policy.rules;

import com.sentinel.policy.model.Condition;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import java.util.Map;

/**
 * Condition that checks if behavioral risk score is within acceptable range
 * Risk score: 0.0 (safe) to 1.0 (dangerous)
 */
@Data
@Builder
@AllArgsConstructor
public class RiskCondition implements Condition {
    private double maxRiskThreshold; // 0.0 to 1.0

    @Override
    public boolean evaluate(Map<String, Object> attributes) {
        Object riskScore = attributes.get("riskScore");
        if (riskScore == null) {
            return false;
        }

        double score = ((Number) riskScore).doubleValue();
        return score <= maxRiskThreshold;
    }

    @Override
    public String getType() {
        return "RISK";
    }
}
