package com.sentinel.shared.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.UUID;

/**
 * DTO for session summary data.
 * Matches the sessions_summary table defined in PRD Section 8.4.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SessionSummaryDTO {

    private UUID sessionId;
    private Long firstSeenNs;
    private Long lastSeenNs;
    private Integer eventCount;
    private Double maxRiskScore;
    private Integer denyCount;
    private String userId;
    private Boolean flagged;
    private List<String> endpointsAccessed;
    private String hash;
}
