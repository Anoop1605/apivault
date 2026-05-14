package com.sentinel.policy.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
import java.util.UUID;

@Entity
@Table(name = "policy_rules_history")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PolicyRuleHistory {

    @Id
    @Column(name = "snapshot_id")
    private UUID snapshotId;

    @Column(name = "rule_id", nullable = false)
    private String ruleId;

    @Column(name = "rule_version", nullable = false)
    private Integer ruleVersion;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "snapshot_json", columnDefinition = "jsonb", nullable = false)
    private List<String> snapshotJson;

    @Column(name = "activated_by")
    private String activatedBy;

    @Column(name = "activated_at", nullable = false)
    private OffsetDateTime activatedAt;

    @Column(name = "is_active", nullable = false)
    private Boolean active;

    @Column(name = "reason")
    private String reason;
}
