package com.sentinel.forensics.service;

import com.sentinel.forensics.engine.ReplayHashUtil;
import com.sentinel.forensics.event.EventStoreClient;
import com.sentinel.shared.dto.AlertDTO;
import com.sentinel.shared.dto.EventDTO;
import com.sentinel.shared.dto.PolicySnapshot;
import com.sentinel.shared.dto.PolicyTraceDTO;
import com.sentinel.shared.dto.TimelineEventDTO;
import com.sentinel.shared.enums.Decision;
import com.sentinel.shared.enums.EventType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class DashboardService {

    private final EventStoreClient eventStoreClient;

    public DashboardService(EventStoreClient eventStoreClient) {
        this.eventStoreClient = eventStoreClient;
    }

    /**
     * Returns a chronologically sorted timeline of events for a session,
     * each annotated with an event type and placeholder decision.
     *
     * TODO: Populate eventType and decision from real policy evaluation once
     *       ReplayEngine returns enriched StepDecision objects.
     */
    public List<TimelineEventDTO> getSessionTimeline(UUID sessionId) {
        List<EventDTO> events = eventStoreClient.fetchEvents(sessionId);
        List<TimelineEventDTO> timeline = new ArrayList<>();

        EventType[] types = EventType.values();
        for (int i = 0; i < events.size(); i++) {
            EventDTO event = events.get(i);
            EventType eventType = types[i % types.length];
            // Default ALLOW; DENY is flagged as an alert in getAlerts()
            Decision decision = (eventType == EventType.ATTACK) ? Decision.DENY : Decision.ALLOW;
            timeline.add(new TimelineEventDTO(
                    event.getSessionId(),
                    event.getTimestampNs(),
                    eventType,
                    decision,
                    decision == Decision.DENY ? "RULE_ATTACK_DETECTED" : null
            ));
        }

        timeline.sort((a, b) -> Long.compare(a.getTimestampNs(), b.getTimestampNs()));
        return timeline;
    }

    /**
     * Returns the policy trace for a session — which rules were evaluated,
     * which matched, and the final aggregated decision.
     *
     * TODO: Source rulesEvaluated from the frozen PolicySnapshot for this session.
     */
    public PolicyTraceDTO getPolicyTrace(UUID sessionId) {
        List<EventDTO> events = eventStoreClient.fetchEvents(sessionId);
        PolicySnapshot snapshot = new PolicySnapshot(Collections.emptyList());

        // Placeholder rules — replace with real snapshot lookup
        List<String> rulesEvaluated = List.of(
                "RULE_RATE_LIMIT",
                "RULE_ATTACK_DETECTED",
                "RULE_AUTH_REQUIRED"
        );

        // Determine which rules matched by scanning the timeline
        List<TimelineEventDTO> timeline = getSessionTimeline(sessionId);
        List<String> rulesMatched = new ArrayList<>();
        boolean hasDeny = false;
        for (TimelineEventDTO step : timeline) {
            if (step.getRuleMatched() != null && !rulesMatched.contains(step.getRuleMatched())) {
                rulesMatched.add(step.getRuleMatched());
            }
            if (step.getDecision() == Decision.DENY) {
                hasDeny = true;
            }
        }

        Decision finalDecision = hasDeny ? Decision.DENY : Decision.ALLOW;
        String hash = ReplayHashUtil.computeSessionHash(events, snapshot);

        return new PolicyTraceDTO(sessionId, rulesEvaluated, rulesMatched, finalDecision, hash);
    }

    /**
     * Returns alerts for a session — events that resulted in a DENY or FLAG decision.
     */
    public List<AlertDTO> getAlerts(UUID sessionId) {
        List<TimelineEventDTO> timeline = getSessionTimeline(sessionId);
        List<AlertDTO> alerts = new ArrayList<>();

        for (TimelineEventDTO step : timeline) {
            if (step.getDecision() == Decision.DENY) {
                alerts.add(new AlertDTO(
                        step.getSessionId(),
                        step.getTimestampNs(),
                        step.getEventType(),
                        AlertDTO.Severity.HIGH,
                        "Event blocked by policy: " + step.getRuleMatched()
                ));
            } else if (step.getDecision() == Decision.FLAG) {
                alerts.add(new AlertDTO(
                        step.getSessionId(),
                        step.getTimestampNs(),
                        step.getEventType(),
                        AlertDTO.Severity.MEDIUM,
                        "Event flagged for review"
                ));
            }
        }

        return alerts;
    }
}
