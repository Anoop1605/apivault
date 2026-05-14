package com.sentinel.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Assigns a session ID to every request.
 * PRD Section 7.2, Step 4: Assign or propagate session_id (X-Session-ID header
 * or generate new UUID).
 */
@Slf4j
@Component
public class SessionAssignFilter implements GlobalFilter, Ordered {

    private static final String SESSION_HEADER = "X-Session-ID";
    private static final String SESSION_ATTRIBUTE = "sessionId";

    @Override
    public int getOrder() {
        // Must run first so every downstream filter sees the same session ID.
        return -100;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String sessionId = exchange.getRequest().getHeaders().getFirst(SESSION_HEADER);

        if (sessionId == null || sessionId.isBlank()) {
            sessionId = UUID.randomUUID().toString();
            log.debug("Generated new session ID: {}", sessionId);
        } else {
            log.debug("Using existing session ID from header: {}", sessionId);
        }

        // Store in exchange attributes for downstream filters
        exchange.getAttributes().put(SESSION_ATTRIBUTE, sessionId);

        // Add session ID to request headers for upstream service
        String finalSessionId = sessionId;
        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                .header(SESSION_HEADER, finalSessionId)
                .build();

        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }
}
