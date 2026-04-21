package com.apivault.eventstore.dto;

public class EventDTO {

    public String eventType;
    public String endpoint;
    public String httpMethod;

    public String userId;
    public String sessionId;

    public String decision;
    public String policyRuleId;

    public Double riskScore;
}