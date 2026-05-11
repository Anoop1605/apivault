package com.sentinel.eventstore.writer;

import com.sentinel.eventstore.model.SecurityEvent;
import com.sentinel.eventstore.repository.EventRepository;
import com.sentinel.shared.dto.EventDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * EventWriter — writes security events to the append-only event store.
 * Converts EventDTO (from gateway) to SecurityEvent entity and persists to DB.
 * PRD Section 5.5: Event Store — append-only with no UPDATE/DELETE allowed.
 */
@Slf4j
@Service
public class EventWriter {

    private final EventRepository eventRepository;

    public EventWriter(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    /**
     * Persist a single event to the event store.
     * Converts EventDTO to SecurityEvent JPA entity and saves.
     *
     * @param eventDto The event DTO from the gateway
     * @return The persisted SecurityEvent
     * @throws RuntimeException if write fails
     */
    public SecurityEvent writeEvent(EventDTO eventDto) {
        try {
            SecurityEvent event = mapDtoToEntity(eventDto);
            SecurityEvent saved = eventRepository.save(event);
            log.info("Event persisted: eventId={}, type={}, sessionId={}",
                    saved.getId(), saved.getEventType(), saved.getSessionId());
            return saved;
        } catch (Exception e) {
            log.error("Failed to write event: {}", e.getMessage(), e);
            throw new RuntimeException("Event write failed: " + e.getMessage(), e);
        }
    }

    /**
     * Map EventDTO (from gateway) to SecurityEvent (JPA entity).
     * Maps the core fields represented by SecurityEvent.
     */
    private SecurityEvent mapDtoToEntity(EventDTO dto) {
        SecurityEvent event = new SecurityEvent();
        event.setSessionId(dto.getSessionId());
        event.setTimestampNs(dto.getTimestampNs() != null ? dto.getTimestampNs() : System.nanoTime());
        event.setEventType(dto.getEventType());
        event.setEndpoint(dto.getEndpoint());
        event.setSourceIp(dto.getSourceIp());
        event.setDecision(dto.getDecision());
        event.setRuleMatched(dto.getPolicyRuleId());
        return event;
    }
}
