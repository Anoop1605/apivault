package com.sentinel.eventstore.api;

import com.sentinel.eventstore.query.EventQueryService;
import com.sentinel.shared.dto.EventDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/events")
public class ForensicQueryController {

    private final EventQueryService eventQueryService;

    public ForensicQueryController(EventQueryService eventQueryService) {
        this.eventQueryService = eventQueryService;
    }

    @GetMapping
    public List<EventDTO> getAllEvents() {
        return eventQueryService.getAllEvents();
    }

    // 🔥 NEW: RECENT
    @GetMapping("/recent")
    public List<EventDTO> getRecentEvents(
            @RequestParam(defaultValue = "10") int minutes) {

        return eventQueryService.getRecentEvents(minutes);
    }

    @GetMapping("/paginated")
    public List<EventDTO> getPaginated(
            @RequestParam int page,
            @RequestParam int size) {

        return eventQueryService.getPaginatedEvents(page, size);
    }

    @GetMapping("/sessions")
    public List<UUID> getAllSessions() {
        return eventQueryService.getAllSessions();
    }

    @GetMapping("/sessions/{sessionId}")
    public List<EventDTO> getEventsBySession(@PathVariable UUID sessionId) {
        return eventQueryService.fetchEventsBySession(sessionId);
    }

    @GetMapping("/latest")
    public EventDTO getLatestEvent() {
        return eventQueryService.getLatestEvent();
    }

    @GetMapping("/count")
    public long getEventCount() {
        return eventQueryService.getEventCount();
    }

    @GetMapping("/decision/{decision}")
    public List<EventDTO> getByDecision(@PathVariable String decision) {
        return eventQueryService.getByDecision(decision);
    }

    @GetMapping("/endpoint")
    public List<EventDTO> getByEndpoint(@RequestParam String endpoint) {
        return eventQueryService.getByEndpoint(endpoint);
    }

    @GetMapping("/user/{userId}")
    public List<EventDTO> getByUser(@PathVariable String userId) {
        return eventQueryService.getByUser(userId);
    }

    @GetMapping("/time")
    public List<EventDTO> getByTimeRange(
            @RequestParam long start,
            @RequestParam long end) {

        return eventQueryService.getByTimeRange(start, end);
    }

    @GetMapping("/risk/high")
    public List<EventDTO> getHighRisk(
            @RequestParam(defaultValue = "0.7") double threshold) {

        return eventQueryService.getHighRisk(threshold);
    }

    @GetMapping("/verify-chain")
    public Map<String, Object> verifyChain() {
        return eventQueryService.verifyChain();
    }
}