package com.sentinel.forensics.replay;

import com.sentinel.shared.dto.EventDTO;
import com.sentinel.shared.dto.PolicySnapshot;
import com.sentinel.shared.dto.ReplayReport;
import com.sentinel.shared.dto.StepDecision;
import com.sentinel.shared.enums.Decision;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
@Slf4j
public class WhatIfSimulationEngine {

    private final ReplayEngine replayEngine;

    public WhatIfSimulationEngine(ReplayEngine replayEngine) {
        this.replayEngine = replayEngine;
    }

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

        // Reconstruct with original snapshot
        List<StepDecision> stepsOriginal = replayEngine.reconstruct(events, originalSnapshot);

        // Reconstruct with alternate snapshot
        List<StepDecision> stepsSimulated = replayEngine.simulateWithModifiedRules(events, originalSnapshot, alternateSnapshot);

        // Count divergences and build report
        int divergenceCount = 0;
        int firstDivergenceStep = -1;

        List<Decision> originalDecisions = new ArrayList<>();
        List<Decision> simulatedDecisions = new ArrayList<>();

        for (int i = 0; i < stepsOriginal.size(); i++) {
            StepDecision origStep = stepsOriginal.get(i);
            StepDecision simStep = stepsSimulated.get(i);

            String origDecisionStr = origStep.getOriginalDecision();
            String simDecisionStr = simStep.getSimulatedDecision();

            if (origDecisionStr != null) {
                originalDecisions.add(Decision.valueOf(origDecisionStr));
            }
            if (simDecisionStr != null) {
                simulatedDecisions.add(Decision.valueOf(simDecisionStr));
            }

            if (!origDecisionStr.equals(simDecisionStr)) {
                divergenceCount++;
                if (firstDivergenceStep == -1) {
                    firstDivergenceStep = i + 1;
                }
            }
        }

        ReplayReport report = ReplayReport.builder()
                .sessionId(sessionId)
                .totalEvents(events.size())
                .divergenceCount(divergenceCount)
                .steps(stepsSimulated)
                .isSimulation(true)
                .generatedAt(LocalDateTime.now())
                .build();

        log.info("What-if simulation complete: {} divergences out of {} events",
                divergenceCount, events.size());

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
