package com.sentinel.shared.context;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Immutable value object representing the full context of an inbound request.
 * Produced by P1 (Gateway), consumed by P2 (Policy Engine, Risk Scorer).
 * FROZEN after Phase 1 — changes require full team agreement.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RequestContext {

    /** JWT subject claim. Null for unauthenticated requests. */
    private String userId;

    /** Roles extracted from JWT claims. */
    private List<String> roles;

    /** Full resolved API path, e.g., /api/payments/delete */
    private String endpoint;

    /** HTTP verb: GET, POST, PUT, DELETE, PATCH */
    private String httpMethod;

    /** Originating client IP address. */
    private String sourceIp;

    /** Session UUID — from X-Session-ID header or generated. */
    private String sessionId;

    /** Behavioral risk score (0.0–1.0). Populated by Risk Scorer. */
    private Double riskScore;

    /** User-Agent header value. */
    private String userAgent;

    /** SHA-256 hex digest of request body. Never the body itself. */
    private String bodyHash;
}
