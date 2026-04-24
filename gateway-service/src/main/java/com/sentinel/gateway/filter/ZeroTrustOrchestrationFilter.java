package com.sentinel.gateway.filter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.concurrent.TimeUnit;
import java.util.List;

/**
 * Orchestrates the Zero-Trust execution of the Risk Scorer and Policy Engine.
 * P1 owns the orchestration and metric emission (FR-DB-07).
 * P2 owns the actual engine implementations (currently stubs).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ZeroTrustOrchestrationFilter implements GlobalFilter, Ordered {

    private final MeterRegistry meterRegistry;

    @Override
    public int getOrder() {
        // Must run after Auth Filter but before Event Emission
        return -60;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        
        // Skip for actuator
        if (path.startsWith("/actuator") || path.startsWith("/health") || path.startsWith("/metrics")) {
            return chain.filter(exchange);
        }

        // 1. BEHAVIORAL RISK EVALUATION STUB
        double riskScore = evaluateRiskScoreStub(exchange);
        exchange.getAttributes().put("riskScore", riskScore);

        if (riskScore >= 0.7) {
            log.warn("High risk score detected ({}). Flagging request.", riskScore);
            meterRegistry.counter("risk.flagged.count").increment();
            // PRD allows either FLAG or DENY depending on strictness. For now, we continue but flag.
        }

        // 2. ABAC POLICY EVALUATION STUB
        long policyStartTime = System.currentTimeMillis();
        boolean policyDecisionAllow = evaluatePolicyDecisionStub(exchange);
        long policyDuration = System.currentTimeMillis() - policyStartTime;

        // Emit mandatory metric: policy.eval.time.p99
        Timer.builder("policy.eval.time")
                .publishPercentiles(0.99)
                .register(meterRegistry)
                .record(policyDuration, TimeUnit.MILLISECONDS);

        if (!policyDecisionAllow) {
            log.warn("Zero-Trust Policy Engine denied request for path: {}", path);
            return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "Policy Engine Denied Access"));
        }

        return chain.filter(exchange);
    }

    /**
     * Stubs P2's Risk Scorer. Always returns 0.1 for now.
     */
    private double evaluateRiskScoreStub(ServerWebExchange exchange) {
        // TODO (P2): Replace with actual BehavioralRiskScorer.score(requestContext)
        return 0.1;
    }

    /**
     * Stubs P2's Policy Evaluator. Always allows for now.
     */
    private boolean evaluatePolicyDecisionStub(ServerWebExchange exchange) {
        // TODO (P2): Replace with actual PolicyEngine.evaluate(requestContext)
        return true;
    }
}
