package com.sentinel.policy.engine;

import com.sentinel.policy.cache.PolicyCache;
import com.sentinel.policy.scorer.BehavioralRiskScorer;
import com.sentinel.policy.scorer.signals.RiskScoreResult;
import com.sentinel.shared.context.RequestContext;
import com.sentinel.shared.decision.PolicyDecision;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Core Policy Engine Orchestrator
 * 
 * This class is the primary entry point for the Gateway. It combines:
 * 1. The Pre-Gate Firewall (Behavioral Risk Scorer)
 * 2. The ABAC (Attribute-Based Access Control) Rule Evaluator
 * 
 * ───────────────────────────────────────────────────────────────────
 * Architecture: Pre-Gate Firewall Pattern
 * ───────────────────────────────────────────────────────────────────
 * We do NOT mix risk evaluation and rule evaluation in the same loop.
 * 
 * If we did (First-Match-Wins), an attacker with a stolen JWT could
 * hit an "ALLOW" rule and bypass the risk check entirely. Furthermore,
 * parsing ABAC rules takes CPU cycles. If an attacker is spamming us,
 * we want to drop their request as cheaply and quickly as possible.
 * 
 * Therefore, Risk Scorer runs FIRST. If it flags the request, we
 * short-circuit and DENY immediately. The ABAC engine never wakes up.
 */
@Service
public class PolicyEngine {

    private final BehavioralRiskScorer riskScorer;
    private final PolicyCache policyCache;

    public PolicyEngine(BehavioralRiskScorer riskScorer, PolicyCache policyCache) {
        this.riskScorer = riskScorer;
        this.policyCache = policyCache;
    }

    /**
     * Evaluates an incoming request and decides whether to ALLOW or DENY.
     * 
     * @param ctx The context containing the user, IP, endpoint, and session
     * @return A PolicyDecision to be sent back to the Gateway
     */
    public PolicyDecision evaluateRequest(RequestContext ctx) {

        // =================================================================
        // PHASE 1: PRE-GATE FIREWALL (Behavioral Risk Check)
        // =================================================================
        RiskScoreResult riskResult = riskScorer.score(ctx);

        if (riskResult.flagged()) {
            // SHORT-CIRCUIT: Immediate DENY.
            // The user is doing something suspicious (e.g., hammering an endpoint,
            // using a token from 5 different IPs). We block them immediately.
            return PolicyDecision.deny(
                    "RISK_THRESHOLD_EXCEEDED",
                    "Request flagged by Behavioral Risk Scorer due to anomalous activity.",
                    riskResult);
        }

        // =================================================================
        // PHASE 2: ABAC EVALUATION
        // =================================================================
        // If the user passed the risk check, we now evaluate their attributes
        // (Roles, Time, Endpoint) against the active PolicyRules.

        List<PolicyRuleEvaluator> evaluators = policyCache.getActiveEvaluators();

        for (PolicyRuleEvaluator evaluator : evaluators) {
            if (evaluator.matches(ctx)) {
                // First rule that matches wins. The list is already sorted by priority.
                return PolicyDecision.from(
                        evaluator.getEffect(),
                        evaluator.getRuleId(),
                        "Policy rule matched.",
                        riskResult);
            }
        }

        // Hard Rule: Default policy decision is DENY
        return PolicyDecision.deny(
                "POLICY_NO_MATCH",
                "No matching ALLOW policy found for the given context.",
                riskResult);
    }
}