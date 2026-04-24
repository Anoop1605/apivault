package com.sentinel.eventstore.model;

import com.sentinel.shared.enums.Decision;
import com.sentinel.shared.enums.EventType;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "security_events")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SecurityEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID sessionId;
    private long timestampNs;

    @Enumerated(EnumType.STRING)
    private EventType eventType;

    private String userId;
    private String previousHash;
    private String eventHash;

    // ✅ ROLES
    @ElementCollection
    @CollectionTable(name = "event_roles", joinColumns = @JoinColumn(name = "event_id"))
    @Column(name = "role")
    private List<String> roles;

    private String endpoint;
    private String httpMethod;
    private String sourceIp;
    private String userAgent;

    @Enumerated(EnumType.STRING)
    private Decision decision;

    private String policyRuleId;
    private Integer policyRuleVersion;
    private UUID policyRuleSnapshotId;

    private Double riskScore;

    // ✅ FIXED (NO reserved keywords)
    @ElementCollection
    @CollectionTable(name = "event_risk_signals", joinColumns = @JoinColumn(name = "event_id"))
    @MapKeyColumn(name = "signal_key")
    @Column(name = "signal_value")
    private Map<String, Double> riskSignals;

    // ✅ FIXED (NO reserved keywords)
    @ElementCollection
    @CollectionTable(name = "event_request_context", joinColumns = @JoinColumn(name = "event_id"))
    @MapKeyColumn(name = "ctx_key")
    @Column(name = "ctx_value")
    private Map<String, String> requestContext;

    private String bodyHash;
    private String gatewayVersion;

    private String ruleMatched;
}