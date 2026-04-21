package com.sentinel.shared.dto;

import com.sentinel.shared.enums.Decision;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.UUID;

/**
 * Replay report produced by the Forensic Replay Engine.
 * Used by the Dashboard to display original vs. simulated outcomes.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReplayReport {

    /** Ordered list of replay steps. */
    private List<StepDecision> steps;

    /** Original decisions for each step. */
    private List<Decision> originalDecisions;

    /** Simulated decisions for each step (with modified rules). */
    private List<Decision> simulatedDecisions;

    /** Index of the first step where original and simulated outcomes diverge. */
    private Integer firstDivergenceStep;

    /** Snapshot ID used for the replay. */
    private UUID snapshotIdUsed;

    /** The ID of the session. */
    private UUID sessionId;

    /** Computed hash of the replay report. */
    private String hash;

    /**
     * Represents a single step in the replay.
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class StepDecision {
        private UUID eventId;
        private Long timestampNs;
        private String eventType;
        private String endpoint;
        private String httpMethod;
        private Decision originalDecision;
        private Decision simulatedDecision;
        private String ruleId;
        private Double riskScore;
    }
}
