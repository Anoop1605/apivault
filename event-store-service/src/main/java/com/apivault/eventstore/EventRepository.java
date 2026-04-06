package com.apivault.eventstore;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import java.util.List;
import java.time.Instant;

public interface EventRepository extends JpaRepository<Event, UUID> {

    List<Event> findByUserId(String userId);

    List<Event> findByDecision(String decision);

    List<Event> findByRiskScoreGreaterThan(Double score);

    Event findTopByOrderByTimestampDesc();

    List<Event> findAllByOrderByTimestampAsc();

    // 🔥 TIME FILTER
    List<Event> findByTimestampBetween(Instant start, Instant end);
    List<Event> findByEndpoint(String endpoint);
}