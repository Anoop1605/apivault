package com.sentinel.shared.context;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.List;

/**
 * Immutable snapshot of an incoming API request.
 *
 * Created by the Gateway (P1) from HTTP headers + JWT claims.
 * Consumed read-only by Policy Engine, Risk Scorer, and Event Store.
 *
 * FROZEN after Phase 1 — any field changes need full team sign-off.
 */
@Value
@Builder
public class RequestContext {

    // ── Identity fields (extracted from the JWT by P1) ──────────────

    /** The authenticated user's ID — from JWT "sub" claim */
    String userId;

    /** Roles granted to this user — from JWT "roles" claim, e.g. ["USER", "ADMIN"] */
    List<String> roles;

    /** Unique session identifier — from JWT "session_id" claim or Gateway-assigned */
    String sessionId;

    // ── Request fields (extracted from the HTTP request by P1) ──────

    /** The API endpoint being accessed, e.g. "/api/admin/users" */
    String endpoint;

    /** HTTP method: GET, POST, PUT, DELETE, etc. */
    String method;

    /** Client's source IP address — from X-Forwarded-For or direct connection */
    String sourceIp;

    /** When the request arrived at the Gateway */
    Instant requestTimestamp;

    /**
     * SHA-256 hash of the request body.
     * NEVER store the raw body — this is a hard security rule.
     * null for GET requests (no body).
     */
    String requestBodyHash;
}
