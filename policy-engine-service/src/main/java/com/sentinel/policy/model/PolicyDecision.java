package com.sentinel.policy.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * Result of policy evaluation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PolicyDecision {
    private boolean allowed; // true = ALLOW, false = DENY
    private String reason;
    private String matchedRuleId;
    private double riskScore;
    private LocalDateTime evaluatedAt;

    public static PolicyDecision allow(String ruleId, String reason, double riskScore) {
        return PolicyDecision.builder()
            .allowed(true)
            .matchedRuleId(ruleId)
            .reason(reason)
            .riskScore(riskScore)
            .evaluatedAt(LocalDateTime.now())
            .build();
    }

    public static PolicyDecision deny(String ruleId, String reason, double riskScore) {
        return PolicyDecision.builder()
            .allowed(false)
            .matchedRuleId(ruleId)
            .reason(reason)
            .riskScore(riskScore)
            .evaluatedAt(LocalDateTime.now())
            .build();
    }
}
