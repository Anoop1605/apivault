package com.sentinel.forensics.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sentinel.forensics.client.PolicyEngineClient;
import com.sentinel.forensics.engine.ReplayHashUtil;
import com.sentinel.forensics.event.EventStoreClient;
import com.sentinel.forensics.replay.EvaluationResult;
import com.sentinel.forensics.replay.ReplayEngine;
import com.sentinel.forensics.replay.WhatIfSimulationEngine;
import com.sentinel.shared.dto.EventDTO;
import com.sentinel.shared.dto.PolicySnapshot;
import com.sentinel.shared.dto.ReplayReport;
import com.sentinel.shared.enums.Decision;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReplayService {
    private final PolicyEngineClient policyEngineClient;
    private final ReplayEngine replayEngine;
    private final WhatIfSimulationEngine whatIfSimulationEngine;
    private final EventStoreClient eventStoreClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // PRD FR-RE-03: What-if simulation with alternate PolicySnapshot
    public ReplayReport simulateWhatIf(UUID sessionId, PolicySnapshot alternateSnapshot) {
        List<EventDTO> events = eventStoreClient.fetchEvents(sessionId);
        PolicySnapshot originalSnapshot = buildPolicySnapshot(events);
        ReplayReport report = whatIfSimulationEngine.simulate(
                sessionId, events, originalSnapshot, alternateSnapshot);
        report.setHash(ReplayHashUtil.computeSessionHash(events, alternateSnapshot));
        return report;
    }

    // PRD FR-RE-01, FR-RE-02: Fetch events + frozen snapshot, reconstruct decisions
    public ReplayReport replaySession(UUID sessionId) {
        List<EventDTO> events = eventStoreClient.fetchEvents(sessionId);
        PolicySnapshot snapshot = buildPolicySnapshot(events);
        List<EvaluationResult> evaluations = replayEngine.reconstruct(events, snapshot);

        // Build ReplayReport.StepDecision objects from evaluations and events
        List<ReplayReport.StepDecision> steps = new ArrayList<>();
        List<Decision> originalDecisions = new ArrayList<>();

        for (int i = 0; i < events.size(); i++) {
            EventDTO event = events.get(i);
            EvaluationResult eval = evaluations.get(i);

            Decision decision = Decision.valueOf(eval.getDecision());

            ReplayReport.StepDecision step = ReplayReport.StepDecision.builder()
                    .eventId(event.getEventId())
                    .timestampNs(event.getTimestampNs())
                    .eventType(event.getEventType() != null ? event.getEventType().toString() : "UNKNOWN")
                    .endpoint(event.getEndpoint())
                    .httpMethod(event.getHttpMethod())
                    .originalDecision(decision)
                    .simulatedDecision(decision) // Same as original in straight replay
                    .ruleId(eval.getRuleMatched())
                    .riskScore(event.getRiskScore())
                    .build();

            steps.add(step);
            originalDecisions.add(decision);
        }

        ReplayReport report = ReplayReport.builder()
                .sessionId(sessionId)
                .steps(steps)
                .originalDecisions(originalDecisions)
                .simulatedDecisions(originalDecisions) // Same as original in straight replay
                .firstDivergenceStep(-1) // No divergence in straight replay
                .snapshotIdUsed(extractSnapshotId(events))
                .hash(ReplayHashUtil.computeSessionHash(events, snapshot))
                .build();

        return report;
    }

    private UUID extractSnapshotId(List<EventDTO> events) {
        return events.stream()
                .map(EventDTO::getPolicyRuleSnapshotId)
                .filter(java.util.Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    /**
     * Fetches the frozen PolicySnapshot for a session.
     *
     * Production path: query policy_rules_history WHERE snapshot_id =
     * event.policyRuleSnapshotId
     * to get the exact rule set that was active when this session's events were
     * recorded.
     *
     * Current implementation: derives snapshot from the first event that carries a
     * policyRuleSnapshotId. Falls back to empty snapshot if none found.
     *
     * TODO: Replace with a real PolicySnapshotClient call once
     * policy-engine-service exposes GET /admin/policies/snapshots/{snapshotId}.
     */
    PolicySnapshot buildPolicySnapshot(List<EventDTO> events) {
        for (EventDTO event : events) {
            if (event.getPolicyRuleSnapshotId() != null) {
                try {
                    PolicySnapshot snapshot = policyEngineClient.fetchSnapshot(event.getPolicyRuleSnapshotId());
                    if (snapshot != null && snapshot.getRules() != null) {
                        return snapshot;
                    }
                } catch (Exception ignored) {
                    // Fall through to the persisted event snapshot if present.
                }
            }

            if (event.getPolicyRuleSnapshot() != null && !event.getPolicyRuleSnapshot().isBlank()) {
                try {
                    List<String> rules = objectMapper.readValue(
                            event.getPolicyRuleSnapshot(),
                            new TypeReference<List<String>>() {
                            });
                    return new PolicySnapshot(rules);
                } catch (Exception ignored) {
                    // Fall through to the last-resort rule-id list below.
                }
            }
        }

        List<String> rules = events.stream()
                .map(EventDTO::getPolicyRuleId)
                .filter(r -> r != null && !r.isEmpty())
                .distinct()
                .collect(Collectors.toList());
        return new PolicySnapshot(rules);
    }
}
