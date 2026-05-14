package com.sentinel.gateway.filter;

import com.sentinel.gateway.event.GatewayEventPublisher;
import com.sentinel.shared.context.RequestContext;
import com.sentinel.shared.enums.EventType;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
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

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class ZeroTrustOrchestrationFilter implements GlobalFilter, Ordered {

    private static final double FLAG_THRESHOLD = 0.70;

    private final MeterRegistry meterRegistry;
    private final WebClient webClient;
    private final GatewayEventPublisher eventPublisher;

    public ZeroTrustOrchestrationFilter(
            MeterRegistry meterRegistry,
            WebClient.Builder webClientBuilder,
            GatewayEventPublisher eventPublisher,
            @Value("${sentinel.policy-engine.url:http://localhost:8082}") String policyEngineUrl) {
        this.meterRegistry = meterRegistry;
        this.eventPublisher = eventPublisher;
        this.webClient = webClientBuilder.baseUrl(policyEngineUrl).build();
    }

    @Override
    public int getOrder() {
        return -70;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        if (path.startsWith("/actuator") || path.startsWith("/health") || path.startsWith("/metrics")) {
            return chain.filter(exchange);
        }

        long startTime = System.currentTimeMillis();
        RequestContext context = buildRequestContext(exchange);

        return webClient.post()
                .uri("/policy/evaluate")
                .bodyValue(context)
                .retrieve()
                .bodyToMono(PolicyEvalResponse.class)
                .timeout(Duration.ofSeconds(5))
                .flatMap(result -> {
                    long duration = System.currentTimeMillis() - startTime;
                    Timer.builder("policy.eval.time")
                            .publishPercentiles(0.99)
                            .register(meterRegistry)
                            .record(duration, TimeUnit.MILLISECONDS);

                    exchange.getAttributes().put("riskScore", result.riskScore);
                    exchange.getAttributes().put("policyDecision", result.decision);
                    exchange.getAttributes().put("policyRuleId", result.ruleId);
                    exchange.getAttributes().put("policyRuleVersion", result.ruleVersion);
                    exchange.getAttributes().put("policyRuleSnapshotId", result.snapshotId);
                    exchange.getAttributes().put("policySnapshotRules", result.snapshotRules);

                    Mono<Void> eventFlow = Mono.empty();
                    if (result.riskScore >= FLAG_THRESHOLD) {
                        eventFlow = eventFlow.then(eventPublisher.publish(exchange, EventType.RISK_FLAGGED));
                    }

                    if ("ALLOW".equals(result.decision)) {
                        eventFlow = eventFlow.then(eventPublisher.publish(exchange, EventType.POLICY_ALLOWED));
                        return eventFlow.then(chain.filter(exchange));
                    }

                    eventFlow = eventFlow.then(eventPublisher.publish(exchange, EventType.POLICY_DENIED));
                    return eventFlow.then(Mono.error(
                            new ResponseStatusException(HttpStatus.FORBIDDEN, "Policy denied access")));
                })
                .onErrorMap(ResponseStatusException.class, ex -> ex)
                .onErrorMap(ex -> {
                    log.error("Policy Engine unavailable for {}: {}", path, ex.getMessage());
                    return new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Policy engine unavailable");
                });
    }

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
                .httpMethod(exchange.getRequest().getMethod() != null ? exchange.getRequest().getMethod().name() : "GET")
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

    private static class PolicyEvalResponse {
        public String ruleId;
        public Integer ruleVersion;
        public String decision;
        public UUID snapshotId;
        public List<String> snapshotRules;
        public double riskScore;
    }
}
