package com.sentinel.forensics.service;


import com.sentinel.forensics.replay.ReplayEngine;
import com.sentinel.forensics.replay.WhatIfSimulationEngine;
import com.sentinel.forensics.event.EventStoreClient;
import com.sentinel.shared.dto.EventDTO;
import com.sentinel.shared.dto.ReplayReport;
import com.sentinel.shared.dto.StepDecision;
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
    // What-If Simulation: replay with alternate PolicySnapshot
    public ReplayReport simulateWhatIf(UUID sessionId, com.sentinel.shared.dto.PolicySnapshot alternateSnapshot) {
        List<EventDTO> events = eventStoreClient.fetchEvents(sessionId);
        return whatIfSimulationEngine.simulate(sessionId, events, alternateSnapshot);
    }

    // PRD: Fetch events and frozen PolicySnapshot, call ReplayEngine, build ReplayReport
    public ReplayReport replaySession(UUID sessionId) {
        List<EventDTO> events = eventStoreClient.fetchEvents(sessionId);
        com.sentinel.shared.dto.PolicySnapshot snapshot = fetchPolicySnapshot(sessionId);
        List<StepDecision> steps = replayEngine.reconstruct(events, snapshot);
        ReplayReport report = new ReplayReport();
        report.setSessionId(sessionId);
        report.setSteps(steps);
        return report;
    }

    // TODO: Implement actual snapshot fetch logic
    private com.sentinel.shared.dto.PolicySnapshot fetchPolicySnapshot(UUID sessionId) {
        // Placeholder: return empty snapshot
        return new com.sentinel.shared.dto.PolicySnapshot(java.util.Collections.emptyList());
    }
}
