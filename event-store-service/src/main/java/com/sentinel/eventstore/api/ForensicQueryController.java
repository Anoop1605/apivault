package com.sentinel.eventstore.api;

import com.sentinel.eventstore.query.EventQueryService;
import com.sentinel.shared.dto.EventDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/events")
public class ForensicQueryController {

    private final EventQueryService eventQueryService;

    public ForensicQueryController(EventQueryService eventQueryService) {
        this.eventQueryService = eventQueryService;
    }

    /**
     * GET /events/sessions/{sessionId}
     * Returns all events for the given session, ordered by timestamp ascending.
     * Called by forensics-replay-service via RestEventStoreClient.
     */
    @GetMapping("/sessions/{sessionId}")
    public ResponseEntity<List<EventDTO>> getEventsBySession(@PathVariable UUID sessionId) {
        List<EventDTO> events = eventQueryService.fetchEventsBySession(sessionId);
        if (events.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(events);
    }
}
