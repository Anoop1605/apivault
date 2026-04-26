package com.sentinel.eventstore.writer;

import com.sentinel.eventstore.policy.PolicyRuleHistory;
import com.sentinel.eventstore.model.SecurityEvent;
import com.sentinel.eventstore.repository.EventRepository;
import com.sentinel.eventstore.repository.PolicyRuleHistoryRepository;
import com.sentinel.eventstore.util.HashUtil;
import com.sentinel.shared.dto.EventDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@Slf4j
public class EventWriter {

    private final EventRepository eventRepository;
    private final PolicyRuleHistoryRepository historyRepository;

    public EventWriter(EventRepository eventRepository,
                       PolicyRuleHistoryRepository historyRepository) {
        this.eventRepository = eventRepository;
        this.historyRepository = historyRepository;
    }

    public SecurityEvent writeEvent(EventDTO dto) {
        try {

            // 🔗 GET LAST EVENT FOR HASH CHAIN
            SecurityEvent lastEvent = eventRepository.findTopByOrderByTimestampNsDesc();

            String previousHash = (lastEvent != null)
                    ? lastEvent.getEventHash()
                    : "GENESIS";

            // 🔐 BODY HASH
            String bodyHash = (dto.getBodyHash() != null)
                    ? dto.getBodyHash()
                    : HashUtil.sha256(String.valueOf(dto.getSessionId()));

            // 🧾 CREATE POLICY SNAPSHOT (SOURCE OF TRUTH)
            PolicyRuleHistory snapshot = PolicyRuleHistory.builder()
                    .ruleId(dto.getPolicyRuleId())
                    .version(dto.getPolicyRuleVersion())
                    .fullConditions(
                            dto.getPolicyRuleSnapshot() != null
                                    ? dto.getPolicyRuleSnapshot()
                                    : "{}"
                    )
                    .decision(dto.getDecision())
                    .activatedAt(Instant.now())
                    .activatedBy("system")
                    .build();

            // 💾 SAVE SNAPSHOT
            PolicyRuleHistory savedSnapshot = historyRepository.save(snapshot);

            // 📦 CREATE EVENT
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

                    // ✅ IMPORTANT FIX (NO NULL NOW)
                    .policyRuleSnapshot(savedSnapshot.getFullConditions())
                    .policyRuleSnapshotId(savedSnapshot.getSnapshotId())

                    .riskScore(dto.getRiskScore())
                    .riskSignals(dto.getRiskSignals())
                    .requestContext(dto.getRequestContext())
                    .bodyHash(bodyHash)
                    .previousHash(previousHash)
                    .gatewayVersion(dto.getGatewayVersion())
                    .ruleMatched(dto.getPolicyRuleId())
                    .build();

            // 🔐 GENERATE EVENT HASH (CHAINING)
            String eventHash = HashUtil.generateHash(event);
            event.setEventHash(eventHash);

            // 💾 SAVE EVENT
            return eventRepository.save(event);

        } catch (Exception e) {
            log.error("Error writing event", e);
            throw new RuntimeException(e);
        }
    }
}