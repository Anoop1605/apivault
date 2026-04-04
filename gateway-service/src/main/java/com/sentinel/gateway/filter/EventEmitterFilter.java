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

import java.net.InetSocketAddress;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    @Value("#{'${sentinel.gateway.public-paths:/actuator,/health,/metrics}'.split(',')}")
    private List<String> publicPaths;

    @Value("#{'${sentinel.gateway.redact-headers:Authorization,Cookie,Set-Cookie}'.split(',')}")
    private List<String> redactHeaders;

    @Override
    public int getOrder() {
        // Runs after session assignment (-80), before routing
        return -50;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // Skip event emission for actuator endpoints
        if (publicPaths.stream().anyMatch(path::startsWith)) {
            return chain.filter(exchange);
        }

        Object sessionAttr = exchange.getAttribute("sessionId");
        String sessionId = sessionAttr != null ? sessionAttr.toString() : UUID.randomUUID().toString();
        
        String userId = exchange.getAttribute("userId");
        @SuppressWarnings("unchecked")
        List<String> roles = exchange.getAttribute("roles");

        Map<String, Object> requestContextMap = new HashMap<>();
        exchange.getRequest().getHeaders().forEach((key, values) -> {
            String val = String.join(",", values);
            if (redactHeaders.stream().anyMatch(h -> h.equalsIgnoreCase(key))) {
                requestContextMap.put(key, "[REDACTED]");
            } else {
                requestContextMap.put(key, val);
            }
        });

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
                .requestContext(requestContextMap)
                .build();

        // Emit REQUEST_RECEIVED, then proceed with chain
        return emitEvent(receivedEvent)
                .then(chain.filter(exchange))
                .then(Mono.defer(() -> {
                    // Post-filter: emit REQUEST_FORWARDED on success
                    var status = exchange.getResponse().getStatusCode();
                    int statusCode = status != null ? status.value() : 0;

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
                            .requestContext(requestContextMap)
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
                .bodyValue((Object) event)
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
        InetSocketAddress remoteAddress = exchange.getRequest().getRemoteAddress();
        if (remoteAddress != null && remoteAddress.getAddress() != null) {
            return remoteAddress.getAddress().getHostAddress();
        }
        return "127.0.0.1";
    }
}
