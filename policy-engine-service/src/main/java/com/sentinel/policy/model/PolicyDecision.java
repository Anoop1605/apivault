package com.sentinel.policy.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * PolicyDecision — The decision outcome from evaluating a request against
 * policies.
 * Records which rule matched, the decision (ALLOW/DENY), and supporting
 * context.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PolicyDecision {

    private String ruleId;
    private Integer ruleVersion;
    private String decision;
    private List<String> matchedConditions;
    private long evaluationTimeNs;
    private double riskScore;
    private UUID snapshotId;
    private List<String> snapshotRules;

    public boolean isAllowed() {
        return "ALLOW".equalsIgnoreCase(decision);
    }

    public boolean isDenied() {
        return "DENY".equalsIgnoreCase(decision);
    }
}
