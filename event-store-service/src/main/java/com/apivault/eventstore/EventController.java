package com.apivault.eventstore;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.time.Instant;

@RestController
@RequestMapping("/events")
public class EventController {

    @Autowired
    private EventService eventService;

    @PostMapping
    public Event createEvent(@RequestBody Event event) {
        return eventService.saveEvent(event);
    }

    @GetMapping
    public List<Event> getAllEvents() {
        return eventService.getAllEvents();
    }

    @GetMapping("/user/{userId}")
    public List<Event> getEventsByUserId(@PathVariable String userId) {
        return eventService.getEventsByUserId(userId);
    }

    @GetMapping("/decision/{decision}")
    public List<Event> getEventsByDecision(@PathVariable String decision) {
        return eventService.getEventsByDecision(decision);
    }

    

    // 🔥 TIME FILTER API
    @GetMapping("/time")
    public List<Event> getEventsByTimeRange(
            @RequestParam String start,
            @RequestParam String end) {

        Instant startTime = Instant.parse(start);
        Instant endTime = Instant.parse(end);

        return eventService.getEventsByTimeRange(startTime, endTime);
    }

    @GetMapping("/verify")
    public String verifyChain() {
        boolean valid = eventService.verifyChain();
        return valid ? "Chain is VALID ✅" : "Chain is TAMPERED ❌";
    }
    @GetMapping("/risk/high")
public List<Event> getHighRiskEvents(@RequestParam Double threshold) {
    return eventService.getHighRiskEvents(threshold);
}
@GetMapping("/endpoint")
public List<Event> getEventsByEndpoint(@RequestParam String endpoint) {
    return eventService.getEventsByEndpoint(endpoint);
}
}
