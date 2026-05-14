package com.sentinel.gateway.filter;

import com.sentinel.shared.context.RequestContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Orchestrates the Zero-Trust execution of the Risk Scorer and Policy Engine.
 *
 * On every inbound request, this filter:
 *  1. Builds a RequestContext from the exchange attributes (user, IP, endpoint, etc.)
 *  2. Sends it to the Policy Engine's /policy/evaluate endpoint via WebClient
 *  3. Reads back the PolicyEvaluationResult (decision + risk score)
 *  4. If DENY → rejects with 403 Forbidden
 *  5. If ALLOW → enriches the exchange with riskScore and continues
 *
 * PRD FR-DB-07: Emits policy.eval.time.p99 metric.
 */
@Slf4j
@Component
public class ZeroTrustOrchestrationFilter implements GlobalFilter, Ordered {

    private final MeterRegistry meterRegistry;
    private final WebClient webClient;
    private final String policyEngineUrl;

    public ZeroTrustOrchestrationFilter(
            MeterRegistry meterRegistry,
            WebClient.Builder webClientBuilder,
            @Value("${sentinel.policy-engine.url:http://localhost:8082}") String policyEngineUrl) {
        this.meterRegistry = meterRegistry;
        this.policyEngineUrl = policyEngineUrl;
        this.webClient = webClientBuilder.baseUrl(policyEngineUrl).build();
        log.info("ZeroTrustOrchestrationFilter initialized with policy-engine at: {}", policyEngineUrl);
    }

    @Override
    public int getOrder() {
        // Must run after Auth Filter but before Event Emission
        return -60;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // Skip for actuator / health / metrics
        if (path.startsWith("/actuator") || path.startsWith("/health") || path.startsWith("/metrics")) {
            return chain.filter(exchange);
        }

        long startTime = System.currentTimeMillis();

        // Build RequestContext from exchange attributes (populated by earlier filters)
        RequestContext context = buildRequestContext(exchange);

        // Call the Policy Engine's /policy/evaluate endpoint
        return webClient.post()
                .uri("/policy/evaluate")
                .bodyValue(context)
                .retrieve()
                .bodyToMono(PolicyEvalResponse.class)
                .timeout(Duration.ofMillis(5000))
                .doOnNext(result -> {
                    long duration = System.currentTimeMillis() - startTime;
                    Timer.builder("policy.eval.time")
                            .publishPercentiles(0.99)
                            .register(meterRegistry)
                            .record(duration, TimeUnit.MILLISECONDS);

                    log.debug("Policy Engine response: decision={}, elapsed={}ms, endpoint={}",
                            result.decision, duration, path);
                })
                .flatMap(result -> {
                    // Store the risk score and decision in the exchange for downstream use
                    double riskScore = result.riskScore;
                    if (result.decision != null && result.decision.contains("DENY")) {
                        exchange.getAttributes().put("riskScore", riskScore);
                        log.warn("Zero-Trust Policy Engine DENIED request for path: {}", path);
                        meterRegistry.counter("policy.denied.count").increment();
                        return Mono.error(new ResponseStatusException(
                                HttpStatus.FORBIDDEN, "Policy Engine Denied Access"));
                    }

                    exchange.getAttributes().put("riskScore", riskScore);
                    exchange.getAttributes().put("policyDecision", result.decision);
                    exchange.getAttributes().put("policyRuleId", result.ruleId);

                    if (result.decision != null && result.decision.contains("FLAG")) {
                        log.warn("Request FLAGGED by Policy Engine (path={})", path);
                        meterRegistry.counter("risk.flagged.count").increment();
                    }

                    return chain.filter(exchange);
                })
                .onErrorResume(ResponseStatusException.class, Mono::error) // re-throw 403
                .onErrorResume(ex -> {
                    // Policy Engine is unavailable — fail-open with warning
                    log.warn("Policy Engine unavailable at {}. Failing OPEN for path: {}. Error: {}",
                            policyEngineUrl, path, ex.getMessage());
                    exchange.getAttributes().put("riskScore", 0.0);
                    exchange.getAttributes().put("policyDecision", "ALLOW_FAILOPEN");
                    meterRegistry.counter("policy.failopen.count").increment();
                    return chain.filter(exchange);
                });
    }

    /**
     * Builds the RequestContext DTO from the exchange attributes set by
     * earlier filters (SessionAssignFilter, JwtAuthFilter).
     */
    private RequestContext buildRequestContext(ServerWebExchange exchange) {
        Object sessionAttr = exchange.getAttribute("sessionId");
        String sessionId = sessionAttr != null ? sessionAttr.toString() : null;
        String userId = exchange.getAttribute("userId");

        @SuppressWarnings("unchecked")
        List<String> roles = exchange.getAttribute("roles");

        return RequestContext.builder()
                .userId(userId)
                .roles(roles)
                .endpoint(exchange.getRequest().getURI().getPath())
                .httpMethod(exchange.getRequest().getMethod().name())
                .sourceIp(extractClientIp(exchange))
                .sessionId(sessionId)
                .userAgent(exchange.getRequest().getHeaders().getFirst("User-Agent"))
                .build();
    }

    private String extractClientIp(ServerWebExchange exchange) {
        String forwarded = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (forwarded != null && !forwarded.isEmpty()) {
            return forwarded.split(",")[0].trim();
        }
        InetSocketAddress remoteAddress = exchange.getRequest().getRemoteAddress();
        if (remoteAddress != null && remoteAddress.getAddress() != null) {
            return remoteAddress.getAddress().getHostAddress();
        }
        return "127.0.0.1";
    }

    /**
     * Lightweight DTO to deserialize the Policy Engine's response.
     * Avoids importing the policy-engine module's model classes.
     */
    private static class PolicyEvalResponse {
        public String ruleId;
        public String decision;
        public List<String> matchedConditions;
        public long evaluationTimeNs;
        public double riskScore;
    }
}
