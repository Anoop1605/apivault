package com.sentinel.shared.enums;

/**
 * Decision outcome for policy evaluation.
 * Matches to the decision column in the events table (VARCHAR(10)).
 */
public enum Decision {
    ALLOW,
    DENY,
    FLAG,
    BLOCK, // Strong deny with blocking action
    REVIEW, // Flagged for manual review
    NONE, // No decision made yet (e.g., REQUEST_RECEIVED)
    NOT_APPLICABLE // Decision not relevant (e.g., GATEWAY_ERROR)
}
