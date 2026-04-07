package com.sentinel.policy.scorer;

import com.sentinel.shared.dto.RequestContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * BehavioralRiskScorer — Calculates composite behavioral risk score (0.0–1.0).
 * Implements simplified scoring based on four signals:
 * - IP Reputation (30%): blacklist check
 * - Request Rate (25%): rate anomaly detection
 * - JWT Anomaly (25%): unusual JWT claims detection
 * - Endpoint Frequency (20%): endpoint access frequency anomaly
 *
 * PRD Section 7.1: Behavioral Risk Scoring
 */
@Slf4j
@Service
public class BehavioralRiskScorer {

    // Simplified IP blacklist (in production, would use external threat intel)
    private static final Set<String> IP_BLACKLIST = Set.of(
            "192.0.2.1", "198.51.100.1", "203.0.113.1" // Example IPs
    );

    // In-memory rate tracking (simplified - in production would use Redis)
    private static final Map<String, RequestRateInfo> REQUEST_RATE_MAP = new HashMap<>();
    private static final long RATE_WINDOW_MS = 60_000; // 1 minute
    private static final int RATE_THRESHOLD = 100; // requests per minute

    /**
     * Calculate composite behavioral risk score (0.0 = safe, 1.0 = dangerous)
     *
     * @param requestContext Request attributes
     * @return Risk score between 0.0 and 1.0
     */
    public double computeRiskScore(RequestContext requestContext) {
        if (requestContext == null) {
            return 0.0;
        }

        double riskScore = 0.0;

        // Signal 1: IP Reputation (30% weight)
        double ipRepScore = computeIpReputationSignal(requestContext.getSourceIp());
        riskScore += ipRepScore * 0.30;
        log.debug("IP reputation score: {}", ipRepScore);

        // Signal 2: Request Rate Anomaly (25% weight)
        double requestRateScore = computeRequestRateSignal(requestContext.getSourceIp());
        riskScore += requestRateScore * 0.25;
        log.debug("Request rate score: {}", requestRateScore);

        // Signal 3: JWT Anomaly (25% weight)
        double jwtAnomalyScore = computeJwtAnomalySignal(requestContext);
        riskScore += jwtAnomalyScore * 0.25;
        log.debug("JWT anomaly score: {}", jwtAnomalyScore);

        // Signal 4: Endpoint Frequency (20% weight)
        double endpointFreqScore = computeEndpointFrequencySignal(requestContext);
        riskScore += endpointFreqScore * 0.20;
        log.debug("Endpoint frequency score: {}", endpointFreqScore);

        // Normalize to [0.0, 1.0]
        riskScore = Math.min(1.0, Math.max(0.0, riskScore));

        log.info("Calculated risk score for IP={}: {}", requestContext.getSourceIp(), riskScore);
        return riskScore;
    }

    /**
     * Compute IP reputation signal
     * Simplified: Check against blacklist (0.5 if blacklisted, 0.1 if clean)
     */
    private double computeIpReputationSignal(String sourceIp) {
        if (sourceIp == null) {
            return 0.0;
        }

        if (IP_BLACKLIST.contains(sourceIp)) {
            log.warn("IP in blacklist: {}", sourceIp);
            return 0.5;
        }

        return 0.1; // Default low risk for clean IPs
    }

    /**
     * Compute request rate anomaly signal
     * Simplified: Check if requests from IP exceed 100 req/min
     */
    private double computeRequestRateSignal(String sourceIp) {
        if (sourceIp == null) {
            return 0.0;
        }

        long now = System.currentTimeMillis();

        // Get or create request rate info
        REQUEST_RATE_MAP.putIfAbsent(sourceIp, new RequestRateInfo());
        RequestRateInfo rateInfo = REQUEST_RATE_MAP.get(sourceIp);

        // Clean old entries outside the window
        if (now - rateInfo.lastTimestamp > RATE_WINDOW_MS) {
            rateInfo.requestCount = 0;
            rateInfo.lastTimestamp = now;
        }

        rateInfo.requestCount++;
        rateInfo.lastTimestamp = now;

        // If exceeds threshold, return high risk
        if (rateInfo.requestCount > RATE_THRESHOLD) {
            log.warn("High request rate for IP {}: {} requests/min", sourceIp, rateInfo.requestCount);
            return 0.7;
        }

        return 0.1; // Low risk for normal rate
    }

    /**
     * Compute JWT anomaly signal
     * Simplified: Check if roles list is empty (anomaly) or has unusual claims
     */
    private double computeJwtAnomalySignal(RequestContext requestContext) {
        if (requestContext == null || requestContext.getRoles() == null) {
            return 0.3; // Anomaly if no roles
        }

        // Check for unusual number of roles (more than 10 is suspicious)
        if (requestContext.getRoles().size() > 10) {
            log.warn("Unusual number of roles: {}", requestContext.getRoles().size());
            return 0.5;
        }

        // Check for known suspicious role names
        boolean hasSuspiciousRole = requestContext.getRoles().stream()
                .anyMatch(role -> role.contains("admin") && !role.equalsIgnoreCase("user.admin"));
        if (hasSuspiciousRole) {
            return 0.4;
        }

        return 0.1; // Normal JWT
    }

    /**
     * Compute endpoint frequency anomaly signal
     * Simplified: Check if user accesses same endpoint too frequently
     */
    private double computeEndpointFrequencySignal(RequestContext requestContext) {
        if (requestContext == null || requestContext.getUserId() == null || requestContext.getEndpoint() == null) {
            return 0.1;
        }

        // In a real system, would query event store for access frequency
        // Simplified: assume 0.1 (low risk) by default
        // A production system would check if endpoint accessed >5 times in 1 minute

        return 0.1;
    }

    /**
     * Simple helper class to track request rates per IP
     */
    private static class RequestRateInfo {
        int requestCount = 0;
        long lastTimestamp = System.currentTimeMillis();
    }
}

