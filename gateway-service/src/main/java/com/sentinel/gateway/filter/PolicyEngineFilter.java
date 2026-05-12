package com.sentinel.gateway.filter;

import com.sentinel.shared.context.RequestContext;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

/**
 * PolicyEngineFilter — Enforces Zero-Trust ABAC policies by calling the Policy Engine service.
 * PRD Section 10.5: No request is implicitly trusted.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PolicyEngineFilter implements GlobalFilter, Ordered {

    private final WebClient.Builder webClientBuilder;

    @Value("${sentinel.policy-engine.url:http://localhost:8082}")
    private String policyEngineUrl;

    @Value("#{'${sentinel.gateway.public-paths:/actuator,/health,/metrics}'.split(',')}")
    private List<String> publicPaths;

    @Override
    public int getOrder() {
        // Runs after authentication (-100) and session assignment (-90), before event emission (-70)
        return -80;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // Skip policy evaluation for public endpoints
        if (publicPaths.stream().anyMatch(path::startsWith)) {
            return chain.filter(exchange);
        }

        // Build RequestContext from exchange attributes and headers
        RequestContext context = RequestContext.builder()
                .userId(exchange.getAttribute("userId"))
                .roles(exchange.getAttribute("roles"))
                .endpoint(path)
                .httpMethod(exchange.getRequest().getMethod().name())
                .sourceIp(extractClientIp(exchange))
                .riskScore(extractRiskScore(exchange))
                .build();

        log.debug("Evaluating policy for user: {}, path: {}", context.getUserId(), path);

        return webClientBuilder.build()
                .post()
                .uri(policyEngineUrl + "/api/policy/evaluate")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(context)
                .retrieve()
                .bodyToMono(PolicyDecisionResponse.class)
                .flatMap(decision -> {
                    log.info("Policy decision for {}: {} (Rule: {})", path, decision.getDecision(), decision.getRuleId());
                    
                    // Store decision in exchange attributes for downstream filters (like EventEmitterFilter)
                    exchange.getAttributes().put("policyDecision", decision.getDecision());
                    exchange.getAttributes().put("policyRuleId", decision.getRuleId());
                    exchange.getAttributes().put("policyRuleVersion", decision.getRuleVersion());
                    exchange.getAttributes().put("policyRuleSnapshotId", decision.getSnapshotId());

                    if ("DENY".equalsIgnoreCase(decision.getDecision())) {
                        log.warn("Access DENIED by policy rule {} for user {}", decision.getRuleId(), context.getUserId());
                        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                        return exchange.getResponse().setComplete();
                    }

                    return chain.filter(exchange);
                })
                .doOnError(e -> log.error("Policy evaluation failed: {}", e.getMessage()))
                .onErrorResume(e -> {
                    // Fail-safe: Deny if policy engine is unreachable (Zero Trust)
                    exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                    return exchange.getResponse().setComplete();
                });
    }

    private String extractClientIp(ServerWebExchange exchange) {
        String forwarded = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (forwarded != null && !forwarded.isEmpty()) {
            return forwarded.split(",")[0].trim();
        }
        return exchange.getRequest().getRemoteAddress() != null ? 
               exchange.getRequest().getRemoteAddress().getAddress().getHostAddress() : "127.0.0.1";
    }

    private Double extractRiskScore(ServerWebExchange exchange) {
        String scoreHeader = exchange.getRequest().getHeaders().getFirst("X-Risk-Score");
        try {
            return scoreHeader != null ? Double.parseDouble(scoreHeader) : 0.0;
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    private static class PolicyDecisionResponse {
        private String ruleId;
        private Integer ruleVersion;
        private UUID snapshotId;
        private String decision;
    }
}
