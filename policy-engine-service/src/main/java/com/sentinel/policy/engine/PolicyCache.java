package com.sentinel.policy.cache;

import com.sentinel.policy.engine.PolicyRuleEvaluator;
import com.sentinel.policy.model.PolicyRule;

import java.util.List;

/**
 * A cache for active, compiled policy rules.
 * <p>
 * The cache's responsibility is to hold "ready-to-run" evaluators,
 * not just raw database entities, to keep the PolicyEngine's hot path fast.
 * An implementation of this interface will perform the one-time compilation
 * of a {@link PolicyRule} into a {@link PolicyRuleEvaluator}.
 */
public interface PolicyCache {

    /**
     * Retrieves the active, sorted list of rule evaluators.
     * This is called by the PolicyEngine on every request and must be highly
     * performant.
     *
     * @return A {@link List} of {@link PolicyRuleEvaluator}, sorted by priority.
     */
    List<PolicyRuleEvaluator> getActiveEvaluators();

    /**
     * Refreshes the cache with a new set of policy rules from the source.
     * This method is responsible for compiling the raw PolicyRule objects
     * into efficient PolicyRuleEvaluator instances and sorting them.
     *
     * @param rules The complete list of active policy rules from the database.
     */
    void refresh(List<PolicyRule> rules);
}