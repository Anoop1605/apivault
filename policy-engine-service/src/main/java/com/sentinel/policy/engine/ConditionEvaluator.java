package com.sentinel.policy.engine;

import com.sentinel.policy.model.PolicyRule;
import com.sentinel.shared.context.RequestContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * ConditionEvaluator — Evaluates policy conditions against request attributes.
 * Supports: ROLE, TIME, IP (CIDR), RISK conditions with multiple operators.
 * PRD Section 7.2: ABAC condition evaluation logic.
 */
@Slf4j
@Component
public class ConditionEvaluator {

    /**
     * Evaluate a single condition against request attributes
     *
     * @param condition The condition specification
     * @param requestContext Request attributes (roles, sourceIp, riskScore, etc.)
     * @return true if condition is satisfied, false otherwise
     */
    public static boolean evaluate(PolicyRule.ConditionSpec condition, RequestContext requestContext) {
        if (condition == null) {
            return true; // null condition always matches
        }

        String type = condition.getType();
        String operator = condition.getOperator();
        Object value = condition.getValue();

        try {
            switch (type.toUpperCase()) {
                case "ROLE":
                    return evaluateRoleCondition(operator, value, requestContext);
                case "TIME":
                    return evaluateTimeCondition(operator, value);
                case "IP":
                    return evaluateIPCondition(operator, value, requestContext);
                case "RISK":
                    return evaluateRiskCondition(operator, value, requestContext);
                default:
                    log.warn("Unknown condition type: {}", type);
                    return false;
            }
        } catch (Exception e) {
            log.error("Error evaluating condition: type={}, operator={}, error={}", type, operator, e.getMessage());
            return false;
        }
    }

    /**
     * Evaluate ROLE condition
     * Operators: EQUALS, CONTAINS
     */
    private static boolean evaluateRoleCondition(String operator, Object value, RequestContext requestContext) {
        if (requestContext == null || requestContext.getRoles() == null) {
            return false;
        }

        List<String> userRoles = requestContext.getRoles();

        if ("EQUALS".equalsIgnoreCase(operator)) {
            return userRoles.contains(value.toString());
        } else if ("CONTAINS".equalsIgnoreCase(operator)) {
            String searchRole = value.toString();
            return userRoles.stream().anyMatch(r -> r.contains(searchRole));
        }

        return false;
    }

    /**
     * Evaluate TIME condition
     * Operators: BETWEEN
     * Format: "HH:MM-HH:MM" (24-hour format)
     */
    private static boolean evaluateTimeCondition(String operator, Object value) {
        if ("BETWEEN".equalsIgnoreCase(operator)) {
            LocalTime now = LocalTime.now();
            String[] parts = value.toString().split("-");
            if (parts.length == 2) {
                LocalTime start = LocalTime.parse(parts[0]);
                LocalTime end = LocalTime.parse(parts[1]);
                return now.isAfter(start) && now.isBefore(end);
            }
        }
        return false;
    }

    /**
     * Evaluate IP condition
     * Operators: IN_CIDR, EQUALS
     */
    private static boolean evaluateIPCondition(String operator, Object value, RequestContext requestContext) {
        if (requestContext == null || requestContext.getSourceIp() == null) {
            return false;
        }

        String clientIp = requestContext.getSourceIp();

        if ("IN_CIDR".equalsIgnoreCase(operator)) {
            return isIpInCidr(clientIp, value.toString());
        } else if ("EQUALS".equalsIgnoreCase(operator)) {
            return clientIp.equals(value.toString());
        }

        return false;
    }

    /**
     * Evaluate RISK condition
     * Operators: LESS_THAN, GREATER_THAN, EQUALS
     */
    private static boolean evaluateRiskCondition(String operator, Object value, RequestContext requestContext) {
        if (requestContext == null || requestContext.getRiskScore() == null) {
            return false;
        }

        double riskScore = requestContext.getRiskScore();
        double threshold = Double.parseDouble(value.toString());

        switch (operator.toUpperCase()) {
            case "LESS_THAN":
                return riskScore < threshold;
            case "GREATER_THAN":
                return riskScore > threshold;
            case "EQUALS":
                return Math.abs(riskScore - threshold) < 0.001;
            default:
                return false;
        }
    }

    /**
     * Simple CIDR validation - checks if IP is within CIDR range
     * Simplified implementation for IPv4
     */
    private static boolean isIpInCidr(String ip, String cidr) {
        try {
            String[] parts = cidr.split("/");
            if (parts.length != 2) {
                return false;
            }

            String networkIp = parts[0];
            int maskBits = Integer.parseInt(parts[1]);

            long ipLong = ipToLong(ip);
            long networkLong = ipToLong(networkIp);

            long mask = -1 << (32 - maskBits);
            return (ipLong & mask) == (networkLong & mask);
        } catch (Exception e) {
            log.debug("Error validating CIDR: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Convert IPv4 address string to long value
     */
    private static long ipToLong(String ip) {
        String[] parts = ip.split("\\.");
        long result = 0;
        for (String part : parts) {
            result = result * 256 + Integer.parseInt(part);
        }
        return result;
    }
}
