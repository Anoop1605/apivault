package com.sentinel.forensics.replay;

import com.sentinel.shared.dto.EventDTO;
import com.sentinel.shared.dto.StepDecision;
import com.sentinel.shared.dto.PolicySnapshot;
import com.sentinel.shared.dto.ReplayReport;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.UUID;

/**
 * WhatIfSimulationEngine
 * Performs what-if replay simulation using an alternate PolicySnapshot.
 * PRD: Enables policy change impact analysis without modifying real data.
 */
public class WhatIfSimulationEngine {
    /**
     * Simulate replay with alternate PolicySnapshot.
     * @param sessionId Session UUID
     * @param events Ordered event list
     * @param alternateSnapshot Alternate policy snapshot for simulation
     * @return ReplayReport with simulated decisions
     */
    public ReplayReport simulate(UUID sessionId, List<EventDTO> events, PolicySnapshot alternateSnapshot) {
        events.sort(Comparator.comparingLong(EventDTO::getTimestampNs));
        List<StepDecision> steps = new ArrayList<>();
        for (EventDTO event : events) {
            // TODO: Apply alternateSnapshot rules to event for what-if simulation
            StepDecision step = new StepDecision(event);
            steps.add(step);
        }
        ReplayReport report = new ReplayReport();
        report.setSessionId(sessionId);
        report.setSteps(steps);
        // TODO: Set policyRuleSnapshotId in report
        return report;
    }
}
