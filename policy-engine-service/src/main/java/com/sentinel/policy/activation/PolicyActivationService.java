package com.sentinel.policy.activation;

import com.sentinel.policy.model.PolicyRule;
import com.sentinel.policy.repository.PolicyRepository;
import com.sentinel.policy.service.PolicyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * PolicyActivationService — Loads and activates policies on application startup.
 *
 * On startup, reads all active PolicyRules from the database and pushes them
 * into the PolicyEngine's in-memory cache, ensuring the engine is ready to
 * evaluate requests from the very first inbound call.
 *
 * PRD Section 7.1: "Active rules are loaded into cache at startup."
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PolicyActivationService {

    private final PolicyService policyService;

    /**
     * Triggered once the application context is fully ready.
     * Loads all active policies from the database into the PolicyEngine cache.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("PolicyActivationService: Loading active policies into PolicyEngine cache...");
        try {
            policyService.reloadPoliciesInEngine();
            log.info("PolicyActivationService: Policy cache initialized successfully.");
        } catch (Exception e) {
            log.warn("PolicyActivationService: Could not load policies from database (DB may not be seeded yet). " +
                    "Engine will start with empty rule set. Error: {}", e.getMessage());
        }
    }
}
