package com.sentinel.shared.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * RequestContext — Contains attributes extracted from HTTP request for policy evaluation.
 * Used by ConditionEvaluator and PolicyEngine for ABAC (Attribute-Based Access Control).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestContext {

    /** JWT subject claim - user identifier */
    private String userId;

    /** Roles extracted from JWT claims */
    private List<String> roles;

    /** Client source IP address */
    private String sourceIp;

    /** Full API endpoint path */
    private String endpoint;

    /** HTTP method (GET, POST, PUT, DELETE, PATCH) */
    private String httpMethod;

    /** User-Agent header */
    private String userAgent;

    /** Behavioral risk score (0.0 - 1.0) */
    private Double riskScore;

    /** Risk signal breakdown: {ip_rep, request_rate, jwt_anomaly, endpoint_freq} */
    private Map<String, Double> riskSignals;

    /** Additional request attributes for custom conditions */
    private Map<String, Object> additionalAttributes;
}
