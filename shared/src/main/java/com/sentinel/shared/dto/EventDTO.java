package com.sentinel.shared.dto;

import java.util.UUID;

public class EventDTO {

    // PRD §10.3 — all 13 canonical fields required for SHA-256 event hash
    private UUID eventId;
    private UUID sessionId;
    private long timestampNs;
    private String eventType;
    private String userId;
    private String endpoint;
    private String httpMethod;
    private String decision;
    private String policyRuleId;
    private Integer policyRuleVersion;
    private UUID policyRuleSnapshotId;
    private Float riskScore;
    private String bodyHash;
    private String gatewayVersion;

    public UUID getEventId() { return eventId; }
    public void setEventId(UUID eventId) { this.eventId = eventId; }

    public UUID getSessionId() { return sessionId; }
    public void setSessionId(UUID sessionId) { this.sessionId = sessionId; }

    public long getTimestampNs() { return timestampNs; }
    public void setTimestampNs(long timestampNs) { this.timestampNs = timestampNs; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getEndpoint() { return endpoint; }
    public void setEndpoint(String endpoint) { this.endpoint = endpoint; }

    public String getHttpMethod() { return httpMethod; }
    public void setHttpMethod(String httpMethod) { this.httpMethod = httpMethod; }

    public String getDecision() { return decision; }
    public void setDecision(String decision) { this.decision = decision; }

    public String getPolicyRuleId() { return policyRuleId; }
    public void setPolicyRuleId(String policyRuleId) { this.policyRuleId = policyRuleId; }

    public Integer getPolicyRuleVersion() { return policyRuleVersion; }
    public void setPolicyRuleVersion(Integer policyRuleVersion) { this.policyRuleVersion = policyRuleVersion; }

    public UUID getPolicyRuleSnapshotId() { return policyRuleSnapshotId; }
    public void setPolicyRuleSnapshotId(UUID policyRuleSnapshotId) { this.policyRuleSnapshotId = policyRuleSnapshotId; }

    public Float getRiskScore() { return riskScore; }
    public void setRiskScore(Float riskScore) { this.riskScore = riskScore; }

    public String getBodyHash() { return bodyHash; }
    public void setBodyHash(String bodyHash) { this.bodyHash = bodyHash; }

    public String getGatewayVersion() { return gatewayVersion; }
    public void setGatewayVersion(String gatewayVersion) { this.gatewayVersion = gatewayVersion; }
}
