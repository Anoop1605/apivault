package com.sentinel.eventstore.policy;

import com.sentinel.shared.enums.Decision;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "policy_rules")
@Data
public class PolicyRule {

    @Id
    private String ruleId;

    private Integer version;

    @Column(columnDefinition = "jsonb")
    private String conditions;

    @Enumerated(EnumType.STRING)
    private Decision decision;
}