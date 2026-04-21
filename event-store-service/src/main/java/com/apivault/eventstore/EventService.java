package com.apivault.eventstore;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class EventService {

    @Autowired
    private EventRepository eventRepository;

    // 🔹 COMMON HASH BUILDER (PRD CORRECT)
    private String buildData(Event e) {
        return String.valueOf(e.getPreviousHash()) +
               String.valueOf(e.getTimestampNs()) +
               String.valueOf(e.getUserId()) +
               String.valueOf(e.getEndpoint()) +
               String.valueOf(e.getHttpMethod()) +
               String.valueOf(e.getSourceIp()) +
               String.valueOf(e.getDecision()) +
               String.valueOf(e.getRiskScore());
    }

    // 🔹 SAVE EVENT (HASH CHAIN)
    public Event saveEvent(Event event) {

        // ✅ PRD: timestamp as long (nanoseconds style)
        event.setTimestampNs(System.currentTimeMillis() * 1_000_000);

        // ✅ sessionId
        if (event.getSessionId() == null) {
            event.setSessionId(UUID.randomUUID());
        }

        // ✅ default values
        if (event.getEventType() == null) {
            event.setEventType("REQUEST_RECEIVED");
        }

        event.setGatewayVersion("v1");

        // 🔹 GET LAST EVENT
        Event lastEvent = eventRepository.findTopByOrderByTimestampNsDesc();

        String previousHash = (lastEvent != null)
                ? lastEvent.getCurrentHash()
                : "GENESIS";

        event.setPreviousHash(previousHash);

        // 🔹 BUILD HASH DATA
        String data = buildData(event);

        String currentHash = HashUtil.generateHash(data);

        event.setCurrentHash(currentHash);

        return eventRepository.save(event);
    }

    // 🔍 GET ALL EVENTS
    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    // 🔍 FILTER BY USER
    public List<Event> getEventsByUserId(String userId) {
        return eventRepository.findByUserId(userId);
    }

    // 🔍 FILTER BY DECISION
    public List<Event> getEventsByDecision(String decision) {
        return eventRepository.findByDecision(decision);
    }

    // 🔍 FILTER HIGH RISK
    public List<Event> getHighRiskEvents(Double score) {
        return eventRepository.findByRiskScoreGreaterThan(score);
    }

    // 🔍 FILTER BY ENDPOINT
    public List<Event> getEventsByEndpoint(String endpoint) {
        return eventRepository.findByEndpoint(endpoint);
    }

    // 🔍 SESSION TIMELINE
    public List<Event> getSessionTimeline(String sessionId) {
        return eventRepository.findBySessionIdOrderByTimestampNsAsc(UUID.fromString(sessionId));
    }

    // 🔍 TIME RANGE
    public List<Event> getEventsByTimeRange(long startNs, long endNs) {
        return eventRepository.findByTimestampNsBetween(startNs, endNs);
    }

    // 🔐 VERIFY CHAIN
    public boolean verifyChain() {

        List<Event> events = eventRepository.findAllByOrderByTimestampNsAsc();

        for (int i = 0; i < events.size(); i++) {

            Event current = events.get(i);

            String expectedPreviousHash;

            if (i == 0) {
                expectedPreviousHash = "GENESIS";
            } else {
                expectedPreviousHash = events.get(i - 1).getCurrentHash();
            }

            // 🔹 CHECK 1: chain linkage
            if (!current.getPreviousHash().equals(expectedPreviousHash)) {
                return false;
            }

            // 🔹 REBUILD HASH
            String data = buildData(current);
            String recalculatedHash = HashUtil.generateHash(data);

            // 🔹 CHECK 2: hash integrity
            if (!recalculatedHash.equals(current.getCurrentHash())) {
                return false;
            }
        }

        return true;
    }
}