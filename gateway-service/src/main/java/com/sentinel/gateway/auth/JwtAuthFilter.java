package com.sentinel.gateway.auth;

import com.sentinel.shared.dto.EventDTO;
import com.sentinel.shared.enums.EventType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Global filter that validates JWT on every request.
 * PRD: FR-AU-01 — requests without valid JWT are rejected with HTTP 401.
 * PRD: FR-AU-03 — emits AUTH_FAILED event on validation failure.
 * PRD: FR-AU-04 — extracted claims propagated as exchange attributes.
 * PRD: FR-AU-05 — configurable public endpoints bypass validation.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter implements GlobalFilter, Ordered {

    private final JwtUtil jwtUtil;
    private final WebClient.Builder webClientBuilder;

    @Value("${sentinel.event-store.url:http://localhost:8081}")
    private String eventStoreUrl;

    @Value("${sentinel.gateway.version:2.0.0}")
    private String gatewayVersion;

    /** Public endpoints that bypass JWT validation (PRD FR-AU-05). */
    @Value("#{'${sentinel.gateway.public-paths:/actuator,/health,/metrics}'.split(',')}")
    private List<String> publicPaths;

    @Override
    public int getOrder() {
        // Runs first in filter chain — authentication must happen before anything else
        return -100;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // Skip JWT validation for public endpoints
        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Missing or invalid Authorization header for path: {}", path);
            return handleAuthFailure(exchange, "Missing or invalid Authorization header", null);
        }

        String token = authHeader.substring(7);

        try {
            Claims claims = jwtUtil.parseAndValidate(token);

            // Propagate extracted claims as exchange attributes (FR-AU-04)
            String userId = jwtUtil.getUserId(claims);
            List<String> roles = jwtUtil.getRoles(claims);
            String scope = jwtUtil.getScope(claims);

            exchange.getAttributes().put("userId", userId);
            exchange.getAttributes().put("roles", roles);
            exchange.getAttributes().put("scope", scope);
            exchange.getAttributes().put("jwtClaims", claims);

            log.debug("JWT validated for user: {} with roles: {}", userId, roles);
            return chain.filter(exchange);

        } catch (JwtException e) {
            log.warn("JWT validation failed for path {}: {}", path, e.getMessage());
            return handleAuthFailure(exchange, e.getMessage(), token);
        }
    }

    /**
     * Handle authentication failure: return 401 + emit AUTH_FAILED event.
     */
    private Mono<Void> handleAuthFailure(ServerWebExchange exchange, String reason, String token) {
        // Emit AUTH_FAILED event asynchronously (FR-AU-03)
        emitAuthFailedEvent(exchange, reason, token).subscribe();

        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String body = "{\"error\":\"Unauthorized\",\"message\":\"" + reason + "\"}";
        byte[] bytes = body != null ? body.getBytes() : new byte[0];
        return exchange.getResponse().writeWith(
                Mono.just(exchange.getResponse().bufferFactory().wrap(bytes)));
    }

    /**
     * Emit AUTH_FAILED event to event-store-service.
     */
    private Mono<Void> emitAuthFailedEvent(ServerWebExchange exchange, String reason, String token) {
        log.info("Emitting AUTH_FAILED. Headers: {}", exchange.getRequest().getHeaders());
        Object sessionAttr = exchange.getAttribute("sessionId");
        String sessionIdStr = sessionAttr != null ? sessionAttr.toString() : UUID.randomUUID().toString();
        UUID sessionId;
        try {
            sessionId = UUID.fromString(sessionIdStr);
        } catch (IllegalArgumentException e) {
            sessionId = UUID.randomUUID();
        }

        String userId = exchange.getRequest().getHeaders().getFirst("X-User-ID");
        String riskScoreHeader = exchange.getRequest().getHeaders().getFirst("X-Risk-Score");
        Double riskScore = riskScoreHeader != null ? Double.parseDouble(riskScoreHeader) : 0.0;

        String policyRuleId = exchange.getRequest().getHeaders().getFirst("X-Policy-Rule-ID");
        if (policyRuleId == null) policyRuleId = "AUTH-POLICY-DEFAULT";

        Integer policyRuleVersion = parseInteger(exchange.getRequest().getHeaders().getFirst("X-Policy-Rule-Version"));
        if (policyRuleVersion == null) policyRuleVersion = 1;

        UUID policyRuleSnapshotId = parseUUID(exchange.getRequest().getHeaders().getFirst("X-Policy-Rule-Snapshot-ID"));
        if (policyRuleSnapshotId == null) policyRuleSnapshotId = UUID.fromString("00000000-0000-0000-0000-000000000000");

        String bodyHash = exchange.getRequest().getHeaders().getFirst("X-Body-Hash");
        if (bodyHash == null || bodyHash.isEmpty()) {
            bodyHash = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"; // SHA-256 of empty string
        }

        EventDTO event = EventDTO.builder()
                .eventId(UUID.randomUUID())
                .timestampNs(Instant.now().getEpochSecond() * 1_000_000_000L + Instant.now().getNano())
                .eventType(EventType.AUTH_FAILED)
                .sessionId(sessionId)
                .userId(userId != null ? userId : "unauthenticated")
                .riskScore(riskScore)
                .decision(com.sentinel.shared.enums.Decision.DENY)
                .endpoint(exchange.getRequest().getURI().getPath())
                .httpMethod(exchange.getRequest().getMethod().name())
                .sourceIp(extractClientIp(exchange))
                .userAgent(exchange.getRequest().getHeaders().getFirst(HttpHeaders.USER_AGENT))
                .gatewayVersion(gatewayVersion)
                .policyRuleId(policyRuleId)
                .policyRuleVersion(policyRuleVersion)
                .policyRuleSnapshotId(policyRuleSnapshotId)
                .bodyHash(bodyHash)
                .build();

        return webClientBuilder.build()
                .post()
                .uri(eventStoreUrl + "/api/events")
                .bodyValue((Object) event)
                .retrieve()
                .bodyToMono(Void.class)
                .doOnError(e -> log.error("Failed to emit AUTH_FAILED event: {}", e.getMessage()))
                .onErrorResume(e -> Mono.empty());
    }

    private boolean isPublicPath(String path) {
        return publicPaths.stream().anyMatch(path::startsWith);
    }

    private Integer parseInteger(String value) {
        try {
            return value != null ? Integer.parseInt(value) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private UUID parseUUID(String value) {
        try {
            return value != null ? UUID.fromString(value) : null;
        } catch (IllegalArgumentException e) {
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
        return "unknown";
    }
}
