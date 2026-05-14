package com.sentinel.policy.api;

import com.sentinel.policy.model.PolicyRule;
import com.sentinel.policy.service.PolicyService;
import com.sentinel.shared.dto.PolicySnapshot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * PolicyAdminController — REST API for policy management.
 * Endpoints for CRUD operations on policy rules.
 * PRD Section 6.3: Admin endpoints with POLICY_ADMIN role requirement.
 */
@Slf4j
@RestController
@RequestMapping("/admin/policies")
@RequiredArgsConstructor
public class PolicyAdminController {

    private final PolicyService policyService;

    /**
     * GET /admin/policies — List all active policies
     */
    @GetMapping
    public ResponseEntity<List<PolicyRule>> getAllActivePolicies() {
        log.debug("Fetching all active policies");
        List<PolicyRule> policies = policyService.getAllActivePolicies();
        return ResponseEntity.ok(policies);
    }

    /**
     * GET /admin/policies/all — List all policies (including inactive)
     */
    @GetMapping("/all")
    public ResponseEntity<List<PolicyRule>> getAllPolicies() {
        log.debug("Fetching all policies");
        List<PolicyRule> policies = policyService.getAllPolicies();
        return ResponseEntity.ok(policies);
    }

    /**
     * GET /admin/policies/{ruleId} — Get a specific policy by rule ID
     */
    @GetMapping("/{ruleId}")
    public ResponseEntity<PolicyRule> getPolicyByRuleId(@PathVariable String ruleId) {
        log.debug("Fetching policy: ruleId={}", ruleId);
        Optional<PolicyRule> policy = policyService.getPolicyByRuleId(ruleId);
        return policy.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/snapshots/{snapshotId}")
    public ResponseEntity<PolicySnapshot> getSnapshot(@PathVariable UUID snapshotId) {
        try {
            return ResponseEntity.ok(policyService.getSnapshot(snapshotId));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * POST /admin/policies — Create a new policy
     */
    @PostMapping
    public ResponseEntity<PolicyRule> createPolicy(@RequestBody PolicyRule rule) {
        log.info("Creating new policy: name={}", rule.getName());
        PolicyRule created = policyService.createPolicy(rule);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * PUT /admin/policies/{ruleId} — Update an existing policy
     */
    @PutMapping("/{ruleId}")
    public ResponseEntity<PolicyRule> updatePolicy(
            @PathVariable String ruleId,
            @RequestBody PolicyRule rule) {
        log.info("Updating policy: ruleId={}", ruleId);
        try {
            PolicyRule updated = policyService.updatePolicy(ruleId, rule);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            log.warn("Policy not found: ruleId={}", ruleId);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * DELETE /admin/policies/{ruleId} — Delete a policy
     */
    @DeleteMapping("/{ruleId}")
    public ResponseEntity<Void> deletePolicy(@PathVariable String ruleId) {
        log.info("Deleting policy: ruleId={}", ruleId);
        try {
            policyService.deletePolicy(ruleId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            log.warn("Policy not found: ruleId={}", ruleId);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * PATCH /admin/policies/{ruleId}/activate — Activate a policy
     */
    @PatchMapping("/{ruleId}/activate")
    public ResponseEntity<PolicyRule> activatePolicy(@PathVariable String ruleId) {
        log.info("Activating policy: ruleId={}", ruleId);
        try {
            PolicyRule activated = policyService.activatePolicy(ruleId);
            return ResponseEntity.ok(activated);
        } catch (IllegalArgumentException e) {
            log.warn("Policy not found: ruleId={}", ruleId);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * PATCH /admin/policies/{ruleId}/deactivate — Deactivate a policy
     */
    @PatchMapping("/{ruleId}/deactivate")
    public ResponseEntity<PolicyRule> deactivatePolicy(@PathVariable String ruleId) {
        log.info("Deactivating policy: ruleId={}", ruleId);
        try {
            PolicyRule deactivated = policyService.deactivatePolicy(ruleId);
            return ResponseEntity.ok(deactivated);
        } catch (IllegalArgumentException e) {
            log.warn("Policy not found: ruleId={}", ruleId);
            return ResponseEntity.notFound().build();
        }
    }
}
