package com.sentinel.eventstore.writer;

import com.sentinel.eventstore.model.SecurityEvent;
import com.sentinel.eventstore.repository.EventRepository;
import com.sentinel.eventstore.util.HashUtil;
import com.sentinel.shared.dto.EventDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * EventWriter — writes security events to the append-only event store.
 * Implements cryptographic hash chaining (PRD Section 5.5).
 */
@Slf4j
@Service
public class EventWriter {

    private final EventRepository eventRepository;
    private final com.sentinel.eventstore.repository.AlertRepository alertRepository;

    public EventWriter(EventRepository eventRepository, 
                       com.sentinel.eventstore.repository.AlertRepository alertRepository) {
        this.eventRepository = eventRepository;
        this.alertRepository = alertRepository;
    }

    /**
     * Persist a single event to the event store with hash chaining.
     */
    @Transactional
    public SecurityEvent writeEvent(EventDTO eventDto) {
        try {
            SecurityEvent event = mapDtoToEntity(eventDto);
            
            // 1. Get the previous hash from the latest event in the DB
            SecurityEvent latest = eventRepository.findTopByOrderByTimestampNsDesc().orElse(null);
            String prevHash = (latest != null) ? latest.getEventHash() : "0000000000000000000000000000000000000000000000000000000000000000";
            event.setPreviousHash(prevHash);
            
            // 2. Generate the hash for the current event
            String currentHash = HashUtil.generateHash(event);
            event.setEventHash(currentHash);

            SecurityEvent saved = eventRepository.save(event);
            
            // 3. Automatically generate an alert for high-risk decisions
            if (com.sentinel.shared.enums.Decision.BLOCK.equals(saved.getDecision()) || 
                com.sentinel.shared.enums.Decision.DENY.equals(saved.getDecision())) {
                
                com.sentinel.eventstore.model.Alert alert = com.sentinel.eventstore.model.Alert.builder()
                        .sessionId(saved.getSessionId())
                        .userId(saved.getUserId())
                        .endpoint(saved.getEndpoint())
                        .reason(saved.getPolicyRuleId() != null ? saved.getPolicyRuleId() : "Manual Block")
                        .riskScore(saved.getRiskScore() != null ? saved.getRiskScore() : 1.0)
                        .timestampNs(saved.getTimestampNs())
                        .status(com.sentinel.eventstore.model.Alert.AlertStatus.OPEN)
                        .build();
                
                alertRepository.save(alert);
                log.warn("🚨 ALERT GENERATED: eventId={}, reason={}", saved.getId(), alert.getReason());
            }

            log.info("Event persisted: eventId={}, type={}, sessionId={}, hash={}",
                    saved.getId(), saved.getEventType(), saved.getSessionId(), saved.getEventHash().substring(0, 8));
            return saved;
        } catch (Exception e) {
            log.error("Failed to write event: {}", e.getMessage(), e);
            throw new RuntimeException("Event write failed: " + e.getMessage(), e);
        }
    }

    private SecurityEvent mapDtoToEntity(EventDTO dto) {
        SecurityEvent event = new SecurityEvent();
        event.setSessionId(dto.getSessionId());
        event.setTimestampNs(dto.getTimestampNs() != null ? dto.getTimestampNs() : System.currentTimeMillis() * 1_000_000L);
        event.setEventType(dto.getEventType());
        event.setUserId(dto.getUserId());
        event.setRoles(dto.getRoles());
        event.setEndpoint(dto.getEndpoint());
        event.setHttpMethod(dto.getHttpMethod());
        event.setSourceIp(dto.getSourceIp());
        event.setUserAgent(dto.getUserAgent());
        event.setDecision(dto.getDecision());
        event.setPolicyRuleId(dto.getPolicyRuleId());
        event.setPolicyRuleVersion(dto.getPolicyRuleVersion());
        event.setPolicyRuleSnapshot(dto.getPolicyRuleSnapshot());
        event.setPolicyRuleSnapshotId(dto.getPolicyRuleSnapshotId());
        event.setRiskScore(dto.getRiskScore());
        event.setRiskSignals(dto.getRiskSignals());
        event.setRequestContext(dto.getRequestContext());
        event.setBodyHash(dto.getBodyHash());
        event.setGatewayVersion(dto.getGatewayVersion());
        event.setRuleMatched(dto.getPolicyRuleId()); // ruleMatched is used for searching
        return event;
    }
}
