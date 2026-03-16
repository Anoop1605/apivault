package com.sentinel.gateway.filter;

import com.sentinel.shared.dto.EventDTO;
import com.sentinel.shared.enums.EventType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Emits event-sourcing events to the Event Store.
 * PRD FR-GW-04: REQUEST_RECEIVED event for every inbound request.
 * PRD FR-GW-05: REQUEST_FORWARDED event upon successful upstream forwarding.
 * PRD FR-GW-06: GATEWAY_ERROR event on upstream unavailability.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EventEmitterFilter implements GlobalFilter, Ordered {

    private final WebClient.Builder webClientBuilder;

    @Value("${sentinel.event-store.url:http://localhost:8081}")
    private String eventStoreUrl;

    @Value("${sentinel.gateway.version:2.0.0}")
    private String gatewayVersion;

    /** Public endpoints that don't need event tracking */
    private static final List<String> SKIP_PATHS = List.of(
            "/actuator", "/health", "/metrics");

    @Override
    public int getOrder() {
        // Runs after session assignment (-80), before routing
        return -50;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // Skip event emission for actuator endpoints
        if (SKIP_PATHS.stream().anyMatch(path::startsWith)) {
            return chain.filter(exchange);
        }

        String sessionId = exchange.getAttributeOrDefault("sessionId", UUID.randomUUID().toString());
        String userId = exchange.getAttribute("userId");
        List<String> roles = exchange.getAttribute("roles");

        // Build REQUEST_RECEIVED event (FR-GW-04)
        EventDTO receivedEvent = EventDTO.builder()
                .eventId(UUID.randomUUID())
                .timestampNs(nowNanos())
                .eventType(EventType.REQUEST_RECEIVED)
                .sessionId(parseUUID(sessionId))
                .userId(userId)
                .roles(roles)
                .endpoint(path)
                .httpMethod(exchange.getRequest().getMethod().name())
                .sourceIp(extractClientIp(exchange))
                .userAgent(exchange.getRequest().getHeaders().getFirst(HttpHeaders.USER_AGENT))
                .gatewayVersion(gatewayVersion)
                .build();

        // Emit REQUEST_RECEIVED, then proceed with chain
        return emitEvent(receivedEvent)
                .then(chain.filter(exchange))
                .then(Mono.defer(() -> {
                    // Post-filter: emit REQUEST_FORWARDED on success
                    int statusCode = exchange.getResponse().getStatusCode() != null
                            ? exchange.getResponse().getStatusCode().value()
                            : 0;

                    EventType postEventType;
                    if (statusCode >= 200 && statusCode < 500) {
                        postEventType = EventType.REQUEST_FORWARDED;
                    } else {
                        postEventType = EventType.GATEWAY_ERROR;
                    }

                    EventDTO forwardedEvent = EventDTO.builder()
                            .eventId(UUID.randomUUID())
                            .timestampNs(nowNanos())
                            .eventType(postEventType)
                            .sessionId(parseUUID(sessionId))
                            .userId(userId)
                            .roles(roles)
                            .endpoint(path)
                            .httpMethod(exchange.getRequest().getMethod().name())
                            .sourceIp(extractClientIp(exchange))
                            .userAgent(exchange.getRequest().getHeaders().getFirst(HttpHeaders.USER_AGENT))
                            .gatewayVersion(gatewayVersion)
                            .build();

                    return emitEvent(forwardedEvent);
                }));
    }

    /**
     * Send event to event-store-service via WebClient.
     */
    private Mono<Void> emitEvent(EventDTO event) {
        return webClientBuilder.build()
                .post()
                .uri(eventStoreUrl + "/api/events")
                .bodyValue(event)
                .retrieve()
                .bodyToMono(Void.class)
                .doOnSuccess(v -> log.debug("Emitted {} event: {}", event.getEventType(), event.getEventId()))
                .doOnError(e -> log.error("Failed to emit {} event: {}", event.getEventType(), e.getMessage()))
                .onErrorResume(e -> Mono.empty());
    }

    private long nowNanos() {
        Instant now = Instant.now();
        return now.getEpochSecond() * 1_000_000_000L + now.getNano();
    }

    private UUID parseUUID(String value) {
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            return UUID.randomUUID();
        }
    }

    private String extractClientIp(ServerWebExchange exchange) {
        String forwarded = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (forwarded != null && !forwarded.isEmpty()) {
            return forwarded.split(",")[0].trim();
        }
        return exchange.getRequest().getRemoteAddress() != null
                ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                : "127.0.0.1";
    }
}
