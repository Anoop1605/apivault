package com.sentinel.policy.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * ABAC (Attribute-Based Access Control) Policy Rule
 * Represents a single access control rule with conditions and effect.
 * Persisted to policy_rules table in PostgreSQL with JSONB support for conditions.
 */
@Entity
@Table(name = "policy_rules")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PolicyRule implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "rule_id", unique = true, nullable = false)
    private String ruleId;

    @Column(name = "version", nullable = false)
    private Integer version;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "conditions", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private List<ConditionSpec> conditions;

    @Enumerated(EnumType.STRING)
    @Column(name = "decision", nullable = false)
    private PolicyEffect effect; // ALLOW or DENY

    @Column(name = "priority", nullable = false)
    private Integer priority; // Higher priority evaluated first

    @Column(name = "active", nullable = false)
    private Boolean active;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (version == null) version = 1;
        if (active == null) active = true;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum PolicyEffect {
        ALLOW, DENY
    }

    /**
     * Condition specification as stored in JSONB
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ConditionSpec {
        private String type; // ROLE, TIME, IP, RISK, etc.
        private String operator; // equals, contains, between, in_cidr, etc.
        private Object value; // Type depends on condition
    }
}
