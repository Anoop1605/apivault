package com.sentinel.policy.repository;

import com.sentinel.policy.model.PolicyRuleHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PolicyRuleHistoryRepository extends JpaRepository<PolicyRuleHistory, UUID> {

    Optional<PolicyRuleHistory> findTopByRuleIdOrderByActivatedAtDesc(String ruleId);

    Optional<PolicyRuleHistory> findBySnapshotId(UUID snapshotId);
}
