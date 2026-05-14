package com.sentinel.policy.service;

import com.sentinel.policy.engine.PolicyEngine;
import com.sentinel.policy.model.PolicyRule;
import com.sentinel.policy.repository.PolicyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * PolicyService — Business logic for policy rule management.
 * Handles CRUD operations, activation, and cache synchronization with PolicyEngine.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PolicyService {

    private final PolicyRepository policyRepository;
    private final PolicyEngine policyEngine;
    private final PolicySnapshotService policySnapshotService;

    /**
     * Get all active policies
     */
    public List<PolicyRule> getAllActivePolicies() {
        return attachSnapshotIds(policyRepository.findByActiveTrueOrderByPriorityDesc());
    }

    /**
     * Get all policies (including inactive)
     */
    public List<PolicyRule> getAllPolicies() {
        return attachSnapshotIds(policyRepository.findAllByOrderByPriorityDesc());
    }

    /**
     * Get policy by rule ID
     */
    public Optional<PolicyRule> getPolicyByRuleId(String ruleId) {
        return policyRepository.findByRuleId(ruleId).map(this::attachSnapshotId);
    }

    /**
     * Get policy by UUID ID
     */
    public Optional<PolicyRule> getPolicyById(UUID id) {
        return policyRepository.findById(id).map(this::attachSnapshotId);
    }

    /**
     * Create new policy rule
     */
    @Transactional
    public PolicyRule createPolicy(PolicyRule rule) {
        if (rule.getId() == null) {
            rule.setId(UUID.randomUUID());
        }
        if (rule.getVersion() == null) {
            rule.setVersion(1);
        }
        if (rule.getActive() == null) {
            rule.setActive(true);
        }
        if (rule.getCreatedAt() == null) {
            rule.setCreatedAt(LocalDateTime.now());
        }
        if (rule.getUpdatedAt() == null) {
            rule.setUpdatedAt(LocalDateTime.now());
        }

        log.info("Creating policy: ruleId={}, name={}", rule.getRuleId(), rule.getName());
        PolicyRule saved = policyRepository.save(rule);

        reloadPoliciesInEngine();
        return saved;
    }

    /**
     * Update existing policy rule
     */
    @Transactional
    public PolicyRule updatePolicy(String ruleId, PolicyRule rule) {
        PolicyRule existing = policyRepository.findByRuleId(ruleId)
                .orElseThrow(() -> new IllegalArgumentException("Policy not found: " + ruleId));

        // Update fields
        existing.setName(rule.getName());
        existing.setDescription(rule.getDescription());
        existing.setConditions(rule.getConditions());
        existing.setEffect(rule.getEffect());
        existing.setPriority(rule.getPriority());
        existing.setActive(rule.getActive());
        existing.setVersion(existing.getVersion() + 1);
        existing.setUpdatedAt(LocalDateTime.now());

        log.info("Updating policy: ruleId={}", ruleId);
        PolicyRule saved = policyRepository.save(existing);

        reloadPoliciesInEngine();
        return saved;
    }

    /**
     * Delete policy rule
     */
    @Transactional
    public void deletePolicy(String ruleId) {
        PolicyRule policy = policyRepository.findByRuleId(ruleId)
                .orElseThrow(() -> new IllegalArgumentException("Policy not found: " + ruleId));

        log.info("Deleting policy: ruleId={}", ruleId);
        policyRepository.delete(policy);

        reloadPoliciesInEngine();
    }

    /**
     * Activate a policy rule (creates snapshot in policy_rules_history)
     */
    @Transactional
    public PolicyRule activatePolicy(String ruleId) {
        PolicyRule policy = policyRepository.findByRuleId(ruleId)
                .orElseThrow(() -> new IllegalArgumentException("Policy not found: " + ruleId));

        if (!policy.getActive()) {
            policy.setActive(true);
            policy.setUpdatedAt(LocalDateTime.now());

            log.info("Activating policy: ruleId={}", ruleId);
            PolicyRule saved = policyRepository.save(policy);

            reloadPoliciesInEngine();
            return saved;
        }

        return policy;
    }

    /**
     * Deactivate a policy rule
     */
    @Transactional
    public PolicyRule deactivatePolicy(String ruleId) {
        PolicyRule policy = policyRepository.findByRuleId(ruleId)
                .orElseThrow(() -> new IllegalArgumentException("Policy not found: " + ruleId));

        if (policy.getActive()) {
            policy.setActive(false);
            policy.setUpdatedAt(LocalDateTime.now());

            log.info("Deactivating policy: ruleId={}", ruleId);
            PolicyRule saved = policyRepository.save(policy);

            reloadPoliciesInEngine();
            return saved;
        }

        return policy;
    }

    /**
     * Reload all active policies into the PolicyEngine
     */
    public void reloadPoliciesInEngine() {
        List<PolicyRule> activePolicies = attachSnapshotIds(policyRepository.findByActiveTrueOrderByPriorityDesc());
        if (!activePolicies.isEmpty()) {
            policySnapshotService.syncActiveSnapshots(activePolicies);
            activePolicies = attachSnapshotIds(policyRepository.findByActiveTrueOrderByPriorityDesc());
        }
        policyEngine.setPolicies(activePolicies);
        log.info("Reloaded {} active policies into PolicyEngine", activePolicies.size());
    }

    public com.sentinel.shared.dto.PolicySnapshot getSnapshot(UUID snapshotId) {
        return policySnapshotService.getSnapshot(snapshotId);
    }

    private List<PolicyRule> attachSnapshotIds(List<PolicyRule> rules) {
        return rules.stream().map(this::attachSnapshotId).toList();
    }

    private PolicyRule attachSnapshotId(PolicyRule rule) {
        rule.setSnapshotId(policySnapshotService.getLatestSnapshotId(rule.getRuleId()));
        return rule;
    }
}
