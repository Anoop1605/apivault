package com.sentinel.eventstore.model;

import com.sentinel.shared.enums.Decision;
import com.sentinel.shared.enums.EventType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
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

    @Column(name = "session_id", nullable = false)
    private UUID sessionId;

    @Column(name = "timestamp_ns", nullable = false)
    private long timestampNs;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private EventType eventType;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "previous_hash")
    private String previousHash;

    @Column(name = "event_hash")
    private String eventHash;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "roles_json", columnDefinition = "jsonb")
    private List<String> roles;

    @Column(name = "endpoint")
    private String endpoint;

    @Column(name = "http_method")
    private String httpMethod;

    @Column(name = "source_ip")
    private String sourceIp;

    @Column(name = "user_agent")
    private String userAgent;

    @Enumerated(EnumType.STRING)
    @Column(name = "decision")
    private Decision decision;

    @Column(name = "policy_rule_id")
    private String policyRuleId;

    @Column(name = "policy_rule_version")
    private Integer policyRuleVersion;

    @Column(name = "policy_rule_snapshot", columnDefinition = "TEXT")
    private String policyRuleSnapshot;

    @Column(name = "policy_rule_snapshot_id")
    private UUID policyRuleSnapshotId;

    @Column(name = "risk_score")
    private Double riskScore;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "risk_signals", columnDefinition = "jsonb")
    private Map<String, Double> riskSignals;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "request_context", columnDefinition = "jsonb")
    private Map<String, String> requestContext;

    @Column(name = "body_hash")
    private String bodyHash;

    @Column(name = "gateway_version")
    private String gatewayVersion;

    @Column(name = "rule_matched")
    private String ruleMatched;

    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;
}
