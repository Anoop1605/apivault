package com.sentinel.shared.dto;

import com.sentinel.shared.enums.Decision;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Replay report produced by the Forensic Replay Engine.
 * Contains step-by-step decision trace and tampering detection results.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReplayReport {

    /** Session ID being replayed */
    private UUID sessionId;

    /** Total number of events in the session */
    private Integer totalEvents;

    /** Number of divergences found between original and reconstructed decisions */
    private Integer divergenceCount;

    /** Whether tampering was detected in any events */
    private Boolean tamperingDetected;

    /** List of event IDs that were tampered with */
    private List<String> tamperedEventIds;

    /** Ordered list of replay steps showing original vs simulated decisions */
    private List<StepDecision> steps;

    /** Whether this is a simulation (true) or actual replay (false) */
    private Boolean isSimulation;

    /** Timestamp when report was generated */
    private LocalDateTime generatedAt;

    /** Computed hash of the replay report for integrity verification */
    private String reportHash;
}

