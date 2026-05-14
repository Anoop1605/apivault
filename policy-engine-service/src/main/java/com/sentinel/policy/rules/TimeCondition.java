package com.sentinel.policy.rules;

import com.sentinel.shared.context.RequestContext;

import java.time.LocalTime;

/**
 * TimeCondition — Evaluates whether the current time falls within a specified window.
 *
 * Used in ABAC rules to enforce time-based access policies. For example,
 * "DENY admin access outside 09:00-18:00" restricts sensitive operations
 * to business hours.
 *
 * Supported operators:
 *  - BETWEEN: current time must be within the start-end range (format: "HH:mm-HH:mm")
 */
public class TimeCondition {

    private final String operator;
    private final LocalTime startTime;
    private final LocalTime endTime;

    public TimeCondition(String operator, String timeRange) {
        this.operator = operator;

        if ("BETWEEN".equalsIgnoreCase(operator) && timeRange.contains("-")) {
            String[] parts = timeRange.split("-");
            this.startTime = LocalTime.parse(parts[0].trim());
            this.endTime = LocalTime.parse(parts[1].trim());
        } else {
            this.startTime = LocalTime.MIN;
            this.endTime = LocalTime.MAX;
        }
    }

    /**
     * Evaluates the time condition.
     *
     * @param ctx The request context (time is evaluated at the moment of the call)
     * @return true if the condition is satisfied
     */
    public boolean evaluate(RequestContext ctx) {
        if (!"BETWEEN".equalsIgnoreCase(operator)) {
            return false;
        }

        LocalTime now = LocalTime.now();
        return now.isAfter(startTime) && now.isBefore(endTime);
    }

    public String getType() {
        return "TIME";
    }

    public String getOperator() {
        return operator;
    }
}
