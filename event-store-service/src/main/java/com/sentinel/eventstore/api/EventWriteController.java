package com.sentinel.eventstore.api;

import com.sentinel.eventstore.model.SecurityEvent;
import com.sentinel.eventstore.writer.EventWriter;
import com.sentinel.shared.dto.EventDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/events")
public class EventWriteController {

    private final EventWriter eventWriter;

    public EventWriteController(EventWriter eventWriter) {
        this.eventWriter = eventWriter;
    }

    @PostMapping
    public ResponseEntity<?> writeEvent(@RequestBody EventDTO eventDto) {

        // ✅ Validate required fields
        if (eventDto.getSessionId() == null) {
            log.warn("Rejected event: missing sessionId");
            return ResponseEntity.badRequest().body("sessionId is required");
        }

        // ✅ Timestamp fallback — null-safe (timestampNs is boxed Long)
        if (eventDto.getTimestampNs() == null || eventDto.getTimestampNs() == 0L) {
            eventDto.setTimestampNs(System.currentTimeMillis() * 1_000_000);
        }

        try {
            log.info("Incoming event: type={}, sessionId={}, userId={}",
                    eventDto.getEventType(),
                    eventDto.getSessionId(),
                    eventDto.getUserId());

            // ✅ Save event
            SecurityEvent saved = eventWriter.writeEvent(eventDto);

            // ✅ Build response
            EventDTO responseDto = EventDTO.builder()
        .eventId(saved.getId())
        .timestampNs(saved.getTimestampNs())
        .eventType(saved.getEventType())
        .sessionId(saved.getSessionId())
        .userId(saved.getUserId())
        .roles(saved.getRoles())
        .endpoint(saved.getEndpoint())
        .httpMethod(saved.getHttpMethod())
        .sourceIp(saved.getSourceIp())
        .userAgent(saved.getUserAgent())
        .policyRuleId(saved.getPolicyRuleId())
        .policyRuleVersion(saved.getPolicyRuleVersion())
        .policyRuleSnapshotId(saved.getPolicyRuleSnapshotId())
        .riskScore(saved.getRiskScore())
        .riskSignals(saved.getRiskSignals())
        .decision(saved.getDecision())
        .requestContext(saved.getRequestContext())
        .bodyHash(saved.getBodyHash())
        .eventHash(saved.getEventHash())
        .gatewayVersion(saved.getGatewayVersion())
        .build();

            return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);

        } catch (Exception e) {
            log.error("Event write failed", e);
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body("Event write failed");
        }
    }
}