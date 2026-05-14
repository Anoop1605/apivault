package com.sentinel.policy.rules;

import com.sentinel.shared.context.RequestContext;

import java.util.List;
import java.util.function.Predicate;

/**
 * RoleCondition — Evaluates whether the request's JWT roles satisfy a role-based constraint.
 *
 * Delegates to the compiled predicate system in ConditionEvaluator. This class provides
 * a named, self-documenting representation of the ROLE condition type for use in
 * policy rule definitions and audit logs.
 *
 * Supported operators:
 *  - EQUALS: user must have exactly this role
 *  - CONTAINS: any of the user's roles must contain the target string
 */
public class RoleCondition {

    private final String operator;
    private final String targetRole;

    public RoleCondition(String operator, String targetRole) {
        this.operator = operator;
        this.targetRole = targetRole;
    }

    /**
     * Evaluates the role condition against the given request context.
     *
     * @param ctx The request context containing user roles
     * @return true if the condition is satisfied
     */
    public boolean evaluate(RequestContext ctx) {
        List<String> roles = ctx.getRoles();
        if (roles == null || roles.isEmpty()) {
            return false;
        }

        return switch (operator.toUpperCase()) {
            case "EQUALS" -> roles.contains(targetRole);
            case "CONTAINS" -> roles.stream().anyMatch(r -> r.contains(targetRole));
            default -> false;
        };
    }

    public String getType() {
        return "ROLE";
    }

    public String getOperator() {
        return operator;
    }

    public String getTargetRole() {
        return targetRole;
    }
}
