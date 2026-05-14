package com.sentinel.eventstore.repository;

import com.sentinel.eventstore.model.SecurityEvent;
import com.sentinel.shared.enums.Decision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EventRepository extends JpaRepository<SecurityEvent, UUID> {

    List<SecurityEvent> findBySessionIdOrderByTimestampNsAsc(UUID sessionId);

    List<SecurityEvent> findByDecision(Decision decision);

    List<SecurityEvent> findByEndpoint(String endpoint);

    List<SecurityEvent> findByUserId(String userId);

    List<SecurityEvent> findByTimestampNsBetween(long start, long end);

    List<SecurityEvent> findAllByOrderByTimestampNsAsc(); // ✅ used for chain verification

    SecurityEvent findTopByOrderByTimestampNsDesc();

    long countByDecision(Decision decision);

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(DISTINCT e.sessionId) FROM SecurityEvent e")
    long countDistinctSessionIds();

    @org.springframework.data.jpa.repository.Query("SELECT DISTINCT e.sessionId FROM SecurityEvent e")
    List<UUID> findDistinctSessionIds();
}