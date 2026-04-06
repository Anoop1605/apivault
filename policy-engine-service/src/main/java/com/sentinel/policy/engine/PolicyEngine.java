package com.sentinel.policy.engine;

import com.sentinel.policy.model.PolicyDecision;
import com.sentinel.policy.model.PolicyRule;
import com.sentinel.policy.scorer.BehavioralRiskScorer;
import com.sentinel.shared.context.RequestContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Core policy evaluation engine implementing ABAC (Attribute-Based Access Control).
 * Evaluates RequestContext against PolicyRules in priority order.
 * Returns ALLOW/DENY decision based on rule conditions and zero-trust default.
 *
 * PRD Section 7: Policy Evaluation & Decision
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PolicyEngine {

    private final BehavioralRiskScorer riskScorer;
    private List<PolicyRule> activePolicies = List.of();

    /**
     * Evaluate request against all active policies
     * Returns ALLOW if matching rule has ALLOW effect, else defaults to DENY (zero-trust)
     *
     * @param requestContext Request attributes (roles, ip, timestamp, etc.)
     * @return PolicyDecision (allow/deny)
     */
    public PolicyDecision evaluate(RequestContext requestContext) {
        if (requestContext == null) {
            log.warn("Null request context, denying access");
            return PolicyDecision.deny(null, "Null request context", 1.0);
        }

        // Calculate risk score
        double riskScore = riskScorer.computeRiskScore(requestContext);
        requestContext = RequestContext.builder()
                .userId(requestContext.getUserId())
                .roles(requestContext.getRoles())
                .sessionId(requestContext.getSessionId())
                .endpoint(requestContext.getEndpoint())
                .method(requestContext.getMethod())
                .sourceIp(requestContext.getSourceIp())
                .requestTimestamp(requestContext.getRequestTimestamp())
                .userAgent(requestContext.getUserAgent())
                .requestBodyHash(requestContext.getRequestBodyHash())
                .riskScore(riskScore)
                .build();

        // Sort policies by priority (higher priority first)
        List<PolicyRule> sortedPolicies = activePolicies.stream()
                .filter(PolicyRule::getActive)
                .sorted(Comparator.comparingInt(PolicyRule::getPriority).reversed())
                .toList();

        // Evaluate each policy in priority order
        for (PolicyRule rule : sortedPolicies) {
            if (allConditionsMatch(rule, requestContext)) {
                log.info("Policy matched: {} with effect: {}", rule.getRuleId(), rule.getEffect());
                String reason = "Matched rule: " + rule.getName();

                if (rule.getEffect() == PolicyRule.PolicyEffect.ALLOW) {
                    return PolicyDecision.allow(rule.getRuleId(), reason, riskScore);
                } else {
                    return PolicyDecision.deny(rule.getRuleId(), reason, riskScore);
                }
            }
        }

        // Default: DENY if no rule matches (zero-trust principle)
        log.warn("No policy matched, defaulting to DENY (zero-trust)");
        return PolicyDecision.deny(null, "No matching policy found", riskScore);
    }

    /**
     * Check if all conditions in a rule are satisfied (AND logic)
     */
    private boolean allConditionsMatch(PolicyRule rule, RequestContext requestContext) {
        if (rule.getConditions() == null || rule.getConditions().isEmpty()) {
            return true; // No conditions = always match
        }

        return rule.getConditions().stream()
                .allMatch(condition -> ConditionEvaluator.evaluate(condition, requestContext));
    }

    /**
     * Evaluate request against policies (legacy Map-based interface for backwards compatibility)
     */
    public PolicyDecision evaluate(Map<String, Object> attributes) {
        RequestContext.Builder builder = RequestContext.builder();

        // Extract known fields if available
        if (attributes.containsKey("userId")) {
            builder.userId((String) attributes.get("userId"));
        }
        if (attributes.containsKey("roles")) {
            builder.roles((List<String>) attributes.get("roles"));
        }
        if (attributes.containsKey("sourceIp")) {
            builder.sourceIp((String) attributes.get("sourceIp"));
        }
        if (attributes.containsKey("endpoint")) {
            builder.endpoint((String) attributes.get("endpoint"));
        }
        if (attributes.containsKey("httpMethod")) {
            builder.method((String) attributes.get("httpMethod"));
        }

        return evaluate(builder.build());
    }

    /**
     * Update active policies (called by PolicyAdminController)
     */
    public void setPolicies(List<PolicyRule> policies) {
        this.activePolicies = policies != null ? policies : List.of();
        log.info("Updated active policies: {} rules", activePolicies.size());
    }

    public List<PolicyRule> getActivePolicies() {
        return activePolicies;
    }
}

