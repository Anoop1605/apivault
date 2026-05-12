package com.sentinel.policy.engine;

import com.sentinel.policy.cache.PolicyCache;
import com.sentinel.policy.model.PolicyRule;
import com.sentinel.shared.context.RequestContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.sentinel.policy.model.PolicyDecision;
import com.sentinel.shared.dto.PolicySnapshot;
import java.util.Objects;
import java.util.List;

/**
 * PolicyEngine — Core ABAC policy evaluation engine.
 * Uses pre-compiled PolicyRuleEvaluator objects from the cache for
 * sub-microsecond evaluation.
 * PRD Section 7.1: Evaluates rules in priority order; first match wins.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PolicyEngine {

    private final PolicyCache policyCache;

    /**
     * Evaluate a request and return a full PolicyDecision object.
     * This is the REAL evaluation hot-path.
     */
    public PolicyDecision evaluateToDecision(RequestContext context) {
        long startNs = System.nanoTime();

        List<PolicyRuleEvaluator> evaluators = policyCache.getActiveEvaluators();

        for (PolicyRuleEvaluator evaluator : evaluators) {
            if (evaluator.matches(context)) {
                return PolicyDecision.builder()
                        .ruleId(evaluator.ruleId())
                        .ruleVersion(evaluator.ruleVersion())
                        .snapshotId(evaluator.snapshotId())
                        .decision(evaluator.effect().toString())
                        .evaluationTimeNs(System.nanoTime() - startNs)
                        .build();
            }
        }

        // Default: DENY if no rule matches
        return PolicyDecision.builder()
                .ruleId("POLICY_NO_MATCH")
                .decision("DENY")
                .evaluationTimeNs(System.nanoTime() - startNs)
                .build();
    }

    /**
     * Evaluate a request against all active policies.
     * Returns the first matching rule's decision (ALLOW/DENY).
     *
     * @param context The incoming request context
     * @return The policy decision: ALLOW if no rule denies, otherwise DENY
     */
    public String evaluate(RequestContext context) {
        PolicyDecision decision = evaluateToDecision(context);
        return decision.getDecision();
    }

    /**
     * Evaluates a request against a specific historical or alternate snapshot
     * (What-If Simulation).
     * Reuses the compiled evaluators from the cache to maintain speed.
     */
    public PolicyDecision evaluateSnapshot(RequestContext context, PolicySnapshot snapshot) {
        long startNs = System.nanoTime();

        if (snapshot == null || snapshot.getRules() == null) {
            return PolicyDecision.builder().decision("POLICY_NO_MATCH").ruleId("DEFAULT")
                    .evaluationTimeNs(System.nanoTime() - startNs).build();
        }

        // Fetch specific compiled evaluators for this snapshot
        List<PolicyRuleEvaluator> snapshotEvaluators = snapshot.getRules().stream()
                .map(policyCache::getEvaluator)
                .filter(Objects::nonNull)
                .toList();

        for (PolicyRuleEvaluator evaluator : snapshotEvaluators) {
            if (evaluator.matches(context)) {
                return PolicyDecision.builder().decision(evaluator.getEffect().toString()).ruleId(evaluator.getRuleId())
                        .evaluationTimeNs(System.nanoTime() - startNs).build();
            }
        }

        return PolicyDecision.builder().decision("POLICY_NO_MATCH").ruleId("DEFAULT")
                .evaluationTimeNs(System.nanoTime() - startNs).build();
    }

    /**
     * Update the policies in the engine (called by PolicyService on refresh).
     *
     * @param rules The new list of active policy rules
     */
    public void setPolicies(List<PolicyRule> rules) {
        log.info("Refreshing PolicyEngine with {} rules", rules.size());
        policyCache.refresh(rules);
    }
}