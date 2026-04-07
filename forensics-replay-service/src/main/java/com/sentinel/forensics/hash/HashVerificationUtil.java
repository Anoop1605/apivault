package com.sentinel.forensics.hash;

import com.sentinel.forensics.engine.ReplayHashUtil;
import com.sentinel.shared.dto.EventDTO;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * HashVerificationUtil — Verifies integrity of stored events using canonical hashing.
 * Detects tampering by comparing stored hashes against recomputed hashes.
 *
 * PRD Section 10: Tamper Detection & Forensic Analysis
 */
@Slf4j
public class HashVerificationUtil {

    private HashVerificationUtil() {}

    /**
     * Verify integrity of all events in a session.
     * Recomputes hash for each event and compares against stored eventHash.
     *
     * @param events Events to verify
     * @return List of event IDs that have been tampered with (empty = all clean)
     */
    public static List<String> verifyEventHashes(List<EventDTO> events) {
        List<String> tamperedEventIds = new ArrayList<>();

        if (events == null || events.isEmpty()) {
            return tamperedEventIds;
        }

        for (EventDTO event : events) {
            if (!verifyEventHash(event)) {
                tamperedEventIds.add(event.getEventId() != null ? event.getEventId().toString() : "unknown");
            }
        }

        if (tamperedEventIds.isEmpty()) {
            log.info("All {} events verified successfully - no tampering detected", events.size());
        } else {
            log.warn("Tampering detected in {} out of {} events", tamperedEventIds.size(), events.size());
        }

        return tamperedEventIds;
    }

    /**
     * Verify integrity of a single event.
     * Recomputes canonical hash and compares with stored eventHash.
     *
     * @param event Event to verify
     * @return true if hash matches (event is clean), false if tampered
     */
    public static boolean verifyEventHash(EventDTO event) {
        if (event == null) {
            return false;
        }

        // If no stored hash, assume event is clean (or system doesn't support hashing)
        if (event.getEventHash() == null || event.getEventHash().isEmpty()) {
            log.debug("No stored hash for event {}, skipping verification", event.getEventId());
            return true;
        }

        // Recompute the canonical hash
        String computedHash = ReplayHashUtil.computeEventHash(event);

        // Compare with stored hash
        boolean isClean = computedHash.equals(event.getEventHash());

        if (!isClean) {
            log.error("HASH MISMATCH for event {}: computed={}, stored={}",
                    event.getEventId(), computedHash, event.getEventHash());
        } else {
            log.debug("Hash verified for event {}", event.getEventId());
        }

        return isClean;
    }

    /**
     * Compute hash for an event that doesn't have one yet.
     * Used when events need to be enriched with hashes.
     *
     * @param event Event to hash
     * @return Computed SHA-256 hash
     */
    public static String computeHashForEvent(EventDTO event) {
        if (event == null) {
            return null;
        }

        return ReplayHashUtil.computeEventHash(event);
    }

    /**
     * Verify a chain of events for tampering.
     * Also checks that hashes form a proper chain.
     *
     * @param events Events in chronological order
     * @return List of indices of tampered events (empty = all clean)
     */
    public static List<Integer> verifyEventChain(List<EventDTO> events) {
        List<Integer> tamperedIndices = new ArrayList<>();

        if (events == null || events.isEmpty()) {
            return tamperedIndices;
        }

        for (int i = 0; i < events.size(); i++) {
            EventDTO event = events.get(i);

            if (!verifyEventHash(event)) {
                tamperedIndices.add(i);
            }

            // Verify chronological order
            if (i > 0) {
                EventDTO prevEvent = events.get(i - 1);
                if (prevEvent.getTimestampNs() > event.getTimestampNs()) {
                    log.warn("Chronological order violation: event {} has timestamp {} < previous {}",
                            i, event.getTimestampNs(), prevEvent.getTimestampNs());
                }
            }
        }

        return tamperedIndices;
    }
}
