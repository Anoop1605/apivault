package com.sentinel.policy.api;

import com.sentinel.policy.engine.PolicyEngine;
import com.sentinel.policy.model.PolicyDecision;
import com.sentinel.policy.model.PolicyRule;
import com.sentinel.policy.service.PolicyService;
import com.sentinel.shared.context.RequestContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * PolicyAdminController — REST API for policy management and evaluation.
 * Endpoints for CRUD operations on policy rules and policy evaluation.
 * PRD Section 7.3: Policy Admin API
 */
@Slf4j
@RestController
@RequestMapping("/admin/policies")
@RequiredArgsConstructor
public class PolicyAdminController {

    private final PolicyService policyService;
    private final PolicyEngine policyEngine;

    /**
     * POST /admin/policies/evaluate
     * Evaluate request against policies
     */
    @PostMapping("/evaluate")
    public ResponseEntity<PolicyDecision> evaluatePolicy(@RequestBody RequestContext requestContext) {
        log.info("Evaluating policy for user: {}", requestContext.getUserId());
        PolicyDecision decision = policyEngine.evaluate(requestContext);
        return ResponseEntity.ok(decision);
    }

    /**
     * GET /admin/policies
     * Get all active policies
     */
    @GetMapping
    public ResponseEntity<List<PolicyRule>> getPolicies() {
        List<PolicyRule> policies = policyService.getAllActivePolicies();
        return ResponseEntity.ok(policies);
    }

    /**
     * GET /admin/policies/all
     * Get all policies (including inactive)
     */
    @GetMapping("/all")
    public ResponseEntity<List<PolicyRule>> getAllPolicies() {
        List<PolicyRule> policies = policyService.getAllPolicies();
        return ResponseEntity.ok(policies);
    }

    /**
     * GET /admin/policies/{ruleId}
     * Get specific policy by rule ID
     */
    @GetMapping("/{ruleId}")
    public ResponseEntity<PolicyRule> getPolicyByRuleId(@PathVariable String ruleId) {
        return policyService.getPolicyByRuleId(ruleId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * POST /admin/policies
     * Create new policy
     */
    @PostMapping
    public ResponseEntity<PolicyRule> createPolicy(@RequestBody PolicyRule rule) {
        log.info("Creating policy: {}", rule.getRuleId());
        if (policyService.getPolicyByRuleId(rule.getRuleId()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(null); // Policy already exists
        }

        PolicyRule created = policyService.createPolicy(rule);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * PUT /admin/policies/{ruleId}
     * Update existing policy
     */
    @PutMapping("/{ruleId}")
    public ResponseEntity<PolicyRule> updatePolicy(@PathVariable String ruleId,
                                                   @RequestBody PolicyRule rule) {
        log.info("Updating policy: {}", ruleId);
        try {
            PolicyRule updated = policyService.updatePolicy(ruleId, rule);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * DELETE /admin/policies/{ruleId}
     * Delete policy
     */
    @DeleteMapping("/{ruleId}")
    public ResponseEntity<Void> deletePolicy(@PathVariable String ruleId) {
        log.info("Deleting policy: {}", ruleId);
        try {
            policyService.deletePolicy(ruleId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * PATCH /admin/policies/{ruleId}/activate
     * Activate policy (create snapshot in policy_rules_history)
     */
    @PatchMapping("/{ruleId}/activate")
    public ResponseEntity<PolicyRule> activatePolicy(@PathVariable String ruleId) {
        log.info("Activating policy: {}", ruleId);
        try {
            PolicyRule activated = policyService.activatePolicy(ruleId);
            return ResponseEntity.ok(activated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * PATCH /admin/policies/{ruleId}/deactivate
     * Deactivate policy
     */
    @PatchMapping("/{ruleId}/deactivate")
    public ResponseEntity<PolicyRule> deactivatePolicy(@PathVariable String ruleId) {
        log.info("Deactivating policy: {}", ruleId);
        try {
            PolicyRule deactivated = policyService.deactivatePolicy(ruleId);
            return ResponseEntity.ok(deactivated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * POST /admin/policies/reload
     * Reload all policies from database into PolicyEngine
     */
    @PostMapping("/reload")
    public ResponseEntity<String> reloadPolicies() {
        log.info("Reloading policies from database");
        policyService.reloadPoliciesInEngine();
        return ResponseEntity.ok("Policies reloaded");
    }
}

