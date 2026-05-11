package com.sentinel.gateway.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sentinel.shared.dto.EventDTO;
import com.sentinel.shared.enums.EventType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@Order(-1)
@RequiredArgsConstructor
public class GatewayExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper;
    private final WebClient.Builder webClientBuilder;

    @Value("${sentinel.event-store.url:http://localhost:8081}")
    private String eventStoreUrl;

    @Value("${sentinel.gateway.version:2.0.0}")
    private String gatewayVersion;

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        log.error("Gateway error occurred for path {}: {}", exchange.getRequest().getURI().getPath(), ex.getMessage());

        HttpStatus status;
        String errorMessage;

        if (ex instanceof ConnectException || ex.getMessage().contains("Connection refused")) {
            status = HttpStatus.BAD_GATEWAY;
            errorMessage = "Upstream service is currently unavailable or refusing connection";
        } else {
            status = HttpStatus.SERVICE_UNAVAILABLE;
            errorMessage = "Service unavailable: " + ex.getMessage();
        }

        // Emit GATEWAY_ERROR event
        emitGatewayErrorEvent(exchange, status, ex.getMessage()).subscribe();

        // Write structured JSON response
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("status", status.value());
        errorDetails.put("error", status.getReasonPhrase());
        errorDetails.put("message", errorMessage);
        errorDetails.put("path", exchange.getRequest().getURI().getPath());

        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsBytes(errorDetails);
        } catch (JsonProcessingException e) {
            bytes = "{\"error\":\"Bad Gateway\",\"message\":\"Upstream service unavailable\"}".getBytes();
        }

        return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(bytes)));
    }

    private Mono<Void> emitGatewayErrorEvent(ServerWebExchange exchange, HttpStatus status, String cause) {
        Object sessionAttr = exchange.getAttribute("sessionId");
        String sessionIdStr = sessionAttr != null ? sessionAttr.toString() : UUID.randomUUID().toString();
        UUID sessionId;
        try {
            sessionId = UUID.fromString(sessionIdStr);
        } catch (IllegalArgumentException e) {
            sessionId = UUID.randomUUID();
        }

        String userId = exchange.getAttribute("userId");

        EventDTO event = EventDTO.builder()
                .eventId(UUID.randomUUID())
                .timestampNs(Instant.now().getEpochSecond() * 1_000_000_000L + Instant.now().getNano())
                .eventType(EventType.GATEWAY_ERROR)
                .sessionId(sessionId)
                .userId(userId)
                .endpoint(exchange.getRequest().getURI().getPath())
                .httpMethod(exchange.getRequest().getMethod().name())
                .sourceIp(extractClientIp(exchange))
                .userAgent(exchange.getRequest().getHeaders().getFirst(HttpHeaders.USER_AGENT))
                .gatewayVersion(gatewayVersion)
                .build();

        return webClientBuilder.build()
                .post()
                .uri(eventStoreUrl + "/api/events")
                .bodyValue((Object) event)
                .retrieve()
                .bodyToMono(Void.class)
                .doOnError(e -> log.error("Failed to emit GATEWAY_ERROR event: {}", e.getMessage()))
                .onErrorResume(e -> Mono.empty());
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
