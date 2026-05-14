package com.sentinel.gateway.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sentinel.shared.dto.EventDTO;
import com.sentinel.shared.enums.Decision;
import com.sentinel.shared.enums.EventType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class GatewayEventPublisher {

    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;

    @Value("${sentinel.event-store.url:http://localhost:8081}")
    private String eventStoreUrl;

    @Value("${sentinel.gateway.version:2.0.0}")
    private String gatewayVersion;

    public Mono<Void> publish(ServerWebExchange exchange, EventType eventType) {
        return publish(exchange, eventType, null);
    }

    public Mono<Void> publish(ServerWebExchange exchange, EventType eventType, Map<String, String> requestContext) {
        EventDTO event = EventDTO.builder()
                .eventId(UUID.randomUUID())
                .timestampNs(nowNanos())
                .eventType(eventType)
                .sessionId(parseSessionId(exchange))
                .userId(exchange.getAttribute("userId"))
                .roles(getRoles(exchange))
                .endpoint(exchange.getRequest().getURI().getPath())
                .httpMethod(exchange.getRequest().getMethod() != null ? exchange.getRequest().getMethod().name() : "GET")
                .sourceIp(extractClientIp(exchange))
                .userAgent(exchange.getRequest().getHeaders().getFirst(HttpHeaders.USER_AGENT))
                .decision(parseDecision(exchange.getAttribute("policyDecision")))
                .policyRuleId(exchange.getAttribute("policyRuleId"))
                .policyRuleVersion(exchange.getAttribute("policyRuleVersion"))
                .policyRuleSnapshotId(exchange.getAttribute("policyRuleSnapshotId"))
                .policyRuleSnapshot(serializeSnapshotRules(exchange.getAttribute("policySnapshotRules")))
                .riskScore(exchange.getAttribute("riskScore"))
                .requestContext(requestContext)
                .gatewayVersion(gatewayVersion)
                .build();

        return webClientBuilder.build()
                .post()
                .uri(eventStoreUrl + "/api/events")
                .bodyValue(event)
                .retrieve()
                .bodyToMono(Void.class)
                .doOnError(error -> log.error("Failed to emit {} for session {}: {}",
                        eventType, event.getSessionId(), error.getMessage()));
    }

    private long nowNanos() {
        Instant now = Instant.now();
        return now.getEpochSecond() * 1_000_000_000L + now.getNano();
    }

    private UUID parseSessionId(ServerWebExchange exchange) {
        Object sessionAttr = exchange.getAttribute("sessionId");
        String sessionId = sessionAttr != null ? sessionAttr.toString() : UUID.randomUUID().toString();
        try {
            return UUID.fromString(sessionId);
        } catch (IllegalArgumentException ex) {
            return UUID.randomUUID();
        }
    }

    @SuppressWarnings("unchecked")
    private List<String> getRoles(ServerWebExchange exchange) {
        Object roles = exchange.getAttribute("roles");
        return roles instanceof List<?> ? (List<String>) roles : null;
    }

    private Decision parseDecision(String decision) {
        if (decision == null || decision.isBlank()) {
            return null;
        }
        try {
            return Decision.valueOf(decision);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private String serializeSnapshotRules(Object rules) {
        if (rules == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(rules);
        } catch (JsonProcessingException ex) {
            log.warn("Failed to serialize snapshot rules: {}", ex.getMessage());
            return null;
        }
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
}
