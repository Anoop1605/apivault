package com.sentinel.forensics.service;

import com.sentinel.forensics.engine.ReplayHashUtil;
import com.sentinel.forensics.event.EventStoreClient;
import com.sentinel.forensics.hash.HashVerificationUtil;
import com.sentinel.forensics.hash.HashVerificationUtil.VerificationResult;
import com.sentinel.shared.dto.EventDTO;
import com.sentinel.shared.dto.PolicySnapshot;
import com.sentinel.shared.dto.ReplayReport;
import com.sentinel.shared.dto.SessionSummaryDTO;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

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
    public List<VerificationResult> verifyEventHashes(UUID sessionId) {
        List<EventDTO> events = eventStoreClient.fetchEvents(sessionId);
        // Re-compute each event hash and use it as the "stored" value for now.
        // When real persistence is in place, storedHashes come from the DB column event_hash.
        List<String> storedHashes = events.stream()
                .map(ReplayHashUtil::computeEventHash)
                .collect(java.util.stream.Collectors.toList());
        List<VerificationResult> all = HashVerificationUtil.verifyAll(events, storedHashes);
        return HashVerificationUtil.findTampered(all);
    }

    /**
     * Lists session summaries. In a production system this would come from
     * a session registry or event-store index. For now, returns a single
     * summary derived from the mock event store.
     *
     * TODO: Replace with real session registry when persistence is available.
     */
    public List<SessionSummaryDTO> listReplaySessions() {
        // 3 known demo fixture sessions (F01, F02, F03)
        List<UUID> knownSessions = List.of(
                UUID.fromString("aaaaaaaa-0000-0000-0000-000000000001"),
                UUID.fromString("bbbbbbbb-0000-0000-0000-000000000002"),
                UUID.fromString("cccccccc-0000-0000-0000-000000000003")
        );

        List<SessionSummaryDTO> summaries = new java.util.ArrayList<>();
        for (UUID sessionId : knownSessions) {
            List<EventDTO> events = eventStoreClient.fetchEvents(sessionId);
            if (events == null || events.isEmpty()) continue;

            long start = events.stream().mapToLong(EventDTO::getTimestampNs).min().orElse(0);
            long end   = events.stream().mapToLong(EventDTO::getTimestampNs).max().orElse(0);
            PolicySnapshot snapshot = new PolicySnapshot(Collections.emptyList());
            String hash = ReplayHashUtil.computeSessionHash(events, snapshot);

            summaries.add(SessionSummaryDTO.builder()
                    .sessionId(sessionId)
                    .firstSeenNs(start)
                    .lastSeenNs(end)
                    .eventCount(events.size())
                    .hash(hash)
                    .build());
        }
        return summaries;
    }

}
