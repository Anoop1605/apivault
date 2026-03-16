package com.sentinel.shared.enums;

/**
 * All event types emitted by Sentinel gateway and policy engine.
 * Maps to the event_type column in the events table (VARCHAR(50)).
 */
public enum EventType {
    REQUEST_RECEIVED,
    AUTH_FAILED,
    RISK_FLAGGED,
    POLICY_ALLOWED,
    POLICY_DENIED,
    POLICY_NO_MATCH,
    REQUEST_FORWARDED,
    GATEWAY_ERROR
}
