package com.sentinel.policy.config;

import com.sentinel.policy.model.PolicyRule;
import com.sentinel.policy.service.PolicyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.UUID;

/**
 * PolicySeeder — Seeds the database with default policy rules on startup.
 * Ensures the demo environment has a baseline set of rules to evaluate.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class PolicySeeder implements CommandLineRunner {

    private final PolicyService policyService;

    @Override
    public void run(String... args) {
        if (policyService.getAllPolicies().isEmpty()) {
            log.info("Seeding default policy rules...");

            // 1. Block SQL Injection
            PolicyRule sqlRule = PolicyRule.builder()
                    .id(UUID.randomUUID())
                    .ruleId("RULE-SQL-01")
                    .name("Block SQL Injection")
                    .description("Deny any request containing common SQL injection keywords in the query string.")
                    .priority(100)
                    .active(true)
                    .version(1)
                    .effect("DENY")
                    .conditions(List.of(
                            new PolicyRule.ConditionSpec("QUERY", "CONTAINS", "SELECT"),
                            new PolicyRule.ConditionSpec("QUERY", "CONTAINS", "DROP"),
                            new PolicyRule.ConditionSpec("QUERY", "CONTAINS", "OR 1=1")
                    ))
                    .build();

            // 2. High Risk Behavioral Block
            PolicyRule riskRule = PolicyRule.builder()
                    .id(UUID.randomUUID())
                    .ruleId("RULE-RISK-01")
                    .name("Strict Risk Enforcement")
                    .description("Deny any request with a behavioral risk score above 0.8.")
                    .priority(90)
                    .active(true)
                    .version(1)
                    .effect("DENY")
                    .conditions(List.of(
                            new PolicyRule.ConditionSpec("RISK", "GREATER_THAN", 0.8)
                    ))
                    .build();

            // 3. Admin Access Only
            PolicyRule adminRule = PolicyRule.builder()
                    .id(UUID.randomUUID())
                    .ruleId("RULE-ADMIN-01")
                    .name("Admin Only Endpoints")
                    .description("Require ADMIN role for any path starting with /admin.")
                    .priority(80)
                    .active(true)
                    .version(1)
                    .effect("DENY")
                    .conditions(List.of(
                            new PolicyRule.ConditionSpec("ENDPOINT", "STARTS_WITH", "/admin"),
                            new PolicyRule.ConditionSpec("ROLE", "NOT_EQUALS", "ADMIN")
                    ))
                    .build();
            
            // 4. Default Allow (Zero Trust Fallback is DENY, but we can have an explicit allow rule for common paths)
            PolicyRule allowRule = PolicyRule.builder()
                    .id(UUID.randomUUID())
                    .ruleId("RULE-ALLOW-01")
                    .name("Allow Public APIs")
                    .description("Allow access to public API endpoints.")
                    .priority(10)
                    .active(true)
                    .version(1)
                    .effect("ALLOW")
                    .conditions(List.of(
                            new PolicyRule.ConditionSpec("ENDPOINT", "STARTS_WITH", "/api/public")
                    ))
                    .build();

            policyService.createPolicy(sqlRule);
            policyService.createPolicy(riskRule);
            policyService.createPolicy(adminRule);
            policyService.createPolicy(allowRule);

            log.info("Default policies seeded successfully.");
        } else {
            log.info("Policies already exist. Skipping seed.");
            // Still reload to ensure engine cache is warm
            policyService.reloadPoliciesInEngine();
        }
    }
}
