package com.sentinel.policy.engine;

import com.sentinel.shared.context.RequestContext;
import com.sentinel.shared.enums.Decision;

import java.util.UUID;
import java.util.function.Predicate;

/**
 * A pre-compiled, "ready-to-run" version of a PolicyRule.
 * <p>
 * It encapsulates the logic of evaluating all its conditions (respecting the
 * ALL_OF/ANY_OF combiner) into a single, efficient Predicate. This avoids
 * re-interpreting the rule structure on every request, which is critical for
 * meeting the p99 latency target.
 *
 * @param ruleId    The unique identifier of the original rule, for logging and
 *                  forensics.
 * @param effect    The decision (ALLOW/DENY) to be rendered if this rule
 *                  matches.
 * @param priority  The execution priority of the rule.
 * @param predicate The compiled predicate that returns true if the rule's
 *                  conditions are met.
 */
public record PolicyRuleEvaluator(
        String ruleId,
        Decision effect,
        int ruleVersion,
        UUID snapshotId,
        int priority,
        Predicate<RequestContext> predicate) {
    /**
     * Executes the pre-compiled predicate against the given request context.
     *
     * @param context The incoming request's context.
     * @return {@code true} if the request satisfies the rule's conditions,
     *         {@code false} otherwise.
     */
    public boolean matches(RequestContext context) {
        // The predicate is the "mise en place" — all the complex logic is already baked
        // in.
        // This call is extremely fast.
        return predicate.test(context);
    }

    // Java 16+ Records automatically generate getters (ruleId(), effect(), etc.),
    // which we will use in the PolicyEngine. For clarity, I'm showing them here.
    public String getRuleId() {
        return ruleId;
    }

    public Decision getEffect() {
        return effect;
    }

    public int getRuleVersion() {
        return ruleVersion;
    }

    public UUID getSnapshotId() {
        return snapshotId;
    }
}
