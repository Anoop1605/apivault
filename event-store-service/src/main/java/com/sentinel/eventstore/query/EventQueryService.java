package com.sentinel.eventstore.query;

import com.sentinel.eventstore.model.SecurityEvent;
import com.sentinel.eventstore.repository.EventRepository;
import com.sentinel.eventstore.util.HashUtil;
import com.sentinel.shared.dto.EventDTO;
import com.sentinel.shared.enums.Decision;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class EventQueryService {

    private final EventRepository eventRepository;

    public EventQueryService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    // =========================
    // GET ALL EVENTS
    // =========================
    public List<EventDTO> getAllEvents() {
        log.info("Fetching all events");
        return eventRepository.findAllByOrderByTimestampNsAsc()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // =========================
    // 🔥 NEW: RECENT EVENTS
    // =========================
    public List<EventDTO> getRecentEvents(int minutes) {

        if (minutes <= 0) {
            throw new IllegalArgumentException("Minutes must be > 0");
        }

        long now = System.currentTimeMillis() * 1_000_000;
        long past = now - (minutes * 60L * 1_000_000_000L);

        log.info("Fetching events from last {} minutes", minutes);

        return eventRepository.findByTimestampNsBetween(past, now)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // =========================
    // PAGINATION
    // =========================
    public List<EventDTO> getPaginatedEvents(int page, int size) {

        if (page < 0 || size <= 0) {
            throw new IllegalArgumentException("Invalid pagination params");
        }

        Pageable pageable = PageRequest.of(page, size);

        return eventRepository.findAll(pageable)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // =========================
    // SESSIONS LIST
    // =========================
    public List<UUID> getAllSessions() {
        return eventRepository.findAll()
                .stream()
                .map(SecurityEvent::getSessionId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }

    // =========================
    // LATEST EVENT
    // =========================
    public EventDTO getLatestEvent() {
        return eventRepository.findAllByOrderByTimestampNsAsc()
                .stream()
                .reduce((first, second) -> second)
                .map(this::toDTO)
                .orElse(null);
    }

    // =========================
    // COUNT
    // =========================
    public long getEventCount() {
        return eventRepository.count();
    }

    // =========================
    // SESSION TIMELINE
    // =========================
    public List<EventDTO> fetchEventsBySession(UUID sessionId) {
        return eventRepository
                .findBySessionIdOrderByTimestampNsAsc(sessionId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // =========================
    // FILTER: decision
    // =========================
    public List<EventDTO> getByDecision(String decision) {
        try {
            Decision dec = Decision.valueOf(decision.toUpperCase());
            return eventRepository.findByDecision(dec)
                    .stream()
                    .map(this::toDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return List.of();
        }
    }

    // =========================
    // FILTER: endpoint
    // =========================
    public List<EventDTO> getByEndpoint(String endpoint) {
        return eventRepository.findByEndpoint(endpoint)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // =========================
    // FILTER: user
    // =========================
    public List<EventDTO> getByUser(String userId) {
        return eventRepository.findByUserId(userId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // =========================
    // FILTER: time
    // =========================
    public List<EventDTO> getByTimeRange(long start, long end) {

        if (start > end) {
            throw new IllegalArgumentException("Start must be <= end");
        }

        return eventRepository.findByTimestampNsBetween(start, end)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // =========================
    // HIGH RISK
    // =========================
    public List<EventDTO> getHighRisk(double threshold) {
        return eventRepository.findAll()
                .stream()
                .filter(e -> e.getRiskScore() != null && e.getRiskScore() >= threshold)
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // =========================
    // VERIFY CHAIN
    // =========================
    public Map<String, Object> verifyChain() {

        List<SecurityEvent> events = eventRepository.findAllByOrderByTimestampNsAsc();

        if (events.isEmpty()) {
            return Map.of("valid", true, "message", "No events found");
        }

        for (int i = 0; i < events.size(); i++) {

            SecurityEvent curr = events.get(i);

            String recalculatedHash = HashUtil.generateHash(curr);

            if (!recalculatedHash.equals(curr.getEventHash())) {
                return Map.of("valid", false, "brokenAtEventId", curr.getId());
            }

            if (i > 0) {
                SecurityEvent prev = events.get(i - 1);

                if (!curr.getPreviousHash().equals(prev.getEventHash())) {
                    return Map.of("valid", false, "brokenAtEventId", curr.getId());
                }
            }
        }

        return Map.of("valid", true);
    }

    // DTO
    private EventDTO toDTO(SecurityEvent event) {
        return EventDTO.builder()
                .eventId(event.getId())
                .sessionId(event.getSessionId())
                .timestampNs(event.getTimestampNs())
                .eventType(event.getEventType())
                .userId(event.getUserId())
                .roles(event.getRoles())
                .endpoint(event.getEndpoint())
                .httpMethod(event.getHttpMethod())
                .sourceIp(event.getSourceIp())
                .userAgent(event.getUserAgent())
                .policyRuleId(event.getPolicyRuleId())
                .policyRuleVersion(event.getPolicyRuleVersion())
                .policyRuleSnapshotId(event.getPolicyRuleSnapshotId())
                .riskScore(event.getRiskScore())
                .riskSignals(event.getRiskSignals())
                .decision(event.getDecision())
                .requestContext(event.getRequestContext())
                .bodyHash(event.getBodyHash())
                .eventHash(event.getEventHash())
                .gatewayVersion(event.getGatewayVersion())
                .build();
    }
}