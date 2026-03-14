package com.sentinel.shared.dto;

import com.sentinel.shared.enums.EventType;

import java.util.UUID;

public class AlertDTO {

    public enum Severity { LOW, MEDIUM, HIGH, CRITICAL }

    private UUID sessionId;
    private long timestampNs;
    private EventType triggerEventType;
    private Severity severity;
    private String message;

    public AlertDTO() {}

    public AlertDTO(UUID sessionId, long timestampNs, EventType triggerEventType,
                    Severity severity, String message) {
        this.sessionId = sessionId;
        this.timestampNs = timestampNs;
        this.triggerEventType = triggerEventType;
        this.severity = severity;
        this.message = message;
    }

    public UUID getSessionId() { return sessionId; }
    public void setSessionId(UUID sessionId) { this.sessionId = sessionId; }

    public long getTimestampNs() { return timestampNs; }
    public void setTimestampNs(long timestampNs) { this.timestampNs = timestampNs; }

    public EventType getTriggerEventType() { return triggerEventType; }
    public void setTriggerEventType(EventType triggerEventType) { this.triggerEventType = triggerEventType; }

    public Severity getSeverity() { return severity; }
    public void setSeverity(Severity severity) { this.severity = severity; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
