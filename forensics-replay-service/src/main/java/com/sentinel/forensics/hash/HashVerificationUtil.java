package com.sentinel.forensics.hash;

import com.sentinel.forensics.engine.ReplayHashUtil;
import com.sentinel.shared.dto.EventDTO;
import com.sentinel.shared.dto.VerificationResult;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * HashVerificationUtil — Verifies event hash integrity.
 * Compares stored hashes against re-computed hashes to detect tampering.
 */
@Slf4j
public class HashVerificationUtil {

    /**
     * Verify all events against their stored hashes.
     *
     * @param events       The events to verify
     * @param storedHashes The stored hashes for comparison
     * @return VerificationResult summary with tampering statistics
     */
    public static VerificationResult verifyAll(List<EventDTO> events, List<String> storedHashes) {
        List<String> tamperedIds = new ArrayList<>();

        if (events.size() != storedHashes.size()) {
            log.warn("Event count mismatch: events={}, hashes={}", events.size(), storedHashes.size());
        }

        for (int i = 0; i < events.size(); i++) {
            EventDTO event = events.get(i);
            String storedHash = i < storedHashes.size() ? storedHashes.get(i) : "";
            if (storedHash == null || storedHash.isBlank()) {
                continue;
            }
            String computedHash = ReplayHashUtil.computeEventHash(event);

            if (!storedHash.equals(computedHash)) {
                tamperedIds.add(event.getEventId().toString());
            }
        }

        return VerificationResult.builder()
                .eventsChecked(events.size())
                .isClean(tamperedIds.isEmpty())
                .tamperedCount(tamperedIds.size())
                .tamperedEventIds(tamperedIds)
                .verifiedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Filter and return only tampered event IDs from verification result.
     *
     * @param verificationResult The verification result
     * @return List of tampered event IDs
     */
    public static List<String> findTampered(VerificationResult verificationResult) {
        return verificationResult.getTamperedEventIds() != null
                ? verificationResult.getTamperedEventIds()
                : new ArrayList<>();
    }
}
