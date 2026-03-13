package com.sentinel.forensics.event;

import com.sentinel.shared.dto.EventDTO;
import java.util.List;
import java.util.UUID;
import java.util.ArrayList;

// Mock implementation for local testing
public class MockEventStoreClient implements EventStoreClient {
    @Override
    public List<EventDTO> fetchEvents(UUID sessionId) {
        List<EventDTO> events = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            EventDTO event = new EventDTO();
            event.setSessionId(sessionId);
            event.setTimestampNs(1000L * i);
            events.add(event);
        }
        return events;
    }
}
