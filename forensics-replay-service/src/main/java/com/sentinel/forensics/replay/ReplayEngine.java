package com.sentinel.forensics.replay;

import com.sentinel.shared.dto.EventDTO;
import com.sentinel.shared.dto.PolicySnapshot;
import com.sentinel.shared.dto.StepDecision;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Deterministic replay engine — PRD FR-RE-01, FR-RE-02.
 *
 * Reconstructs the exact gateway decision sequence for a session
 * using the frozen PolicySnapshot that was active at the time,
 * NOT the current live rules.
 */
public class ReplayEngine {

    /**
     * Reconstruct the decision sequence for a session.
     *
     * Rule evaluation logic (PRD FR-PE-02):
     *  - Each rule in the snapshot is a string token (e.g. "BLOCK_DELETE", "ALLOW_GET")
     *  - Format: "<DECISION>_<HTTP_METHOD>"  or  "<DECISION>_ALL"
     *  - First matching rule wins (priority order = snapshot list order)
     *  - Default: ALLOW if no rule matches (safe replay default)
     *
     * TODO: Replace rule string format with structured PolicyRule objects
     *       once policy-engine-service provides a real snapshot API.
     */
    public List<StepDecision> reconstruct(List<EventDTO> events, PolicySnapshot snapshot) {
        events.sort(Comparator.comparingLong(EventDTO::getTimestampNs));
        List<StepDecision> steps = new ArrayList<>();

        for (EventDTO event : steps_loop(events)) {
            String[] result = evaluateRules(event, snapshot);
            String decision   = result[0];
            String ruleMatched = result[1];

            StepDecision step = new StepDecision(event);
            step.setOriginalDecision(decision);
            step.setSimulatedDecision(decision); // same as original in a straight replay
            step.setRuleMatched(ruleMatched);
            steps.add(step);
        }

        return steps;
    }

    // -------------------------------------------------------------------------
    // Rule evaluation — matches snapshot rules against event fields
    // -------------------------------------------------------------------------

    /**
     * Evaluates the event against each rule in the snapshot (in order).
     * Returns [decision, ruleMatched].
     *
     * Rule token format examples:
     *   "BLOCK_DELETE"       → block if http_method == DELETE
     *   "BLOCK_ATTACK"       → block if event_type == ATTACK
     *   "ALLOW_GET"          → allow if http_method == GET
     *   "BLOCK_ALL"          → block everything (catch-all)
     *   "ALLOW_ALL"          → allow everything (catch-all)
     */
    static String[] evaluateRules(EventDTO event, PolicySnapshot snapshot) {
        if (snapshot == null || snapshot.getRules() == null || snapshot.getRules().isEmpty()) {
            return new String[]{"ALLOW", null};
        }

        for (String rule : snapshot.getRules()) {
            if (rule == null || rule.isBlank()) continue;
            String upper = rule.toUpperCase();
            String[] parts = upper.split("_", 2);
            if (parts.length < 2) continue;

            String ruleDecision = parts[0];  // ALLOW or BLOCK
            String criterion    = parts[1];  // DELETE, ATTACK, ALL, GET, etc.

            if (matches(event, criterion)) {
                return new String[]{ruleDecision, rule};
            }
        }

        // Default-allow if no rule matched (replay safe default)
        return new String[]{"ALLOW", null};
    }

    private static boolean matches(EventDTO event, String criterion) {
        switch (criterion) {
            case "ALL":
                return true;
            case "DELETE": case "GET": case "POST": case "PUT": case "PATCH":
                return criterion.equals(
                        event.getHttpMethod() != null ? event.getHttpMethod().toUpperCase() : "");
            case "ATTACK": case "LOGIN": case "LOGOUT": case "ACCESS":
                return criterion.equals(
                        event.getEventType() != null ? event.getEventType().toUpperCase() : "");
            default:
                // Match against policyRuleId or endpoint substring
                return (event.getPolicyRuleId() != null
                        && event.getPolicyRuleId().toUpperCase().contains(criterion))
                    || (event.getEndpoint() != null
                        && event.getEndpoint().toUpperCase().contains(criterion));
        }
    }

    /** Simple pass-through to allow cleaner loop syntax. */
    private static List<EventDTO> steps_loop(List<EventDTO> events) {
        return events;
    }
}
