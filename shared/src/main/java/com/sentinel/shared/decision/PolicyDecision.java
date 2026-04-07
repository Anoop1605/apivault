package com.sentinel.shared.decision;

import com.sentinel.shared.enums.Decision;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * Result of ABAC policy evaluation.
 * Produced by P2 (Policy Engine), consumed by P1 (Gateway) and P3 (Event
 * Store).
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PolicyDecision {

    /** ALLOW or DENY outcome. */
    private Decision decision;

    /** ID of the ABAC rule that produced the decision. */
    private String ruleId;

    /** Version number of the rule at time of evaluation. */
    private Integer ruleVersion;

    /** FK to policy_rules_history — enables deterministic forensic replay. */
    private UUID snapshotId;

    /** List of condition types that were evaluated and matched. */
    private List<String> matchedConditions;
}
