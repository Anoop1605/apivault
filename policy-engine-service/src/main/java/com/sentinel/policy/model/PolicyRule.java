package com.sentinel.policy.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * PolicyRule — Represents an ABAC policy rule in the system.
 * Stored in policy_rules table; immutable snapshots in policy_rules_history.
 */
@Entity
@Table(name = "policy_rules")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PolicyRule {

    @Id
    private UUID id;

    @Column(name = "rule_id", nullable = false, unique = true)
    private String ruleId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "priority", nullable = false)
    private Integer priority;

    @Column(name = "active", nullable = false)
    private Boolean active;

    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    @Column(name = "conditions", columnDefinition = "jsonb")
    private List<ConditionSpec> conditions;

    @Column(name = "decision", nullable = false)
    private String effect;

    @Column(name = "version", nullable = false)
    private Integer version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Transient
    private UUID snapshotId;

    /**
     * Represents a single condition specification within a rule.
     * Example: { "type": "ROLE", "operator": "EQUALS", "value": "admin" }
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConditionSpec {
        private String type; // ROLE, TIME, IP, RISK, etc.
        private String operator; // EQUALS, CONTAINS, IN_CIDR, BETWEEN, GREATER_THAN, etc.
        private Object value; // The value to match against
    }
}
