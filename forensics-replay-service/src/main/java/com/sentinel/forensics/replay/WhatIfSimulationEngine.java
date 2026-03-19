package com.sentinel.forensics.replay;

import com.sentinel.shared.dto.EventDTO;
import com.sentinel.shared.dto.PolicySnapshot;
import com.sentinel.shared.dto.ReplayReport;
import com.sentinel.shared.dto.StepDecision;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * What-if simulation engine — PRD FR-RE-03, FR-RE-04.
 *
 * Re-evaluates each event in a historical session against an ALTERNATE
 * policy rule set, producing a side-by-side comparison with the original
 * decisions and identifying the first divergence point.
 *
 * This is the core forensic feature: "What would have happened if we had
 * stricter rules?" — e.g. the attack would have been blocked at step 2
 * instead of step 5.
 */
public class WhatIfSimulationEngine {

    /**
     * Simulate replay with an alternate PolicySnapshot.
     *
     * @param sessionId         Session UUID
     * @param events            All events for the session (will be sorted internally)
     * @param originalSnapshot  The frozen snapshot used in the real gateway run
     * @param alternateSnapshot The modified rule set to test
     * @return ReplayReport with per-step original vs simulated decisions,
     *         firstDivergenceStep, and snapshotIdUsed
     */
    public ReplayReport simulate(UUID sessionId,
                                  List<EventDTO> events,
                                  PolicySnapshot originalSnapshot,
                                  PolicySnapshot alternateSnapshot) {

        List<EventDTO> sortedEvents = new ArrayList<>(events);
        sortedEvents.sort(Comparator.comparingLong(EventDTO::getTimestampNs));

        List<StepDecision> steps        = new ArrayList<>();
        List<String> originalDecisions  = new ArrayList<>();
        List<String> simulatedDecisions = new ArrayList<>();
        int firstDivergenceStep = -1;

        for (int i = 0; i < sortedEvents.size(); i++) {
            EventDTO event = sortedEvents.get(i);

            // Evaluate with original frozen snapshot
            String[] original   = ReplayEngine.evaluateRules(event, originalSnapshot);
            // Evaluate with alternate (what-if) snapshot
            String[] simulated  = ReplayEngine.evaluateRules(event, alternateSnapshot);

            String origDecision = original[0];
            String simDecision  = simulated[0];
            String ruleMatched  = simulated[1] != null ? simulated[1] : original[1];

            StepDecision step = new StepDecision(event, origDecision, simDecision, ruleMatched);
            steps.add(step);

            originalDecisions.add(origDecision);
            simulatedDecisions.add(simDecision);

            // Record the first step (1-based) where decisions diverge
            if (firstDivergenceStep == -1 && !origDecision.equals(simDecision)) {
                firstDivergenceStep = i + 1;
            }
        }

        ReplayReport report = new ReplayReport();
        report.setSessionId(sessionId);
        report.setSteps(steps);
        report.setOriginalDecisions(originalDecisions);
        report.setSimulatedDecisions(simulatedDecisions);
        report.setFirstDivergenceStep(firstDivergenceStep);
        // snapshotIdUsed comes from the alternateSnapshot once real persistence is in place
        report.setSnapshotIdUsed(null);

        return report;
    }

    /**
     * Convenience overload — uses empty original snapshot (backward compatible
     * with callers that only provide the alternate snapshot).
     */
    public ReplayReport simulate(UUID sessionId,
                                  List<EventDTO> events,
                                  PolicySnapshot alternateSnapshot) {
        return simulate(sessionId, events,
                new PolicySnapshot(java.util.Collections.emptyList()),
                alternateSnapshot);
    }
}
