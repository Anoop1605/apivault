package com.sentinel.eventstore.writer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.sentinel.eventstore.model.SecurityEvent;
import com.sentinel.eventstore.repository.EventRepository;
import com.sentinel.eventstore.util.CanonicalHasher;
import com.sentinel.shared.dto.EventDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

@Slf4j
@Service
public class EventWriter {

    private final EventRepository eventRepository;
    private final CanonicalHasher canonicalHasher;

    public EventWriter(EventRepository eventRepository, CanonicalHasher canonicalHasher) {
        this.eventRepository = eventRepository;
        this.canonicalHasher = canonicalHasher;
    }

    public SecurityEvent writeEvent(EventDTO eventDto) {
        try {
            // 1. Fetch Previous Hash for Chaining (Global Chain)
            SecurityEvent latest = eventRepository.findTopByOrderByTimestampNsDesc();
            String prevHash = (latest != null) ? latest.getCurrentHash() : "GENESIS";

            // 2. Map DTO to Entity
            SecurityEvent event = mapDtoToEntity(eventDto);
            event.setPreviousHash(prevHash);
            
            // 3. Generate Canonical Hash using dedicated utility (PRD §5.5)
            String currentHash = canonicalHasher.calculateHash(event);
            event.setCurrentHash(currentHash);

            SecurityEvent saved = eventRepository.save(event);
            log.info("Event Chain Secured: eventId={}, prev={}, current={}",
                    saved.getId(), 
                    prevHash.equals("GENESIS") ? "GENESIS" : prevHash.substring(0, 8), 
                    currentHash.substring(0, 8));
            return saved;
        } catch (Exception e) {
            log.error("Failed to write event: {}", e.getMessage(), e);
            throw new RuntimeException("Event write failed: " + e.getMessage(), e);
        }
    }

    private SecurityEvent mapDtoToEntity(EventDTO dto) {
        SecurityEvent event = new SecurityEvent();
        event.setId(dto.getEventId() != null ? dto.getEventId() : UUID.randomUUID());
        event.setSessionId(dto.getSessionId());
        event.setUserId(dto.getUserId() != null ? dto.getUserId() : "anonymous");
        
        long tsNs = dto.getTimestampNs() != null ? dto.getTimestampNs() : System.nanoTime();
        event.setTimestampNs(tsNs);
        event.setTimestamp(new java.util.Date());
        
        event.setEventType(dto.getEventType());
        event.setEndpoint(dto.getEndpoint());
        event.setHttpMethod(dto.getHttpMethod() != null ? dto.getHttpMethod() : "GET");
        event.setSourceIp(dto.getSourceIp() != null ? dto.getSourceIp() : "127.0.0.1");
        
        // Handle Decision defaults
        com.sentinel.shared.enums.Decision decision = dto.getDecision();
        if (decision == null) {
            if (dto.getEventType() == com.sentinel.shared.enums.EventType.REQUEST_RECEIVED) {
                decision = com.sentinel.shared.enums.Decision.NONE;
            } else {
                decision = com.sentinel.shared.enums.Decision.NOT_APPLICABLE;
            }
        }
        event.setDecision(decision);
        
        event.setRiskScore(dto.getRiskScore() != null ? dto.getRiskScore() : 0.0);
        event.setGatewayVersion(dto.getGatewayVersion() != null ? dto.getGatewayVersion() : "2.0.0");
        
        // Map new PRD fields
        event.setPolicyRuleId(dto.getPolicyRuleId());
        event.setPolicyRuleVersion(dto.getPolicyRuleVersion());
        event.setPolicyRuleSnapshotId(dto.getPolicyRuleSnapshotId());
        event.setBodyHash(dto.getBodyHash());
        
        return event;
    }
}
