package com.sentinel.policy.rules;

import com.sentinel.policy.model.Condition;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * Condition that checks if user role matches allowed roles
 */
@Data
@Builder
@AllArgsConstructor
public class RoleCondition implements Condition {
    private List<String> allowedRoles;

    @Override
    public boolean evaluate(Map<String, Object> attributes) {
        Object userRole = attributes.get("role");
        if (userRole == null) {
            return false;
        }

        String role = userRole.toString();
        return allowedRoles.contains(role);
    }

    @Override
    public String getType() {
        return "ROLE";
    }
}
