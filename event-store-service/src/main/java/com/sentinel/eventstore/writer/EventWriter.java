package com.sentinel.eventstore.writer;

import com.sentinel.eventstore.model.SecurityEvent;
import com.sentinel.eventstore.repository.EventRepository;
import com.sentinel.shared.dto.EventDTO;
import com.sentinel.shared.enums.Decision;
import com.sentinel.shared.enums.EventType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

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
     * Maps all 20 fields from EventDTO to preserve complete event information.
     */
    private SecurityEvent mapDtoToEntity(EventDTO dto) {
        SecurityEvent event = SecurityEvent.builder()
                .sessionId(dto.getSessionId())
                .timestampNs(dto.getTimestampNs())
                .eventType(dto.getEventType())
                .userId(dto.getUserId())
                .roles(dto.getRoles())
                .endpoint(dto.getEndpoint())
                .httpMethod(dto.getHttpMethod())
                .sourceIp(dto.getSourceIp())
                .userAgent(dto.getUserAgent())
                .decision(dto.getDecision())
                .policyRuleId(dto.getPolicyRuleId())
                .policyRuleVersion(dto.getPolicyRuleVersion())
                .policyRuleSnapshotId(dto.getPolicyRuleSnapshotId())
                .riskScore(dto.getRiskScore())
                .riskSignals(dto.getRiskSignals())
                .requestContext(dto.getRequestContext())
                .bodyHash(dto.getBodyHash())
                .eventHash(dto.getEventHash())
                .gatewayVersion(dto.getGatewayVersion())
                .ruleMatched(dto.getPolicyRuleId())
                .build();

        return event;
    }
}
