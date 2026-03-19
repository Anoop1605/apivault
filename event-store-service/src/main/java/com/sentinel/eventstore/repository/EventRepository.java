package com.sentinel.eventstore.repository;

import com.sentinel.eventstore.model.SecurityEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EventRepository extends JpaRepository<SecurityEvent, UUID> {

    List<SecurityEvent> findBySessionIdOrderByTimestampNsAsc(UUID sessionId);

    boolean existsBySessionId(UUID sessionId);
}
