package com.sentinel.eventstore.model;

import com.sentinel.shared.enums.Decision;
import com.sentinel.shared.enums.EventType;
import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "security_events")
public class SecurityEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "session_id", nullable = false)
    private UUID sessionId;

    @Column(name = "timestamp_ns", nullable = false)
    private long timestampNs;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private EventType eventType;

    @Enumerated(EnumType.STRING)
    @Column(name = "decision")
    private Decision decision;

    @Column(name = "rule_matched")
    private String ruleMatched;

    @Column(name = "source_ip")
    private String sourceIp;

    @Column(name = "endpoint")
    private String endpoint;

    public SecurityEvent() {}

    public UUID getId() { return id; }

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

    public String getSourceIp() { return sourceIp; }
    public void setSourceIp(String sourceIp) { this.sourceIp = sourceIp; }

    public String getEndpoint() { return endpoint; }
    public void setEndpoint(String endpoint) { this.endpoint = endpoint; }
}
