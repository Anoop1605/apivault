package com.sentinel.eventstore.query;

import com.sentinel.eventstore.model.SecurityEvent;
import com.sentinel.eventstore.repository.EventRepository;
import com.sentinel.shared.dto.EventDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class EventQueryService {

    private final EventRepository eventRepository;

    public EventQueryService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    /**
     * Fetches all events for a session, ordered by timestamp ascending.
     * Maps SecurityEvent entities to the shared EventDTO used across services.
     */
    public List<EventDTO> fetchEventsBySession(UUID sessionId) {
        return eventRepository
                .findBySessionIdOrderByTimestampNsAsc(sessionId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private EventDTO toDTO(SecurityEvent event) {
        EventDTO dto = new EventDTO();
        dto.setSessionId(event.getSessionId());
        dto.setTimestampNs(event.getTimestampNs());
        return dto;
    }
}
