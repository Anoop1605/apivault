package com.sentinel.forensics.hash;

import com.sentinel.forensics.engine.ReplayHashUtil;
import com.sentinel.shared.dto.EventDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Tamper verification utility — PRD §10.3 / AC-08.
 *
 * Re-computes the canonical SHA-256 hash for each event and compares
 * it to the stored event_hash. Any mismatch means the event was tampered.
 *
 * Used by:
 *   GET /forensics/verify-hashes?sessionId={id}
 *   CLI: java -jar sentinel-tools.jar verify-hashes
 */
public class HashVerificationUtil {

    private HashVerificationUtil() {}

    /**
     * Result of a single event hash verification.
     */
    public static class VerificationResult {
        private final UUID eventId;
        private final boolean tampered;
        private final String storedHash;
        private final String recomputedHash;

        public VerificationResult(UUID eventId, boolean tampered,
                                  String storedHash, String recomputedHash) {
            this.eventId = eventId;
            this.tampered = tampered;
            this.storedHash = storedHash;
            this.recomputedHash = recomputedHash;
        }

        public UUID getEventId() { return eventId; }
        public boolean isTampered() { return tampered; }
        public String getStoredHash() { return storedHash; }
        public String getRecomputedHash() { return recomputedHash; }
    }

    /**
     * Verifies the hash of a single event.
     * The event must carry its stored hash in event.getBodyHash() is NOT used here —
     * the stored hash must be passed separately (fetched from the event store).
     *
     * @param event      the event to verify
     * @param storedHash the hash value stored in the event_hash column
     * @return VerificationResult indicating whether the event was tampered
     */
    public static VerificationResult verifyEvent(EventDTO event, String storedHash) {
        String recomputed = ReplayHashUtil.computeEventHash(event);
        boolean tampered = !recomputed.equals(storedHash);
        return new VerificationResult(event.getEventId(), tampered, storedHash, recomputed);
    }

    /**
     * Verifies a list of events against their stored hashes.
     * Events and storedHashes must be in the same order.
     *
     * @param events       events to verify
     * @param storedHashes parallel list of hashes from the event store
     * @return list of results where isTampered() == true are suspect events
     */
    public static List<VerificationResult> verifyAll(List<EventDTO> events,
                                                      List<String> storedHashes) {
        if (events.size() != storedHashes.size()) {
            throw new IllegalArgumentException(
                "events and storedHashes lists must be the same size");
        }

        List<VerificationResult> results = new ArrayList<>();
        for (int i = 0; i < events.size(); i++) {
            results.add(verifyEvent(events.get(i), storedHashes.get(i)));
        }
        return results;
    }

    /**
     * Returns only the tampered events from a verification run.
     */
    public static List<VerificationResult> findTampered(List<VerificationResult> results) {
        List<VerificationResult> tampered = new ArrayList<>();
        for (VerificationResult r : results) {
            if (r.isTampered()) tampered.add(r);
        }
        return tampered;
    }
}
