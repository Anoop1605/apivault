package com.sentinel.eventstore.policy;

import com.sentinel.shared.enums.Decision;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "policy_rules_history")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PolicyRuleHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID snapshotId;

    private String ruleId;

    private Integer version;

    // 🔥 full rule snapshot (JSON / text)
    @Column(columnDefinition = "TEXT")
    private String fullConditions;

    @Enumerated(EnumType.STRING)
    private Decision decision;

    private Instant activatedAt;

    private String activatedBy;

    private Instant deactivatedAt;
}