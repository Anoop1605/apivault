package com.sentinel.policy.engine;

import com.sentinel.policy.cache.PolicyCache;
import com.sentinel.policy.model.PolicyRule;
import com.sentinel.shared.context.RequestContext;
import com.sentinel.shared.dto.PolicySnapshot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.sentinel.policy.model.PolicyDecision;
import java.util.List;
import java.util.Objects;

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
     * Evaluate a request against all active policies.
     * Returns a PolicyDecision containing the decision and the matched rule ID.
     *
     * @param context The incoming request context
     * @return The policy decision object
     */
    public PolicyDecision evaluate(RequestContext context) {
        long startNs = System.nanoTime();

        // Get active, pre-compiled evaluators from cache (O(1) lookup)
        List<PolicyRuleEvaluator> evaluators = policyCache.getActiveEvaluators();

        for (PolicyRuleEvaluator evaluator : evaluators) {
            if (evaluator.matches(context)) {
                long elapsedNs = System.nanoTime() - startNs;
                log.debug("Policy matched: ruleId={}, effect={}, elapsedNs={}",
                        evaluator.getRuleId(), evaluator.getEffect(), elapsedNs);
                
                return PolicyDecision.builder()
                        .decision(evaluator.getEffect().toString())
                        .ruleId(evaluator.getRuleId())
                        .ruleVersion(evaluator.getRuleVersion())
                        .snapshotId(evaluator.getSnapshotId())
                        .evaluationTimeNs(elapsedNs)
                        .build();
            }
        }

        long elapsedNs = System.nanoTime() - startNs;
        log.debug("No policy matched; defaulting to DENY. elapsedNs={}", elapsedNs);
        return PolicyDecision.builder()
                .decision("DENY")
                .ruleId("DEFAULT_DENY")
                .ruleVersion(0)
                .evaluationTimeNs(elapsedNs)
                .build();
    }

    /**
     * Evaluates a request against a specific historical or alternate snapshot
     * (What-If Simulation).
     * Reuses the compiled evaluators from the cache to maintain speed.
     */
    public PolicyDecision evaluateSnapshot(RequestContext context, PolicySnapshot snapshot) {
        long startNs = System.nanoTime();

        if (snapshot == null || snapshot.getRules() == null) {
            return PolicyDecision.builder().decision("DENY").ruleId("DEFAULT_DENY").ruleVersion(0)
                    .evaluationTimeNs(System.nanoTime() - startNs).build();
        }

        // Fetch specific compiled evaluators for this snapshot
        List<PolicyRuleEvaluator> snapshotEvaluators = snapshot.getRules().stream()
                .map(policyCache::getEvaluator)
                .filter(Objects::nonNull)
                .toList();

        for (PolicyRuleEvaluator evaluator : snapshotEvaluators) {
            if (evaluator.matches(context)) {
                return PolicyDecision.builder()
                        .decision(evaluator.getEffect().toString())
                        .ruleId(evaluator.getRuleId())
                        .ruleVersion(evaluator.getRuleVersion())
                        .snapshotId(evaluator.getSnapshotId())
                        .snapshotRules(snapshot.getRules())
                        .evaluationTimeNs(System.nanoTime() - startNs).build();
            }
        }

        return PolicyDecision.builder().decision("DENY").ruleId("DEFAULT_DENY")
                .ruleVersion(0)
                .snapshotRules(snapshot.getRules())
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
