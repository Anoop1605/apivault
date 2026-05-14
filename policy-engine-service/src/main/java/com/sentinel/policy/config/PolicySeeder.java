package com.sentinel.policy.config;

import com.sentinel.policy.model.PolicyRule;
import com.sentinel.policy.service.PolicyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class PolicySeeder implements CommandLineRunner {

    private final PolicyService policyService;

    @Override
    public void run(String... args) {
        if (!policyService.getAllPolicies().isEmpty()) {
            policyService.reloadPoliciesInEngine();
            return;
        }

        log.info("Seeding demo ABAC policies");
        policyService.createPolicy(rule(
                "RULE-RISK-STRICT-01",
                "Strict high-risk denial",
                100,
                "DENY",
                List.of(new PolicyRule.ConditionSpec("RISK", "GREATER_THAN", 0.85))));

        policyService.createPolicy(rule(
                "RULE-ALLOW-USER-PROFILE-01",
                "Allow user profile reads",
                30,
                "ALLOW",
                List.of(
                        new PolicyRule.ConditionSpec("ENDPOINT", "EQUALS", "/api/users/profile"),
                        new PolicyRule.ConditionSpec("METHOD", "EQUALS", "GET"))));

        policyService.createPolicy(rule(
                "RULE-ALLOW-ADMIN-READ-01",
                "Allow admin read endpoints for admin role",
                40,
                "ALLOW",
                List.of(
                        new PolicyRule.ConditionSpec("ROLE", "EQUALS", "admin"),
                        new PolicyRule.ConditionSpec("ENDPOINT", "STARTS_WITH", "/api/admin"),
                        new PolicyRule.ConditionSpec("METHOD", "EQUALS", "GET"))));

        policyService.createPolicy(rule(
                "RULE-ALLOW-PAYMENT-DELETE-01",
                "Allow finance-admin payment delete during business hours",
                50,
                "ALLOW",
                List.of(
                        new PolicyRule.ConditionSpec("ROLE", "EQUALS", "finance-admin"),
                        new PolicyRule.ConditionSpec("ENDPOINT", "EQUALS", "/api/payments/delete"),
                        new PolicyRule.ConditionSpec("METHOD", "EQUALS", "DELETE"),
                        new PolicyRule.ConditionSpec("TIME", "BETWEEN", "09:00-17:00"),
                        new PolicyRule.ConditionSpec("RISK", "LESS_THAN", 0.70))));

        policyService.reloadPoliciesInEngine();
    }

    private PolicyRule rule(String ruleId, String name, int priority, String effect,
            List<PolicyRule.ConditionSpec> conditions) {
        LocalDateTime now = LocalDateTime.now();
        return PolicyRule.builder()
                .id(UUID.randomUUID())
                .ruleId(ruleId)
                .name(name)
                .description(name)
                .priority(priority)
                .active(true)
                .version(1)
                .effect(effect)
                .conditions(conditions)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }
}
