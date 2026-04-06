package com.sentinel.policy.model;

import java.util.Map;

/**
 * Interface for evaluating policy conditions
 * Implementations: RoleCondition, TimeCondition, IPCondition, RiskCondition
 */
public interface Condition {

    /**
     * Evaluate the condition against request attributes
     * @param attributes Request attributes (role, ip, timestamp, risk score, etc.)
     * @return true if condition is met, false otherwise
     */
    boolean evaluate(Map<String, Object> attributes);

    /**
     * Get condition type for logging/debugging
     */
    String getType();
}
