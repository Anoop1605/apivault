package com.sentinel.shared.dto;

import com.sentinel.shared.enums.Decision;
import com.sentinel.shared.enums.EventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * DTO representing a single event record.
 * Matches the events table schema defined in PRD Section 8.1.
 * Used across all services for event creation, query responses, and replay.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EventDTO {

    /** Globally unique event identifier (UUID v4). */
    private UUID eventId;

    /** Nanosecond-precision epoch timestamp. */
    private Long timestampNs;

    /** Type of event (REQUEST_RECEIVED, AUTH_FAILED, etc.). */
    private EventType eventType;

    /** Groups all events in a single client session. */
    private UUID sessionId;

    /** JWT subject claim. Null for unauthenticated requests. */
    private String userId;

    /** Roles extracted from JWT claims. */
    private List<String> roles;

    /** Full resolved API path. */
    private String endpoint;

    /** HTTP verb (GET, POST, PUT, DELETE, PATCH). */
    private String httpMethod;

    /** Originating client IP address. */
    private String sourceIp;

    /** User-Agent header value. */
    private String userAgent;

    /** ID of the ABAC rule that produced the decision. */
    private String policyRuleId;

    /** Version number of the rule at evaluation time. */
    private Integer policyRuleVersion;

    /** FK to policy_rules_history for deterministic replay. */
    private UUID policyRuleSnapshotId;

    /** Behavioral risk score (0.0–1.0). */
    private Double riskScore;

    /** Breakdown: {ip_rep, request_rate, jwt_anomaly, endpoint_freq}. */
    private Map<String, Double> riskSignals;

    /** ALLOW, DENY, or FLAG. */
    private Decision decision;

    /** Headers (sanitised), query params, body_hash, content-type. */
    private Map<String, String> requestContext;

    /** SHA-256 hex digest of request body. */
    private String bodyHash;

    /** SHA-256 of canonical serialization of all content fields. */
    private String eventHash;

    /** Running version of the Sentinel gateway. */
    private String gatewayVersion;
}
