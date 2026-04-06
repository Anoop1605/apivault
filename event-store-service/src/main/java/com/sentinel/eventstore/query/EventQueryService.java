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
     * Map SecurityEvent entity to EventDTO with all 20 fields.
     * Maps all fields from SecurityEvent to EventDTO including roles, riskScore, riskSignals,
     * bodyHash, eventHash, gatewayVersion, etc.
     */
    private EventDTO toDTO(SecurityEvent event) {
        EventDTO dto = EventDTO.builder()
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
                .policyRuleSnapshotId(event.getPolicyRuleSnapshotId())
                .riskScore(event.getRiskScore())
                .riskSignals(event.getRiskSignals())
                .decision(event.getDecision())
                .requestContext(event.getRequestContext())
                .bodyHash(event.getBodyHash())
                .eventHash(event.getEventHash())
                .gatewayVersion(event.getGatewayVersion())
                .build();
        return dto;
    }
}

