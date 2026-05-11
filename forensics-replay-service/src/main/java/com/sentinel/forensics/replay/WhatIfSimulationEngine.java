package com.sentinel.forensics.replay;

import com.sentinel.shared.dto.EventDTO;
import com.sentinel.shared.dto.PolicySnapshot;
import com.sentinel.shared.dto.ReplayReport;
import com.sentinel.shared.enums.Decision;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
@Slf4j
@Service
@RequiredArgsConstructor
public class WhatIfSimulationEngine {

    private final ReplayEngine replayEngine;

    /**
     * Simulate replay with an alternate PolicySnapshot.
     *
     * @param sessionId         Session UUID
     * @param events            All events for the session (will be sorted
     *                          internally)
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

        List<ReplayReport.StepDecision> steps = new ArrayList<>();
        List<Decision> originalDecisions = new ArrayList<>();
        List<Decision> simulatedDecisions = new ArrayList<>();
        int firstDivergenceStep = -1;

        for (int i = 0; i < sortedEvents.size(); i++) {
            EventDTO event = sortedEvents.get(i);

            // Evaluate with original frozen snapshot
            EvaluationResult originalEval = replayEngine.evaluateRules(event, originalSnapshot);
            // Evaluate with alternate (what-if) snapshot
            EvaluationResult simulatedEval = replayEngine.evaluateRules(event, alternateSnapshot);

            Decision origDecision = Decision.valueOf(originalEval.getDecision());
            Decision simDecision = Decision.valueOf(simulatedEval.getDecision());
            String ruleMatched = simulatedEval.getRuleMatched() != null
                    ? simulatedEval.getRuleMatched()
                    : originalEval.getRuleMatched();

            // Build ReplayReport.StepDecision (the nested class)
            ReplayReport.StepDecision step = ReplayReport.StepDecision.builder()
                    .eventId(event.getEventId())
                    .timestampNs(event.getTimestampNs())
                    .eventType(event.getEventType() != null ? event.getEventType().toString() : "UNKNOWN")
                    .endpoint(event.getEndpoint())
                    .httpMethod(event.getHttpMethod())
                    .originalDecision(origDecision)
                    .simulatedDecision(simDecision)
                    .ruleId(ruleMatched)
                    .riskScore(event.getRiskScore())
                    .build();

            steps.add(step);
            originalDecisions.add(origDecision);
            simulatedDecisions.add(simDecision);

            // Record the first step (1-based) where decisions diverge
            if (firstDivergenceStep == -1 && !origDecision.equals(simDecision)) {
                firstDivergenceStep = i + 1;
            }
        }

        ReplayReport report = ReplayReport.builder()
                .sessionId(sessionId)
                .steps(steps)
                .originalDecisions(originalDecisions)
                .simulatedDecisions(simulatedDecisions)
                .firstDivergenceStep(firstDivergenceStep)
                .snapshotIdUsed(null) // TODO: use real snapshot ID when persistence added
                .build();

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
