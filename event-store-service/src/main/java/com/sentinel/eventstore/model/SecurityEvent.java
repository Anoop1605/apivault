package com.sentinel.eventstore.model;

import com.sentinel.shared.enums.Decision;
import com.sentinel.shared.enums.EventType;
import jakarta.persistence.*;

import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "events")
public class SecurityEvent {

    @Id
    @Column(name = "event_id")
    private UUID id;

    @Column(name = "session_id", nullable = false)
    private UUID sessionId;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "timestamp_ns", nullable = false)
    private long timestampNs;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "timestamp")
    private Date timestamp;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private EventType eventType;

    @Enumerated(EnumType.STRING)
    @Column(name = "decision")
    private Decision decision;

    @Column(name = "http_method")
    private String httpMethod;

    @Column(name = "source_ip")
    private String sourceIp;

    @Column(name = "endpoint")
    private String endpoint;

    @Column(name = "risk_score")
    private Double riskScore;

    @Column(name = "gateway_version")
    private String gatewayVersion;

    @Column(name = "previous_hash")
    private String previousHash;

    @Column(name = "current_hash")
    private String currentHash;

    @Column(name = "policy_rule_id")
    private String policyRuleId;

    @Column(name = "policy_rule_version")
    private Integer policyRuleVersion;

    @Column(name = "policy_rule_snapshot_id")
    private UUID policyRuleSnapshotId;

    @Column(name = "body_hash")
    private String bodyHash;

    public SecurityEvent() {}

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getSessionId() { return sessionId; }
    public void setSessionId(UUID sessionId) { this.sessionId = sessionId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public long getTimestampNs() { return timestampNs; }
    public void setTimestampNs(long timestampNs) { this.timestampNs = timestampNs; }

    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }

    public EventType getEventType() { return eventType; }
    public void setEventType(EventType eventType) { this.eventType = eventType; }

    public Decision getDecision() { return decision; }
    public void setDecision(Decision decision) { this.decision = decision; }

    public String getHttpMethod() { return httpMethod; }
    public void setHttpMethod(String httpMethod) { this.httpMethod = httpMethod; }

    public String getSourceIp() { return sourceIp; }
    public void setSourceIp(String sourceIp) { this.sourceIp = sourceIp; }

    public String getEndpoint() { return endpoint; }
    public void setEndpoint(String endpoint) { this.endpoint = endpoint; }

    public Double getRiskScore() { return riskScore; }
    public void setRiskScore(Double riskScore) { this.riskScore = riskScore; }

    public String getGatewayVersion() { return gatewayVersion; }
    public void setGatewayVersion(String gatewayVersion) { this.gatewayVersion = gatewayVersion; }

    public String getPreviousHash() { return previousHash; }
    public void setPreviousHash(String previousHash) { this.previousHash = previousHash; }

    public String getCurrentHash() { return currentHash; }
    public void setCurrentHash(String currentHash) { this.currentHash = currentHash; }

    public String getPolicyRuleId() { return policyRuleId; }
    public void setPolicyRuleId(String policyRuleId) { this.policyRuleId = policyRuleId; }

    public Integer getPolicyRuleVersion() { return policyRuleVersion; }
    public void setPolicyRuleVersion(Integer policyRuleVersion) { this.policyRuleVersion = policyRuleVersion; }

    public UUID getPolicyRuleSnapshotId() { return policyRuleSnapshotId; }
    public void setPolicyRuleSnapshotId(UUID policyRuleSnapshotId) { this.policyRuleSnapshotId = policyRuleSnapshotId; }

    public String getBodyHash() { return bodyHash; }
    public void setBodyHash(String bodyHash) { this.bodyHash = bodyHash; }
}
