package com.sentinel.forensics.replay;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * EvaluationResult — Simple result from evaluating a single event against
 * policies.
 * Used internally by replay engines for clean separation of concerns.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EvaluationResult {
    private String decision; // "ALLOW", "DENY", "BLOCK", "REVIEW", "FLAG"
    private String ruleMatched; // Rule ID that produced this decision
    private Double riskScore; // Risk score from the event
}
