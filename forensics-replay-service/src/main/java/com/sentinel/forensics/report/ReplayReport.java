package com.sentinel.forensics.report;

import com.sentinel.shared.enums.Decision;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * ReplayReport — Local forensics-module DTO representing the full output
 * of a replay or what-if simulation run.
 *
 * This mirrors the shared ReplayReport DTO but lives in the forensics module
 * for internal use. The shared version (com.sentinel.shared.dto.ReplayReport)
 * is used for cross-service API contracts.
 *
 * Contains:
 *  - Per-step decisions (original vs simulated)
 *  - First divergence step index
 *  - Session hash for tamper detection
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReplayReport {

    private UUID sessionId;
    private List<StepDecision> steps;
    private List<Decision> originalDecisions;
    private List<Decision> simulatedDecisions;
    private Integer firstDivergenceStep;
    private UUID snapshotIdUsed;
    private String hash;
}
