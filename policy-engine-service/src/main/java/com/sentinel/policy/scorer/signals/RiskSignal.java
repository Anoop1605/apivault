package com.sentinel.policy.scorer.signals;

import com.sentinel.shared.context.RequestContext;

/**
 * Contract for all behavioral risk heuristics.
 * By implementing this interface, new signals can be dynamically added
 * without modifying the core BehavioralRiskScorer.
 */
public interface RiskSignal {

    /**
     * Evaluates the request context and returns a risk score.
     *
     * @param ctx the incoming request context
     * @return a risk score between 0.0 (normal) and 1.0 (high risk)
     */
    double evaluate(RequestContext ctx);

    /**
     * @return the unique identifier for this signal (e.g., "jwt_anomaly")
     */
    String name();

    /**
     * @return the weight this signal carries in the final weighted average
     */
    double weight();
}