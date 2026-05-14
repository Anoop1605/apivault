package com.sentinel.policy.service;

import com.sentinel.policy.model.PolicyRule;
import com.sentinel.policy.model.PolicyRuleHistory;
import com.sentinel.policy.repository.PolicyRuleHistoryRepository;
import com.sentinel.shared.dto.PolicySnapshot;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PolicySnapshotService {

    private final PolicyRuleHistoryRepository historyRepository;

    @Transactional
    public void syncActiveSnapshots(List<PolicyRule> activeRules) {
        List<String> snapshotRules = activeRules.stream()
                .map(PolicyRule::getRuleId)
                .distinct()
                .sorted()
                .toList();

        OffsetDateTime activatedAt = OffsetDateTime.now();
        for (PolicyRule rule : activeRules) {
            historyRepository.save(PolicyRuleHistory.builder()
                    .snapshotId(UUID.randomUUID())
                    .ruleId(rule.getRuleId())
                    .ruleVersion(rule.getVersion())
                    .snapshotJson(snapshotRules)
                    .activatedBy("system")
                    .activatedAt(activatedAt)
                    .active(true)
                    .reason("auto-sync")
                    .build());
        }
    }

    public PolicySnapshot getSnapshot(UUID snapshotId) {
        PolicyRuleHistory history = historyRepository.findBySnapshotId(snapshotId)
                .orElseThrow(() -> new IllegalArgumentException("Snapshot not found: " + snapshotId));
        return new PolicySnapshot(history.getSnapshotJson());
    }

    public UUID getLatestSnapshotId(String ruleId) {
        return historyRepository.findTopByRuleIdOrderByActivatedAtDesc(ruleId)
                .map(PolicyRuleHistory::getSnapshotId)
                .orElse(null);
    }
}
