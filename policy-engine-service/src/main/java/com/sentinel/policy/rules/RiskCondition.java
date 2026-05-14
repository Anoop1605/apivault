package com.sentinel.policy.rules;

import com.sentinel.shared.context.RequestContext;

/**
 * RiskCondition — Evaluates whether the request's computed risk score
 * satisfies a threshold-based constraint.
 *
 * This condition type is used in ABAC rules to enforce risk-based access control.
 * For example: "DENY if risk score GREATER_THAN 0.7" blocks high-risk requests.
 *
 * Supported operators:
 *  - GREATER_THAN: risk score must exceed the threshold
 *  - LESS_THAN: risk score must be below the threshold
 *  - EQUALS: risk score must match the threshold (with floating-point tolerance)
 */
public class RiskCondition {

    private final String operator;
    private final double threshold;

    public RiskCondition(String operator, double threshold) {
        this.operator = operator;
        this.threshold = threshold;
    }

    /**
     * Evaluates the risk condition against the given request context.
     *
     * @param ctx The request context containing the computed risk score
     * @return true if the condition is satisfied
     */
    public boolean evaluate(RequestContext ctx) {
        Double riskScore = ctx.getRiskScore();
        if (riskScore == null) {
            return false;
        }

        return switch (operator.toUpperCase()) {
            case "GREATER_THAN" -> riskScore > threshold;
            case "LESS_THAN" -> riskScore < threshold;
            case "EQUALS" -> Math.abs(riskScore - threshold) < 0.001;
            default -> false;
        };
    }

    public String getType() {
        return "RISK";
    }

    public String getOperator() {
        return operator;
    }

    public double getThreshold() {
        return threshold;
    }
}
