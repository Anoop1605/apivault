package com.sentinel.policy.rules;

import com.sentinel.shared.context.RequestContext;

/**
 * IPCondition — Evaluates whether the request's source IP satisfies an IP-based constraint.
 *
 * Supports both exact-match and CIDR range matching. The heavy CIDR parsing is done
 * once at construction time (same approach as ConditionEvaluator's compile step),
 * keeping the hot-path evaluation to pure bitwise arithmetic.
 *
 * Supported operators:
 *  - EQUALS: exact IP string match
 *  - IN_CIDR: source IP must be within the specified CIDR block
 */
public class IPCondition {

    private final String operator;
    private final String targetValue;

    // Pre-computed CIDR fields (computed once at construction)
    private final long networkLong;
    private final long mask;
    private final boolean isCidr;

    public IPCondition(String operator, String targetValue) {
        this.operator = operator;
        this.targetValue = targetValue;

        if ("IN_CIDR".equalsIgnoreCase(operator) && targetValue.contains("/")) {
            String[] parts = targetValue.split("/");
            int maskBits = Integer.parseInt(parts[1]);
            this.mask = -1L << (32 - maskBits);
            this.networkLong = ipToLong(parts[0]);
            this.isCidr = true;
        } else {
            this.mask = 0;
            this.networkLong = 0;
            this.isCidr = false;
        }
    }

    /**
     * Evaluates the IP condition against the given request context.
     *
     * @param ctx The request context containing the source IP
     * @return true if the condition is satisfied
     */
    public boolean evaluate(RequestContext ctx) {
        String sourceIp = ctx.getSourceIp();
        if (sourceIp == null || sourceIp.isEmpty()) {
            return false;
        }

        if (isCidr) {
            long incomingLong = ipToLong(sourceIp);
            return (incomingLong & mask) == (networkLong & mask);
        }

        // EQUALS
        return targetValue.equals(sourceIp);
    }

    /**
     * Zero-allocation IPv4 string to long converter.
     */
    private static long ipToLong(String ip) {
        long result = 0;
        int octet = 0;
        for (int i = 0; i < ip.length(); i++) {
            char c = ip.charAt(i);
            if (c == '.') {
                result = (result << 8) | octet;
                octet = 0;
            } else {
                octet = octet * 10 + (c - '0');
            }
        }
        return (result << 8) | octet;
    }

    public String getType() {
        return "IP";
    }

    public String getOperator() {
        return operator;
    }
}
