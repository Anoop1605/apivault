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
        // Runs after authentication (-100), session assignment (-90), and policy evaluation (-80)
        // This ensures metadata is available for the REQUEST_RECEIVED event emission.
        return -70;
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
        if (userId == null) {
            userId = exchange.getRequest().getHeaders().getFirst("X-User-ID");
        }
        
        @SuppressWarnings("unchecked")
        List<String> roles = exchange.getAttribute("roles");
        
        String riskScoreHeader = exchange.getRequest().getHeaders().getFirst("X-Risk-Score");
        Double riskScore = riskScoreHeader != null ? Double.parseDouble(riskScoreHeader) : 0.0;

        Map<String, Object> requestContextMap = new HashMap<>();
        exchange.getRequest().getHeaders().forEach((key, values) -> {
            String val = String.join(",", values);
            if (redactHeaders.stream().anyMatch(h -> h.equalsIgnoreCase(key))) {
                requestContextMap.put(key, "[REDACTED]");
            } else {
                requestContextMap.put(key, val);
            }
        });

        // Extract forensic metadata (now available because PolicyEngineFilter ran at -80)
        String policyDecisionStr = exchange.getAttribute("policyDecision");
        com.sentinel.shared.enums.Decision decision = (policyDecisionStr != null)
                ? com.sentinel.shared.enums.Decision.valueOf(policyDecisionStr.toUpperCase())
                : com.sentinel.shared.enums.Decision.ALLOW;

        String policyRuleId = exchange.getAttribute("policyRuleId");
        if (policyRuleId == null) policyRuleId = "GATEWAY-INTERNAL-POLICY";

        Integer policyRuleVersion = exchange.getAttribute("policyRuleVersion");
        if (policyRuleVersion == null) policyRuleVersion = 1;

        UUID policyRuleSnapshotId = exchange.getAttribute("policyRuleSnapshotId");
        if (policyRuleSnapshotId == null) policyRuleSnapshotId = UUID.fromString("00000000-0000-0000-0000-000000000000");
        
        // Ensure bodyHash is NEVER null (use header if present, or default to empty hash)
        String bodyHash = exchange.getRequest().getHeaders().getFirst("X-Body-Hash");
        if (bodyHash == null || bodyHash.isEmpty()) {
            bodyHash = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"; // SHA-256 of empty string
        }

        // Final references for inner lambdas
        final String finalUserId = userId;
        final Double finalRiskScore = riskScore;
        final Map<String, Object> finalContext = requestContextMap;
        final String finalBodyHash = bodyHash;
        final String finalRuleId = policyRuleId;
        final Integer finalRuleVersion = policyRuleVersion;
        final UUID finalSnapshotId = policyRuleSnapshotId;
        final com.sentinel.shared.enums.Decision finalDecision = decision;

        // Build REQUEST_RECEIVED event (FR-GW-04) - now with full metadata!
        EventDTO receivedEvent = EventDTO.builder()
                .eventId(UUID.randomUUID())
                .timestampNs(nowNanos())
                .eventType(EventType.REQUEST_RECEIVED)
                .sessionId(parseUUID(sessionId))
                .userId(finalUserId)
                .roles(roles)
                .endpoint(path)
                .httpMethod(exchange.getRequest().getMethod().name())
                .sourceIp(extractClientIp(exchange))
                .userAgent(exchange.getRequest().getHeaders().getFirst(HttpHeaders.USER_AGENT))
                .gatewayVersion(gatewayVersion)
                .requestContext(finalContext)
                .riskScore(finalRiskScore)
                .decision(finalDecision)
                .policyRuleId(finalRuleId)
                .policyRuleVersion(finalRuleVersion)
                .policyRuleSnapshotId(finalSnapshotId)
                .bodyHash(finalBodyHash)
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
                            .userId(finalUserId)
                            .roles(roles)
                            .endpoint(path)
                            .httpMethod(exchange.getRequest().getMethod().name())
                            .sourceIp(extractClientIp(exchange))
                            .userAgent(exchange.getRequest().getHeaders().getFirst(HttpHeaders.USER_AGENT))
                            .gatewayVersion(gatewayVersion)
                            .requestContext(finalContext)
                            .riskScore(finalRiskScore)
                            .decision(finalDecision)
                            .policyRuleId(finalRuleId)
                            .policyRuleVersion(finalRuleVersion)
                            .policyRuleSnapshotId(finalSnapshotId)
                            .bodyHash(finalBodyHash)
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

    private Integer parseInteger(String value) {
        try {
            return value != null ? Integer.parseInt(value) : null;
        } catch (NumberFormatException e) {
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
