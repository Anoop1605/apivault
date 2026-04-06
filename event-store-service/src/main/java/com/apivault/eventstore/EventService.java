package com.apivault.eventstore;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class EventService {

    @Autowired
    private EventRepository eventRepository;

    // 🔥 SAVE EVENT (HASH CHAIN)
    public Event saveEvent(Event event) {

        event.setTimestamp(Instant.now());

        if (event.getSessionId() == null) {
            event.setSessionId(UUID.randomUUID());
        }

        if (event.getEventType() == null) {
            event.setEventType("REQUEST_RECEIVED");
        }

        event.setGatewayVersion("v1");

        // 🔹 GET LAST EVENT
        Event lastEvent = eventRepository.findTopByOrderByTimestampDesc();

        String previousHash = (lastEvent != null)
                ? lastEvent.getCurrentHash()
                : "GENESIS";

        event.setPreviousHash(previousHash);

        // 🔥 HASH DATA (WITH TIMESTAMP — IMPORTANT FIX)
        String data = previousHash +
                event.getTimestamp().toString() +
                event.getUserId() +
                event.getEndpoint() +
                event.getHttpMethod() +
                event.getSourceIp() +
                event.getDecision() +
                event.getRiskScore();

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
    public List<Event> getEventsByTimeRange(Instant start, Instant end) {
    return eventRepository.findByTimestampBetween(start, end);
}
public List<Event> getEventsByEndpoint(String endpoint) {
    return eventRepository.findByEndpoint(endpoint);
}

    // 🔐 VERIFY CHAIN (FINAL CORRECT VERSION)
    public boolean verifyChain() {

        List<Event> events = eventRepository.findAllByOrderByTimestampAsc();

        for (int i = 0; i < events.size(); i++) {

            Event current = events.get(i);

            String expectedPreviousHash;

            if (i == 0) {
                expectedPreviousHash = "GENESIS";
            } else {
                expectedPreviousHash = events.get(i - 1).getCurrentHash();
            }

            // 🔥 CHECK 1: chain linkage
            if (!current.getPreviousHash().equals(expectedPreviousHash)) {
                return false;
            }

            // 🔥 REBUILD DATA (WITH TIMESTAMP)
            String data = current.getPreviousHash() +
                    current.getTimestamp().toString() +
                    current.getUserId() +
                    current.getEndpoint() +
                    current.getHttpMethod() +
                    current.getSourceIp() +
                    current.getDecision() +
                    current.getRiskScore();

            String recalculatedHash = HashUtil.generateHash(data);

            // 🔥 CHECK 2: hash integrity
            if (!recalculatedHash.equals(current.getCurrentHash())) {
                return false;
            }
        }

        return true;
    }
}