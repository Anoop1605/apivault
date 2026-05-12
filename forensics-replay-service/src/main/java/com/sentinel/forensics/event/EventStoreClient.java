package com.sentinel.forensics.event;

import com.sentinel.shared.dto.EventDTO;
import java.util.List;
import java.util.UUID;

// Modular client interface for event-store-service
public interface EventStoreClient {
    List<EventDTO> fetchEvents(UUID sessionId);
    List<UUID> fetchSessionIds();
}
