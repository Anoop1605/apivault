package com.sentinel.forensics.replay;

import com.sentinel.shared.dto.EventDTO;
import com.sentinel.shared.dto.PolicySnapshot;
import com.sentinel.forensics.client.PolicyEngineClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * ReplayEngine — Orchestrates event replay for forensics analysis.
 * Replays historical security events to reconstruct attack sequences.
 * PRD FR-RE-01, FR-RE-02: Replays events with a frozen policy snapshot.
 */
@Slf4j
@Component
public class ReplayEngine {

    private final PolicyEngineClient policyEngineClient;

    public ReplayEngine(PolicyEngineClient policyEngineClient) {
        this.policyEngineClient = policyEngineClient;
        log.info("ReplayEngine initialized");
    }

    /**
     * Evaluate a single event against the given policy rules.
     * 
     * @param event          The event to evaluate
     * @param policySnapshot The frozen policy state
     * @return EvaluationResult with decision and matched rule ID
     */
    public EvaluationResult evaluateRules(EventDTO event, PolicySnapshot policySnapshot) {
        log.debug("Evaluating event {} against {} policy rules",
                event.getEventId(),
                policySnapshot != null ? policySnapshot.getRules().size() : 0);

        // Actually invoke the Policy Engine via the REST boundary
        return policyEngineClient.evaluate(event, policySnapshot);
    }

    /**
     * Reconstruct a sequence of events with policy decisions.
     * This is the straight replay: no policy changes.
     *
     * @param events         The events to reconstruct
     * @param policySnapshot The policy state to apply
     * @return List of evaluated results
     */
    public List<EvaluationResult> reconstruct(List<EventDTO> events, PolicySnapshot policySnapshot) {
        log.debug("Reconstructing {} events with policy snapshot", events.size());
        List<EvaluationResult> results = new ArrayList<>();

        for (EventDTO event : events) {
            EvaluationResult result = evaluateRules(event, policySnapshot);
            results.add(result);
        }

        return results;
    }
}
