package com.sentinel.eventstore.api;

import com.sentinel.eventstore.model.SecurityEvent;
import com.sentinel.eventstore.writer.EventWriter;
import com.sentinel.shared.dto.EventDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * EventWriteController — accepts events from gateway and stores them.
 * PRD Section 5.5 (FR-ES-07): Event writes SHALL be synchronous.
 * A write failure SHALL result in HTTP 502 to the client.
 */
@Slf4j
@RestController
@RequestMapping("/api/events")
public class EventWriteController {

    private final EventWriter eventWriter;

    public EventWriteController(EventWriter eventWriter) {
        this.eventWriter = eventWriter;
    }

    /**
     * POST /api/events — persist a single event to the event store.
     * Called by gateway filter for every request milestone.
     *
     * @param eventDto The event to persist
     * @return The persisted event
     * @throws RuntimeException on database error → HTTP 502
     */
    @PostMapping
    public ResponseEntity<EventDTO> writeEvent(@RequestBody EventDTO eventDto) {
        try {
            log.debug("Received event: type={}, sessionId={}, timestamp_ns={}",
                    eventDto.getEventType(), eventDto.getSessionId(), eventDto.getTimestampNs());

            SecurityEvent saved = eventWriter.writeEvent(eventDto);

            // Convert back to DTO for response
            EventDTO responseDto = EventDTO.builder()
                    .eventId(saved.getId())
                    .timestampNs(saved.getTimestampNs())
                    .eventType(saved.getEventType())
                    .sessionId(saved.getSessionId())
                    .decision(saved.getDecision())
                    .endpoint(saved.getEndpoint())
                    .sourceIp(saved.getSourceIp())
                    .policyRuleId(saved.getRuleMatched())
                    .build();

            return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
        } catch (Exception e) {
            log.error("Event write failed: {}", e.getMessage(), e);
            // Per PRD §5.5 FR-ES-07: write failure returns HTTP 502
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
        }
    }
}
