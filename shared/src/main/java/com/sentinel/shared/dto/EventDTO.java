package com.sentinel.shared.dto;

import com.sentinel.shared.enums.Decision;
import com.sentinel.shared.enums.EventType;
import lombok.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EventDTO {

    private UUID eventId;
    private Long timestampNs;
    private EventType eventType;
    private UUID sessionId;
    private String userId;
    private List<String> roles;
    private String endpoint;
    private String httpMethod;
    private String sourceIp;
    private String userAgent;

    private String policyRuleId;
    private Integer policyRuleVersion;

    // ✅ NEW
    private String policyRuleSnapshot;

    private UUID policyRuleSnapshotId;

    private Double riskScore;
    private Map<String, Double> riskSignals;
    private Decision decision;
    private Map<String, String> requestContext;
    private String bodyHash;
    private String eventHash;
    private String gatewayVersion;
}