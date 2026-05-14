package com.sentinel.forensics.service;

import com.sentinel.forensics.engine.ReplayHashUtil;
import com.sentinel.forensics.event.EventStoreClient;
import com.sentinel.forensics.hash.HashVerificationUtil;
import com.sentinel.shared.dto.EventDTO;
import com.sentinel.shared.dto.PolicySnapshot;
import com.sentinel.shared.dto.ReplayReport;
import com.sentinel.shared.dto.SessionSummaryDTO;
import com.sentinel.shared.enums.Decision;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class ForensicQueryService {

    private final ReplayService replayService;
    private final EventStoreClient eventStoreClient;

    public ForensicQueryService(ReplayService replayService, EventStoreClient eventStoreClient) {
        this.replayService = replayService;
        this.eventStoreClient = eventStoreClient;
    }

    /**
     * Returns the full replay report for a given session.
     */
    public ReplayReport getReplayReportBySessionId(UUID sessionId) {
        return replayService.replaySession(sessionId);
    }

    /**
     * Verifies the tamper-evident hash for a replay session.
     * Re-computes the hash from raw events and compares to the provided hash.
     */
    public boolean verifyReplayHash(UUID sessionId, String expectedHash) {
        List<EventDTO> events = eventStoreClient.fetchEvents(sessionId);
        PolicySnapshot snapshot = replayService.buildPolicySnapshot(events);
        String actualHash = ReplayHashUtil.computeSessionHash(events, snapshot);
        return actualHash.equals(expectedHash);
    }

    /**
     * Verifies canonical SHA-256 hashes for all events in a session.
     * Re-computes each event hash and compares to the stored value.
     * Returns only tampered events (AC-08).
     *
     * NOTE: Until the event store persists event_hash per event, the stored
     * hash is re-computed on the fly and compared to itself — so no tampering
     * will be detected in the mock setup. This method is ready for when the
     * real event store returns stored hashes.
     */
    public List<String> verifyEventHashes(UUID sessionId) {
        List<EventDTO> events = eventStoreClient.fetchEvents(sessionId);
        List<String> storedHashes = events.stream()
                .map(EventDTO::getEventHash)
                .collect(java.util.stream.Collectors.toList());
        com.sentinel.shared.dto.VerificationResult result = HashVerificationUtil.verifyAll(events, storedHashes);
        return HashVerificationUtil.findTampered(result);
    }

    /**
     * Lists session summaries dynamically by querying all distinct session IDs from the event store.
     * This replaces the old hardcoded demo session approach with real dynamic data fetching.
     */
    public List<SessionSummaryDTO> listReplaySessions() {
        // Fetch ALL session IDs from the event store dynamically
        List<UUID> allSessions = eventStoreClient.fetchAllSessionIds();
        
        List<SessionSummaryDTO> summaries = new java.util.ArrayList<>();
        for (UUID sessionId : allSessions) {
            List<EventDTO> events = eventStoreClient.fetchEvents(sessionId);
            if (events == null || events.isEmpty())
                continue;

            long start = events.stream().mapToLong(EventDTO::getTimestampNs).min().orElse(0);
            long end = events.stream().mapToLong(EventDTO::getTimestampNs).max().orElse(0);
            
            // Calculate metrics dynamically
            long blockedCount = events.stream()
                    .filter(e -> e.getDecision() == Decision.BLOCK || e.getDecision() == Decision.DENY)
                    .count();
            double maxRiskScore = events.stream()
                    .map(EventDTO::getRiskScore)
                    .filter(java.util.Objects::nonNull)
                    .max(Double::compareTo)
                    .orElse(0.0);

            PolicySnapshot snapshot = new PolicySnapshot(Collections.emptyList());
            String hash = ReplayHashUtil.computeSessionHash(events, snapshot);

            summaries.add(SessionSummaryDTO.builder()
                    .sessionId(sessionId)
                    .firstSeenNs(start)
                    .lastSeenNs(end)
                    .eventCount(events.size())
                    .hash(hash)
                    .maxRiskScore(maxRiskScore)
                    .denyCount((int) blockedCount)
                    .flagged(maxRiskScore >= 0.7 || blockedCount > 0)
                    .build());
        }
        return summaries;
    }

}
