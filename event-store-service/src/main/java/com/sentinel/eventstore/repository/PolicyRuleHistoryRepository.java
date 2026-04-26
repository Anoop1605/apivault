package com.sentinel.eventstore.repository;

import com.sentinel.eventstore.policy.PolicyRuleHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PolicyRuleHistoryRepository extends JpaRepository<PolicyRuleHistory, UUID> {
}