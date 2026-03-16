package com.sentinel.shared.enums;

/**
 * Decision outcome for policy evaluation.
 * Maps to the decision column in the events table (VARCHAR(10)).
 */
public enum Decision {
    ALLOW,
    DENY,
    FLAG
}
