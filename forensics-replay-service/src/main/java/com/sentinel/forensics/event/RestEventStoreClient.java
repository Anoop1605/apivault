package com.sentinel.forensics.event;

import com.sentinel.shared.dto.EventDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class RestEventStoreClient implements EventStoreClient {

    private final RestTemplate restTemplate;
    private final String eventStoreBaseUrl;

    public RestEventStoreClient(RestTemplate restTemplate, String eventStoreBaseUrl) {
        this.restTemplate = restTemplate;
        this.eventStoreBaseUrl = eventStoreBaseUrl;
    }

    @Override
    public List<EventDTO> fetchEvents(UUID sessionId) {
        String url = eventStoreBaseUrl + "/events/sessions/" + sessionId;
        try {
            ResponseEntity<EventDTO[]> response = restTemplate.getForEntity(url, EventDTO[].class);
            EventDTO[] body = response.getBody();
            if (body == null) return Collections.emptyList();
            return new ArrayList<>(List.of(body));
        } catch (HttpClientErrorException.NotFound e) {
            return Collections.emptyList();
        }
    }
}
