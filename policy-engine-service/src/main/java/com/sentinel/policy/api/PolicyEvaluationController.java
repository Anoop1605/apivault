package com.sentinel.policy.api;

import com.sentinel.policy.engine.PolicyEngine;
import com.sentinel.policy.model.PolicyDecision;
import com.sentinel.policy.scorer.BehavioralRiskScorer;
import com.sentinel.shared.context.RequestContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * PolicyEvaluationController — REST endpoint for real-time ABAC policy evaluation.
 * Called by the Gateway's ZeroTrustOrchestrationFilter on every inbound request.
 */
@Slf4j
@RestController
@RequestMapping("/policy")
@RequiredArgsConstructor
public class PolicyEvaluationController {

    private final PolicyEngine policyEngine;
    private final BehavioralRiskScorer behavioralRiskScorer;

    /**
     * POST /policy/evaluate
     *
     * Takes a RequestContext from the Gateway, evaluates it against all active ABAC
     * policies and the behavioral risk scorer, and returns a PolicyDecision.
     */
    @PostMapping("/evaluate")
    public ResponseEntity<PolicyDecision> evaluate(@RequestBody RequestContext context) {
        long startNs = System.nanoTime();

        // 1. Compute behavioral risk score from all registered signals
        BehavioralRiskScorer.RiskScoreResult riskResult = behavioralRiskScorer.evaluateRisk(context);
        double riskScore = riskResult.finalScore();

        // 2. Inject computed risk score into context so RISK conditions can match
        context.setRiskScore(riskScore);

        // 3. Evaluate ABAC policy rules (first-match wins, per PRD)
        PolicyDecision decision = policyEngine.evaluate(context);

        long totalTimeNs = System.nanoTime() - startNs;

        log.info("Policy evaluated: decision={}, ruleId={}, riskScore={}, totalTimeNs={}, endpoint={}, userId={}",
                decision.getDecision(), decision.getRuleId(), riskScore, totalTimeNs, context.getEndpoint(), context.getUserId());

        // Update evaluation time and risk score to be returned to Gateway
        decision.setEvaluationTimeNs(totalTimeNs);
        decision.setRiskScore(riskScore);

        return ResponseEntity.ok(decision);
    }

    /**
     * POST /policy/evaluate-snapshot
     *
     * Evaluates a RequestContext against a SPECIFIC PolicySnapshot (list of rules).
     * Used for What-If forensic simulations.
     */
    @PostMapping("/evaluate-snapshot")
    public ResponseEntity<PolicyDecision> evaluateSnapshot(@RequestBody Map<String, Object> body) {
        // Map is used to handle the composite object (context + snapshot)
        // In a real system, we'd use a proper DTO.
        
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        RequestContext context = mapper.convertValue(body.get("context"), RequestContext.class);
        com.sentinel.shared.dto.PolicySnapshot snapshot = mapper.convertValue(body.get("snapshot"), com.sentinel.shared.dto.PolicySnapshot.class);
        
        PolicyDecision decision = policyEngine.evaluateSnapshot(context, snapshot);
        return ResponseEntity.ok(decision);
    }
}
