package com.sentinel.shared.context;

import java.time.Instant;
import java.util.List;

/**
 * Immutable snapshot of an incoming API request.
 *
 * Created by the Gateway (P1) from HTTP headers + JWT claims.
 * Consumed read-only by Policy Engine, Risk Scorer, and Event Store.
 *
 * FROZEN after Phase 1 — any field changes need full team sign-off.
 *
 * NOTE: Previously used Lombok @Value/@Builder, but Lombok is incompatible
 * with JDK 25. Replaced with hand-written immutable class + builder.
 * Functionally identical — same getters, same builder API.
 */
public final class RequestContext {

    // ── Identity fields (extracted from the JWT by P1) ──────────────

    /** The authenticated user's ID — from JWT "sub" claim */
    private final String userId;

    /** Roles granted to this user — from JWT "roles" claim, e.g. ["USER", "ADMIN"] */
    private final List<String> roles;

    /** Unique session identifier — from JWT "session_id" claim or Gateway-assigned */
    private final String sessionId;

    // ── Request fields (extracted from the HTTP request by P1) ──────

    /** The API endpoint being accessed, e.g. "/api/admin/users" */
    private final String endpoint;

    /** HTTP method: GET, POST, PUT, DELETE, etc. */
    private final String method;

    /** Client's source IP address — from X-Forwarded-For or direct connection */
    private final String sourceIp;

    /** When the request arrived at the Gateway */
    private final Instant requestTimestamp;

    /**
     * SHA-256 hash of the request body.
     * NEVER store the raw body — this is a hard security rule.
     * null for GET requests (no body).
     */
    private final String requestBodyHash;

    // ── Private constructor (use Builder) ────────────────────────────

    private RequestContext(Builder builder) {
        this.userId           = builder.userId;
        this.roles            = builder.roles;
        this.sessionId        = builder.sessionId;
        this.endpoint         = builder.endpoint;
        this.method           = builder.method;
        this.sourceIp         = builder.sourceIp;
        this.requestTimestamp  = builder.requestTimestamp;
        this.requestBodyHash  = builder.requestBodyHash;
    }

    // ── Getters (read-only — no setters, this class is immutable) ───

    public String getUserId()             { return userId; }
    public List<String> getRoles()        { return roles; }
    public String getSessionId()          { return sessionId; }
    public String getEndpoint()           { return endpoint; }
    public String getMethod()             { return method; }
    public String getSourceIp()           { return sourceIp; }
    public Instant getRequestTimestamp()   { return requestTimestamp; }
    public String getRequestBodyHash()    { return requestBodyHash; }

    // ── Builder (same API as Lombok @Builder) ───────────────────────

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String userId;
        private List<String> roles;
        private String sessionId;
        private String endpoint;
        private String method;
        private String sourceIp;
        private Instant requestTimestamp;
        private String requestBodyHash;

        public Builder userId(String userId)                     { this.userId = userId; return this; }
        public Builder roles(List<String> roles)                 { this.roles = roles; return this; }
        public Builder sessionId(String sessionId)               { this.sessionId = sessionId; return this; }
        public Builder endpoint(String endpoint)                 { this.endpoint = endpoint; return this; }
        public Builder method(String method)                     { this.method = method; return this; }
        public Builder sourceIp(String sourceIp)                 { this.sourceIp = sourceIp; return this; }
        public Builder requestTimestamp(Instant requestTimestamp) { this.requestTimestamp = requestTimestamp; return this; }
        public Builder requestBodyHash(String requestBodyHash)   { this.requestBodyHash = requestBodyHash; return this; }

        public RequestContext build() {
            return new RequestContext(this);
        }
    }

    // ── toString (for debugging/logging) ─────────────────────────────

    @Override
    public String toString() {
        return "RequestContext{" +
                "userId='" + userId + '\'' +
                ", roles=" + roles +
                ", sessionId='" + sessionId + '\'' +
                ", endpoint='" + endpoint + '\'' +
                ", method='" + method + '\'' +
                ", sourceIp='" + sourceIp + '\'' +
                ", requestTimestamp=" + requestTimestamp +
                ", requestBodyHash='" + requestBodyHash + '\'' +
                '}';
    }
}
