package com.sentinel.eventstore.writer;

import com.sentinel.eventstore.model.SecurityEvent;
import com.sentinel.eventstore.repository.EventRepository;
import com.sentinel.eventstore.util.HashUtil;
import com.sentinel.shared.dto.EventDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EventWriter {

    private final EventRepository eventRepository;

    public EventWriter(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public SecurityEvent writeEvent(EventDTO dto) {
        try {

            // 🔥 STEP 1: Get last event (for chain)
            SecurityEvent lastEvent = eventRepository.findTopByOrderByTimestampNsDesc();

            String previousHash = (lastEvent != null)
                    ? lastEvent.getEventHash()
                    : "GENESIS";

            // 🔥 STEP 2: Build string for hashing
            String dataToHash =
                    String.valueOf(dto.getSessionId()) +
                    dto.getTimestampNs() +
                    dto.getEventType() +
                    dto.getUserId() +
                    dto.getEndpoint() +
                    dto.getSourceIp();

            // 🔥 STEP 3: Generate hashes
            String bodyHash = (dto.getBodyHash() != null)
                    ? dto.getBodyHash()
                    : HashUtil.sha256(dataToHash);

            String eventHash = HashUtil.sha256(dataToHash + previousHash);

            // 🔥 STEP 4: Build entity
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
                    .bodyHash(bodyHash)
                    .previousHash(previousHash)   // 🔥 NEW
                    .eventHash(eventHash)         // 🔥 GENERATED
                    .gatewayVersion(dto.getGatewayVersion())
                    .ruleMatched(dto.getPolicyRuleId())
                    .build();

            SecurityEvent saved = eventRepository.save(event);

            log.info("Event saved: id={}, hash={}, prevHash={}",
                    saved.getId(),
                    saved.getEventHash(),
                    saved.getPreviousHash());

            return saved;

        } catch (Exception e) {
            log.error("Write failed", e);
            throw new RuntimeException(e);
        }
    }
}