package com.sentinel.policy.scorer.signals;

import com.sentinel.shared.context.RequestContext;

public interface RiskSignal {

    /**  The main job — analyse the request, return 0.0 (safe) to 1.0 (max risk) */
    double evaluate(RequestContext ctx);

    /** Unique identifier — used as the key in RiskScoreResult.signalBreakdown */
    String name();

    /** How much this signal contributes to the final score. Default = 25% */
    default double weight() {
        return 0.25;
    }
}
