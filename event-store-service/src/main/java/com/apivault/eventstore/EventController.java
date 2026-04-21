package com.apivault.eventstore;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.apivault.eventstore.dto.EventDTO;

import java.util.List;
import java.util.UUID;
import java.time.Instant;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/events")
public class EventController {

    @Autowired
    private EventService eventService;

    // ✅ CREATE EVENT (auto capture request details)
    @PostMapping
    public Event createEvent(@RequestBody Event event,
                             HttpServletRequest request) {

        event.setEndpoint(request.getRequestURI());
        event.setHttpMethod(request.getMethod());
        event.setSourceIp(request.getRemoteAddr());

        return eventService.saveEvent(event);
    }

    // ✅ GET ALL EVENTS
    @GetMapping
    public List<Event> getAllEvents() {
        return eventService.getAllEvents();
    }

    // ✅ FILTER BY USER
    @GetMapping("/user/{userId}")
    public List<Event> getEventsByUserId(@PathVariable String userId) {
        return eventService.getEventsByUserId(userId);
    }

    // ✅ FILTER BY DECISION
    @GetMapping("/decision/{decision}")
    public List<Event> getEventsByDecision(@PathVariable String decision) {
        return eventService.getEventsByDecision(decision);
    }

    // ✅ FILTER BY TIME RANGE (FIXED FOR NANOSECONDS)
    @GetMapping("/time")
    public List<Event> getEventsByTimeRange(
            @RequestParam String start,
            @RequestParam String end) {

        long startNs = Instant.parse(start).toEpochMilli() * 1_000_000;
        long endNs = Instant.parse(end).toEpochMilli() * 1_000_000;

        return eventService.getEventsByTimeRange(startNs, endNs);
    }

    // ✅ VERIFY HASH CHAIN
    @GetMapping("/verify")
    public String verifyChain() {
        boolean valid = eventService.verifyChain();
        return valid ? "Chain is VALID ✅" : "Chain is TAMPERED ❌";
    }

    // ✅ HIGH RISK EVENTS
    @GetMapping("/risk/high")
    public List<Event> getHighRiskEvents(@RequestParam Double threshold) {
        return eventService.getHighRiskEvents(threshold);
    }

    // ✅ FILTER BY ENDPOINT
    @GetMapping("/endpoint")
    public List<Event> getEventsByEndpoint(@RequestParam String endpoint) {
        return eventService.getEventsByEndpoint(endpoint);
    }

    // ✅ SESSION TIMELINE
    @GetMapping("/sessions/{sessionId}/timeline")
    public List<Event> getSessionTimeline(@PathVariable String sessionId) {
        return eventService.getSessionTimeline(sessionId);
    }

    // ✅ INTERNAL API (FOR GATEWAY / OTHER SERVICES)
    @PostMapping("/internal/events")
    public ResponseEntity<?> receiveEvent(@RequestBody EventDTO dto,
                                          HttpServletRequest request) {

        Event event = new Event();

        event.setEventType(dto.eventType != null ? dto.eventType : "REQUEST_RECEIVED");

        event.setEndpoint(
                dto.endpoint != null ? dto.endpoint : request.getRequestURI()
        );

        event.setHttpMethod(
                dto.httpMethod != null ? dto.httpMethod : request.getMethod()
        );

        event.setSourceIp(request.getRemoteAddr());

        event.setUserId(dto.userId);
        event.setDecision(dto.decision);
        event.setRiskScore(dto.riskScore);

        eventService.saveEvent(event);

        return ResponseEntity.ok("Event stored successfully");
    }
}