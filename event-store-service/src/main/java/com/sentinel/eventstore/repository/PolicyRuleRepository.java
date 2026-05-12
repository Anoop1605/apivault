package com.sentinel.eventstore.repository;

import com.sentinel.eventstore.policy.PolicyRule;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PolicyRuleRepository extends JpaRepository<PolicyRule, String> {
}