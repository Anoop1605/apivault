package com.sentinel.eventstore.repository;

import com.sentinel.eventstore.model.SecurityEvent;
import com.sentinel.shared.enums.Decision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EventRepository extends JpaRepository<SecurityEvent, UUID> {

    List<SecurityEvent> findBySessionIdOrderByTimestampNsAsc(UUID sessionId);

    boolean existsBySessionId(UUID sessionId);

    Optional<SecurityEvent> findTopByOrderByTimestampNsDesc();

    long countByDecision(Decision decision);

    @Query("SELECT COUNT(DISTINCT e.sessionId) FROM SecurityEvent e")
    long countDistinctSessionIds();

    /**
     * Fetch all distinct session IDs from the database.
     * Used for dynamic session discovery in forensics service.
     */
    @Query("SELECT DISTINCT e.sessionId FROM SecurityEvent e ORDER BY e.sessionId")
    List<UUID> findAllDistinctSessionIds();
}
