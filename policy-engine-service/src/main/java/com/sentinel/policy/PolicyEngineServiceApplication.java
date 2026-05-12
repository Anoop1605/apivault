package com.sentinel.policy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.sentinel.policy.service.PolicyService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class PolicyEngineServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(PolicyEngineServiceApplication.class, args);
    }

    @Bean
    public CommandLineRunner initPolicies(PolicyService policyService) {
        return args -> {
            policyService.reloadPoliciesInEngine();
        };
    }
}
