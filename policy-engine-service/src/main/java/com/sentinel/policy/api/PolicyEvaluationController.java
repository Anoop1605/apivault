package com.sentinel.policy.api;

import com.sentinel.policy.engine.PolicyEngine;
import com.sentinel.policy.model.PolicyDecision;
import com.sentinel.shared.context.RequestContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * PolicyEvaluationController — REST API for real-time policy evaluation.
 * PRD Section 7.2: High-performance evaluation endpoint for Gateway.
 */
@Slf4j
@RestController
@RequestMapping("/api/policy")
@RequiredArgsConstructor
public class PolicyEvaluationController {

    private final PolicyEngine policyEngine;

    /**
     * POST /api/policy/evaluate — Evaluate a request context against active policies.
     */
    @PostMapping("/evaluate")
    public ResponseEntity<PolicyDecision> evaluate(@RequestBody RequestContext context) {
        log.debug("Received evaluation request for user: {}, endpoint: {}", context.getUserId(), context.getEndpoint());
        
        // This is a REAL evaluation call using the cached rules
        PolicyDecision decision = policyEngine.evaluateToDecision(context);
        
        return ResponseEntity.ok(decision);
    }
}
