package com.sentinel.policy.repository;

import com.sentinel.policy.model.PolicyRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * PolicyRepository — Spring Data JPA repository for PolicyRule persistence.
 * Provides CRUD operations and custom queries for policy rules.
 */
@Repository
public interface PolicyRepository extends JpaRepository<PolicyRule, UUID> {

    /**
     * Find a policy rule by its rule ID
     */
    Optional<PolicyRule> findByRuleId(String ruleId);

    /**
     * Find all active policy rules, sorted by priority (descending)
     */
    List<PolicyRule> findByActiveTrueOrderByPriorityDesc();

    /**
     * Find all policy rules, sorted by priority (descending)
     */
    List<PolicyRule> findAllByOrderByPriorityDesc();

    /**
     * Find all policy rules by active status
     */
    List<PolicyRule> findByActive(Boolean active);

    /**
     * Check if a rule with given ID exists
     */
    boolean existsByRuleId(String ruleId);
}
