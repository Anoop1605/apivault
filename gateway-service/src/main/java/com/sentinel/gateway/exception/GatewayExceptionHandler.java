package com.sentinel.gateway.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sentinel.gateway.event.GatewayEventPublisher;
import com.sentinel.shared.enums.EventType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.ConnectException;
import java.util.HashMap;
import java.util.Map;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Component
@Order(-1)
@RequiredArgsConstructor
public class GatewayExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper;
    private final GatewayEventPublisher eventPublisher;
    private final MeterRegistry meterRegistry;

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        log.error("Gateway error occurred for path {}: {}", exchange.getRequest().getURI().getPath(), ex.getMessage());

        HttpStatus status;
        String errorMessage;

        if (ex instanceof ResponseStatusException) {
            status = HttpStatus.valueOf(((ResponseStatusException) ex).getStatusCode().value());
            errorMessage = ((ResponseStatusException) ex).getReason();
            if (errorMessage == null) errorMessage = ex.getMessage();
        } else if (ex instanceof ConnectException || (ex.getMessage() != null && ex.getMessage().contains("Connection refused"))) {
            status = HttpStatus.BAD_GATEWAY;
            errorMessage = "Upstream service is currently unavailable or refusing connection";
        } else {
            status = HttpStatus.SERVICE_UNAVAILABLE;
            errorMessage = "Service unavailable: " + ex.getMessage();
        }

        meterRegistry.counter("gateway.error.count", "status", String.valueOf(status.value())).increment();

        // Emit GATEWAY_ERROR event
        eventPublisher.publish(exchange, EventType.GATEWAY_ERROR).onErrorResume(error -> Mono.empty()).subscribe();

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

}
