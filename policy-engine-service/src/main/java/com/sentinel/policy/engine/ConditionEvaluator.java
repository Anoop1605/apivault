package com.sentinel.policy.engine;

import com.sentinel.policy.model.PolicyRule;
import com.sentinel.shared.context.RequestContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * ConditionEvaluator — Evaluates policy conditions against request attributes.
 * Refactored to act as a Compiler: parses conditions ONCE at startup/cache
 * refresh,
 * returning a highly optimized Predicate for the hot path.
 */
@Slf4j
@Component
public class ConditionEvaluator {

    /**
     * Pre-compiles a condition specification into an executable Predicate.
     * All heavy parsing (regex, split, parsing numbers) happens HERE, exactly once.
     *
     * @param condition The condition specification
     * @return A fast, zero-allocation Predicate ready for the hot path.
     */
    public static Predicate<RequestContext> compile(PolicyRule.ConditionSpec condition) {
        if (condition == null) {
            return ctx -> true; // null condition always matches
        }

        String type = condition.getType();
        String operator = condition.getOperator();
        Object value = condition.getValue();

        try {
            switch (type.toUpperCase()) {
                case "ROLE":
                    return compileRoleCondition(operator, value);
                case "TIME":
                    return compileTimeCondition(operator, value);
                case "IP":
                    return compileIPCondition(operator, value);
                case "RISK":
                    return compileRiskCondition(operator, value);
                default:
                    log.warn("Unknown condition type: {}", type);
                    return ctx -> false;
            }
        } catch (Exception e) {
            log.error("Error compiling condition: type={}, operator={}, error={}", type, operator, e.getMessage());
            return ctx -> false; // Failsafe deny
        }
    }

    /**
     * Compiles ROLE condition.
     */
    private static Predicate<RequestContext> compileRoleCondition(String operator, Object value) {
        String targetRole = value.toString();

        if ("EQUALS".equalsIgnoreCase(operator)) {
            return ctx -> ctx.getRoles() != null && ctx.getRoles().contains(targetRole);
        } else if ("CONTAINS".equalsIgnoreCase(operator)) {
            return ctx -> {
                if (ctx.getRoles() == null)
                    return false;
                // Avoid stream overhead in hot path
                for (String r : ctx.getRoles()) {
                    if (r.contains(targetRole))
                        return true;
                }
                return false;
            };
        }
        return ctx -> false;
    }

    /**
     * Compiles TIME condition. Pre-parses the LocalTime boundaries.
     */
    private static Predicate<RequestContext> compileTimeCondition(String operator, Object value) {
        if ("BETWEEN".equalsIgnoreCase(operator)) {
            String[] parts = value.toString().split("-");
            if (parts.length == 2) {
                // Parsed ONCE during compilation
                LocalTime start = LocalTime.parse(parts[0]);
                LocalTime end = LocalTime.parse(parts[1]);

                return ctx -> {
                    LocalTime now = LocalTime.now();
                    return now.isAfter(start) && now.isBefore(end);
                };
            }
        }
        return ctx -> false;
    }

    /**
     * Compiles IP condition. Pre-calculates the network long and bitmask!
     */
    private static Predicate<RequestContext> compileIPCondition(String operator, Object value) {
        String targetIp = value.toString();

        if ("IN_CIDR".equalsIgnoreCase(operator)) {
            String[] parts = targetIp.split("/");
            int maskBits = Integer.parseInt(parts[1]);

            // Pre-calculate mask and network long once
            long mask = -1 << (32 - maskBits);
            long networkLong = fastIpToLong(parts[0]);

            // The hot path is purely bitwise arithmetic
            return ctx -> {
                if (ctx.getSourceIp() == null)
                    return false;
                long incomingIpLong = fastIpToLong(ctx.getSourceIp());
                return (incomingIpLong & mask) == (networkLong & mask);
            };

        } else if ("EQUALS".equalsIgnoreCase(operator)) {
            return ctx -> targetIp.equals(ctx.getSourceIp());
        }
        return ctx -> false;
    }

    /**
     * Compiles RISK condition. Pre-parses the double value.
     */
    private static Predicate<RequestContext> compileRiskCondition(String operator, Object value) {
        // Parsed ONCE
        double threshold = Double.parseDouble(value.toString());

        switch (operator.toUpperCase()) {
            case "LESS_THAN":
                return ctx -> ctx.getRiskScore() != null && ctx.getRiskScore() < threshold;
            case "GREATER_THAN":
                return ctx -> ctx.getRiskScore() != null && ctx.getRiskScore() > threshold;
            case "EQUALS":
                return ctx -> ctx.getRiskScore() != null && Math.abs(ctx.getRiskScore() - threshold) < 0.001;
            default:
                return ctx -> false;
        }
    }

    /**
     * Zero-allocation, high-performance IPv4 string to long converter.
     * Replaces the old String.split("\\.") which creates string arrays on every
     * request.
     */
    private static long fastIpToLong(String ip) {
        long result = 0;
        int octet = 0;
        for (int i = 0; i < ip.length(); i++) {
            char c = ip.charAt(i);
            if (c == '.') {
                result = (result << 8) | octet;
                octet = 0;
            } else {
                // ASCII math: '0' is 48. '5' (53) - '0' (48) = integer 5.
                octet = octet * 10 + (c - '0');
            }
        }
        return (result << 8) | octet;
    }
}
