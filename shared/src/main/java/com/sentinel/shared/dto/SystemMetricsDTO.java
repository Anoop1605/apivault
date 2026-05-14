package com.sentinel.shared.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * SystemMetricsDTO — Aggregated telemetry for the dashboard.
 * Used to provide real-time counts of security events, blocks, and performance.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemMetricsDTO {
    private long totalEvents;
    private long blockedThreats;
    private long activeSessions;
    private double avgResponseTimeMs;
    private String systemThreatLevel; // LOW, MEDIUM, HIGH, CRITICAL
}
