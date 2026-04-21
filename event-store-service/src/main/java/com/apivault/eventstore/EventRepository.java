package com.apivault.eventstore;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import java.util.List;
import java.time.Instant;

public interface EventRepository extends JpaRepository<Event, UUID> {

    List<Event> findByUserId(String userId);

    List<Event> findByDecision(String decision);

    List<Event> findByRiskScoreGreaterThan(Double score);

    Event findTopByOrderByTimestampNsDesc();

    List<Event> findAllByOrderByTimestampNsAsc();

    //  TIME FILTER
    List<Event> findByTimestampNsBetween(Long startNs, Long endNs);
    List<Event> findByEndpoint(String endpoint);
    List<Event> findBySessionIdOrderByTimestampNsAsc(UUID sessionId);
}