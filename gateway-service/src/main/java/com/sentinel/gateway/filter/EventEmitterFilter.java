package com.sentinel.gateway.filter;

import com.sentinel.gateway.event.GatewayEventPublisher;
import com.sentinel.shared.enums.EventType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventEmitterFilter implements GlobalFilter, Ordered {

    private final GatewayEventPublisher eventPublisher;

    @Value("#{'${sentinel.gateway.public-paths:/actuator,/health,/metrics}'.split(',')}")
    private List<String> publicPaths;

    @Value("#{'${sentinel.gateway.redact-headers:Authorization,Cookie,Set-Cookie}'.split(',')}")
    private List<String> redactHeaders;

    @Override
    public int getOrder() {
        // Emit REQUEST_RECEIVED before auth/policy evaluation starts.
        return -90;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        if (publicPaths.stream().anyMatch(path::startsWith)) {
            return chain.filter(exchange);
        }

        Map<String, String> requestContext = new HashMap<>();
        exchange.getRequest().getHeaders().forEach((key, values) -> {
            if (redactHeaders.stream().anyMatch(header -> header.equalsIgnoreCase(key))) {
                requestContext.put(key, "[REDACTED]");
            } else {
                requestContext.put(key, String.join(",", values));
            }
        });

        return eventPublisher.publish(exchange, EventType.REQUEST_RECEIVED, requestContext)
                .then(chain.filter(exchange))
                .then(Mono.defer(() -> {
                    HttpStatusCode status = exchange.getResponse().getStatusCode();
                    if (status != null && status.is2xxSuccessful()) {
                        return eventPublisher.publish(exchange, EventType.REQUEST_FORWARDED, requestContext);
                    }
                    return Mono.empty();
                }));
    }
}
