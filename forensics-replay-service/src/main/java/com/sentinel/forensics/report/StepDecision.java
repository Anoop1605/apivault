package com.sentinel.forensics.report;

import com.sentinel.shared.enums.Decision;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * StepDecision — Represents one event step in a replay report.
 *
 * Local forensics-module DTO mirroring the nested StepDecision in the
 * shared ReplayReport. Used during internal replay processing before
 * the final report is assembled.
 *
 * Contains:
 *  - The event metadata (ID, timestamp, endpoint, method)
 *  - The original decision from the frozen snapshot
 *  - The simulated decision from the alternate snapshot
 *  - Whether the decisions diverged
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StepDecision {

    private UUID eventId;
    private Long timestampNs;
    private String eventType;
    private String endpoint;
    private String httpMethod;
    private Decision originalDecision;
    private Decision simulatedDecision;
    private String ruleId;
    private Double riskScore;

    /**
     * Returns true if the original and simulated decisions are different.
     */
    public boolean isDiverged() {
        if (originalDecision == null || simulatedDecision == null) {
            return false;
        }
        return !originalDecision.equals(simulatedDecision);
    }
}
