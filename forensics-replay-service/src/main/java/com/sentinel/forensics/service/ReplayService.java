package com.sentinel.forensics.service;


import com.sentinel.forensics.replay.ReplayEngine;
import com.sentinel.forensics.replay.WhatIfSimulationEngine;
import com.sentinel.forensics.event.EventStoreClient;
import com.sentinel.shared.dto.EventDTO;
import com.sentinel.shared.dto.ReplayReport;
import com.sentinel.shared.dto.StepDecision;
import com.sentinel.forensics.engine.ReplayHashUtil;
import java.util.List;
import java.util.UUID;

public class ReplayService {
    private final ReplayEngine replayEngine;
    private final WhatIfSimulationEngine whatIfSimulationEngine;
    private final EventStoreClient eventStoreClient;

    public ReplayService(ReplayEngine replayEngine, WhatIfSimulationEngine whatIfSimulationEngine, EventStoreClient eventStoreClient) {
        this.replayEngine = replayEngine;
        this.whatIfSimulationEngine = whatIfSimulationEngine;
        this.eventStoreClient = eventStoreClient;
    }
    // PRD FR-RE-03: What-if simulation with alternate PolicySnapshot
    public ReplayReport simulateWhatIf(UUID sessionId, com.sentinel.shared.dto.PolicySnapshot alternateSnapshot) {
        List<EventDTO> events = eventStoreClient.fetchEvents(sessionId);
        com.sentinel.shared.dto.PolicySnapshot originalSnapshot = fetchPolicySnapshot(sessionId);
        ReplayReport report = whatIfSimulationEngine.simulate(
                sessionId, events, originalSnapshot, alternateSnapshot);
        report.setHash(ReplayHashUtil.computeSessionHash(events, alternateSnapshot));
        return report;
    }

    // PRD FR-RE-01, FR-RE-02: Fetch events + frozen snapshot, reconstruct decisions
    public ReplayReport replaySession(UUID sessionId) {
        List<EventDTO> events = eventStoreClient.fetchEvents(sessionId);
        com.sentinel.shared.dto.PolicySnapshot snapshot = fetchPolicySnapshot(sessionId);
        List<StepDecision> steps = replayEngine.reconstruct(events, snapshot);

        // Extract originalDecisions list from steps
        List<String> originalDecisions = steps.stream()
                .map(StepDecision::getOriginalDecision)
                .collect(java.util.stream.Collectors.toList());

        ReplayReport report = new ReplayReport();
        report.setSessionId(sessionId);
        report.setSteps(steps);
        report.setOriginalDecisions(originalDecisions);
        report.setSimulatedDecisions(originalDecisions); // same as original in straight replay
        report.setFirstDivergenceStep(-1);               // no divergence in straight replay
        report.setHash(ReplayHashUtil.computeSessionHash(events, snapshot));
        return report;
    }

    /**
     * Fetches the frozen PolicySnapshot for a session.
     *
     * Production path: query policy_rules_history WHERE snapshot_id = event.policyRuleSnapshotId
     * to get the exact rule set that was active when this session's events were recorded.
     *
     * Current implementation: derives snapshot from the first event that carries a
     * policyRuleSnapshotId. Falls back to empty snapshot if none found.
     *
     * TODO: Replace with a real PolicySnapshotClient call once policy-engine-service
     *       exposes GET /admin/policies/snapshots/{snapshotId}.
     */
    private com.sentinel.shared.dto.PolicySnapshot fetchPolicySnapshot(UUID sessionId) {
        List<EventDTO> events = eventStoreClient.fetchEvents(sessionId);
        // Use policyRuleId values from the events as stand-in for the frozen rule list
        List<String> rules = events.stream()
                .map(EventDTO::getPolicyRuleId)
                .filter(r -> r != null && !r.isEmpty())
                .distinct()
                .collect(java.util.stream.Collectors.toList());
        return new com.sentinel.shared.dto.PolicySnapshot(rules);
    }
}
