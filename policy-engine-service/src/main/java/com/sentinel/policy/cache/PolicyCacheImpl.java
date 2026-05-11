package com.sentinel.policy.cache;

import com.sentinel.policy.engine.ConditionEvaluator;
import com.sentinel.policy.engine.PolicyRuleEvaluator;
import com.sentinel.policy.model.PolicyRule;
import com.sentinel.shared.context.RequestContext;
import com.sentinel.shared.enums.Decision;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

/**
 * PolicyCacheImpl — Implementation of the PolicyCache interface.
 * Compiles PolicyRule objects into efficient PolicyRuleEvaluator predicates
 * once,
 * then serves them from memory with zero allocation overhead.
 */
@Slf4j
@Component
public class PolicyCacheImpl implements PolicyCache {

    private List<PolicyRuleEvaluator> activeEvaluators = new ArrayList<>();

    /**
     * Returns the active, pre-compiled evaluators, sorted by priority.
     * This is the hot-path method called on every request.
     */
    @Override
    public synchronized List<PolicyRuleEvaluator> getActiveEvaluators() {
        return activeEvaluators;
    }

    /**
     * Retrieves a specific compiled evaluator by rule ID for What-If simulations.
     */
    @Override
    public PolicyRuleEvaluator getEvaluator(String ruleId) {
        // Search the active list for the requested rule
        return activeEvaluators.stream()
                .filter(evaluator -> evaluator.ruleId().equals(ruleId))
                .findFirst()
                .orElse(null);
    }

    /**
     * Refreshes the cache by compiling rules into evaluators.
     * This is an off-path operation called during policy activation/refresh.
     *
     * @param rules The complete list of active policy rules from the database
     */
    @Override
    public void refresh(List<PolicyRule> rules) {
        long startNs = System.nanoTime();

        List<PolicyRuleEvaluator> newEvaluators = new ArrayList<>();

        for (PolicyRule rule : rules) {
            // Compile all conditions into a single predicate
            Predicate<RequestContext> compiledPredicate = compileConditions(rule.getConditions());

            // Create the evaluator record
            PolicyRuleEvaluator evaluator = new PolicyRuleEvaluator(
                    rule.getRuleId(),
                    Decision.valueOf(rule.getEffect().toUpperCase()), // Convert String to Decision enum
                    rule.getPriority(),
                    compiledPredicate);

            newEvaluators.add(evaluator);
        }

        // Sort by priority (descending)
        newEvaluators.sort(Comparator.comparingInt(PolicyRuleEvaluator::priority).reversed());

        this.activeEvaluators = newEvaluators;

        long elapsedNs = System.nanoTime() - startNs;
        log.info("Policy cache refreshed: {} rules compiled in {}ns", rules.size(), elapsedNs);
    }

    /**
     * Compiles a list of conditions into a single predicate using ALL_OF logic.
     * If any condition fails, the overall predicate returns false.
     */
    private Predicate<RequestContext> compileConditions(List<PolicyRule.ConditionSpec> conditions) {
        if (conditions == null || conditions.isEmpty()) {
            return ctx -> true; // Empty conditions = always match
        }

        // Build a composite predicate from individual compiled conditions
        Predicate<RequestContext> composite = ctx -> true;

        for (PolicyRule.ConditionSpec conditionSpec : conditions) {
            Predicate<RequestContext> conditionPredicate = ConditionEvaluator.compile(conditionSpec);
            composite = composite.and(conditionPredicate);
        }

        return composite;
    }
}
