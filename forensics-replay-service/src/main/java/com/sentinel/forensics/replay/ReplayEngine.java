package com.sentinel.forensics.replay;

import com.sentinel.shared.dto.EventDTO;
import com.sentinel.shared.dto.PolicySnapshot;
import com.sentinel.shared.dto.RequestContext;
import com.sentinel.shared.dto.StepDecision;
import com.sentinel.shared.enums.Decision;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * ReplayEngine — Reconstructs and replays a session using frozen policy snapshots.
 * Implements deterministic replay for forensic analysis and what-if simulation.
 *
 * PRD Section 9: Forensic Replay Engine
 */
@Slf4j
@Service
public class ReplayEngine {

    /**
     * Reconstruct a session by replaying all events through the original policy snapshot.
     * Returns list of StepDecision objects with original vs reconstructed decisions.
     *
     * @param events Events from the session (in timestamp order)
     * @param snapshot Frozen policy snapshot at time of original evaluation
     * @return List of step decisions showing original vs replayed decisions
     */
    public List<StepDecision> reconstruct(List<EventDTO> events, PolicySnapshot snapshot) {
        List<StepDecision> steps = new ArrayList<>();

        if (events == null || events.isEmpty()) {
            log.info("No events to replay");
            return steps;
        }

        log.info("Reconstructing session with {} events using snapshot", events.size());

        for (EventDTO event : events) {
            // Evaluate the event using the frozen policy snapshot
            Decision reconstructedDecision = evaluateRules(event, snapshot);

            // Create step decision with original vs reconstructed
            StepDecision step = new StepDecision(event);
            step.setOriginalDecision(event.getDecision() != null ? event.getDecision().toString() : null);
            step.setSimulatedDecision(reconstructedDecision != null ? reconstructedDecision.toString() : null);
            step.setRuleMatched(event.getPolicyRuleId());

            // Check if decisions diverged
            boolean diverged = !String.valueOf(event.getDecision()).equals(String.valueOf(reconstructedDecision));
            step.setDiverged(diverged);

            if (diverged) {
                log.warn("DIVERGENCE: Event {} - Original: {}, Reconstructed: {}",
                        event.getEventId(), event.getDecision(), reconstructedDecision);
            }

            steps.add(step);
        }

        log.info("Reconstruction complete: {} total events, {} divergences",
                events.size(), steps.stream().filter(StepDecision::isDiverged).count());

        return steps;
    }

    /**
     * Evaluate a single event using the frozen policy snapshot.
     * Returns the decision that would have been made at that time.
     *
     * @param event Event to evaluate
     * @param snapshot Frozen policy snapshot
     * @return Decision from frozen snapshot (ALLOW, DENY, or FLAG)
     */
    public static Decision evaluateRules(EventDTO event, PolicySnapshot snapshot) {
        if (event == null) {
            return Decision.DENY;
        }

        if (snapshot == null || snapshot.getRules() == null || snapshot.getRules().isEmpty()) {
            log.warn("No policy snapshot available, defaulting to DENY (zero-trust)");
            return Decision.DENY;
        }

        // In a real implementation, this would:
        // 1. Parse the snapshot rules
        // 2. Build RequestContext from event
        // 3. Evaluate against each rule in priority order
        // 4. Return first matching decision

        // For now, simplified implementation:
        // If snapshot has rules, iterate through them in order
        for (String ruleSpec : snapshot.getRules()) {
            // Simplified: check if event risk score triggers a DENY rule
            if (event.getRiskScore() != null && event.getRiskScore() > 0.8) {
                if (ruleSpec.contains("DENY_HIGH_RISK")) {
                    return Decision.DENY;
                }
            }

            // Check if role matches an ALLOW rule
            if (event.getRoles() != null && !event.getRoles().isEmpty()) {
                if (ruleSpec.contains("ALLOW_ADMIN") && event.getRoles().contains("admin")) {
                    return Decision.ALLOW;
                }
            }
        }

        // Default: DENY (zero-trust principle)
        log.debug("No matching rule in snapshot for event {}, defaulting to DENY", event.getEventId());
        return Decision.DENY;
    }

    /**
     * What-if simulation: replay events with modified policy rules.
     * Compare original decisions against decisions with modified ruleset.
     *
     * @param events Events from the session
     * @param originalSnapshot Original frozen snapshot
     * @param modifiedSnapshot Modified snapshot for what-if analysis
     * @return List of step decisions showing original vs simulated decisions
     */
    public List<StepDecision> simulateWithModifiedRules(List<EventDTO> events,
                                                       PolicySnapshot originalSnapshot,
                                                       PolicySnapshot modifiedSnapshot) {
        List<StepDecision> steps = new ArrayList<>();

        if (events == null || events.isEmpty()) {
            return steps;
        }

        log.info("Running what-if simulation with {} events", events.size());

        for (EventDTO event : events) {
            StepDecision step = new StepDecision(event);

            // Original decision from the actual event
            step.setOriginalDecision(event.getDecision() != null ? event.getDecision().toString() : null);

            // Simulated decision using modified rules
            Decision simulatedDecision = evaluateRules(event, modifiedSnapshot);
            step.setSimulatedDecision(simulatedDecision != null ? simulatedDecision.toString() : null);
            step.setRuleMatched(event.getPolicyRuleId());

            // Check for divergence
            boolean diverged = !String.valueOf(event.getDecision()).equals(String.valueOf(simulatedDecision));
            step.setDiverged(diverged);

            if (diverged) {
                log.info("What-if divergence at event {} - Original: {}, Simulated: {}",
                        event.getEventId(), event.getDecision(), simulatedDecision);
            }

            steps.add(step);
        }

        return steps;
    }
}
