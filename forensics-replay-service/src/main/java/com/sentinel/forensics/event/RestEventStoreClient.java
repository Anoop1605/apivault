package com.sentinel.forensics.event;

import com.sentinel.shared.dto.EventDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class RestEventStoreClient implements EventStoreClient {

    private final RestTemplate restTemplate;
    private final String eventStoreBaseUrl;

    public RestEventStoreClient(RestTemplate restTemplate,
            @Value("${eventstore.base-url:http://localhost:8081}") String eventStoreBaseUrl) {
        this.restTemplate = restTemplate;
        this.eventStoreBaseUrl = eventStoreBaseUrl;
    }

    @Override
    public List<EventDTO> fetchEvents(UUID sessionId) {
        String url = eventStoreBaseUrl + "/events/sessions/" + sessionId;
        try {
            ResponseEntity<EventDTO[]> response = restTemplate.getForEntity(url, EventDTO[].class);
            EventDTO[] body = response.getBody();
            if (body == null)
                return Collections.emptyList();
            return new ArrayList<>(List.of(body));
        } catch (Exception e) {
            log.error("Error fetching events for session {}: {}", sessionId, e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public List<UUID> fetchAllSessionIds() {
        String url = eventStoreBaseUrl + "/events/sessions";
        try {
            ResponseEntity<UUID[]> response = restTemplate.getForEntity(url, UUID[].class);
            UUID[] body = response.getBody();
            if (body == null)
                return Collections.emptyList();
            return new ArrayList<>(List.of(body));
        } catch (Exception e) {
            log.error("Error fetching all session IDs from Event Store: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
}
