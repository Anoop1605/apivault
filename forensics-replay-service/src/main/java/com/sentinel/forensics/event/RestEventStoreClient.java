package com.sentinel.forensics.event;

import com.sentinel.shared.dto.EventDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
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
    private final MockEventStoreClient mockClient = new MockEventStoreClient();

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
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Session {} not found in Event Store, falling back to mock data.", sessionId);
            return mockClient.fetchEvents(sessionId);
        } catch (ResourceAccessException e) {
            log.warn("Event Store unavailable at {}, falling back to mock data.", eventStoreBaseUrl);
            return mockClient.fetchEvents(sessionId);
        } catch (Exception e) {
            log.error("Error fetching events for session {}, falling back to mock data.", sessionId, e);
            return mockClient.fetchEvents(sessionId);
        }
    }
}
