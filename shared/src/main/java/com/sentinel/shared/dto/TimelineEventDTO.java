package com.sentinel.shared.dto;

import com.sentinel.shared.enums.Decision;
import com.sentinel.shared.enums.EventType;

import java.util.UUID;

public class TimelineEventDTO {

    private UUID sessionId;
    private long timestampNs;
    private EventType eventType;
    private Decision decision;
    private String ruleMatched;

    public TimelineEventDTO() {}

    public TimelineEventDTO(UUID sessionId, long timestampNs, EventType eventType,
                             Decision decision, String ruleMatched) {
        this.sessionId = sessionId;
        this.timestampNs = timestampNs;
        this.eventType = eventType;
        this.decision = decision;
        this.ruleMatched = ruleMatched;
    }

    public UUID getSessionId() { return sessionId; }
    public void setSessionId(UUID sessionId) { this.sessionId = sessionId; }

    public long getTimestampNs() { return timestampNs; }
    public void setTimestampNs(long timestampNs) { this.timestampNs = timestampNs; }

    public EventType getEventType() { return eventType; }
    public void setEventType(EventType eventType) { this.eventType = eventType; }

    public Decision getDecision() { return decision; }
    public void setDecision(Decision decision) { this.decision = decision; }

    public String getRuleMatched() { return ruleMatched; }
    public void setRuleMatched(String ruleMatched) { this.ruleMatched = ruleMatched; }
}
