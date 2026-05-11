package com.sentinel.shared.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * VerificationResult — Result of hash-based integrity verification.
 * Used to report tampering detection on a batch of events.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerificationResult {

    /** Total number of events checked */
    private Integer eventsChecked;

    /** Whether all events passed integrity checks (no tampering) */
    private Boolean isClean;

    /** Number of tampered events detected */
    private Integer tamperedCount;

    /** List of event IDs that failed integrity checks */
    private List<String> tamperedEventIds;

    /** Timestamp when verification was performed */
    private LocalDateTime verifiedAt;
}
