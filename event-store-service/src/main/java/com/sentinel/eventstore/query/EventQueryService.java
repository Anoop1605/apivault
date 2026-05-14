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

    /**
     * Fetches all distinct session IDs from the database.
     * Used for dynamic session discovery instead of hardcoded lists.
     */
    public List<UUID> fetchAllSessionIds() {
        return eventRepository.findAllDistinctSessionIds();
    }

    private EventDTO toDTO(SecurityEvent event) {
        return EventDTO.builder()
                .eventId(event.getId())
                .sessionId(event.getSessionId())
                .timestampNs(event.getTimestampNs())
                .eventType(event.getEventType())
                .userId(event.getUserId())
                .roles(event.getRoles())
                .endpoint(event.getEndpoint())
                .httpMethod(event.getHttpMethod())
                .sourceIp(event.getSourceIp())
                .userAgent(event.getUserAgent())
                .policyRuleId(event.getPolicyRuleId())
                .policyRuleVersion(event.getPolicyRuleVersion())
                .policyRuleSnapshot(event.getPolicyRuleSnapshot())
                .policyRuleSnapshotId(event.getPolicyRuleSnapshotId())
                .riskScore(event.getRiskScore())
                .riskSignals(event.getRiskSignals())
                .decision(event.getDecision())
                .requestContext(event.getRequestContext())
                .bodyHash(event.getBodyHash())
                .eventHash(event.getEventHash())
                .gatewayVersion(event.getGatewayVersion())
                .build();
    }
}
