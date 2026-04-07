package com.sentinel.forensics.api;

import com.sentinel.forensics.engine.ReplayHashUtil;
import com.sentinel.forensics.hash.HashVerificationUtil;
import com.sentinel.forensics.replay.ReplayEngine;
import com.sentinel.shared.dto.EventDTO;
import com.sentinel.shared.dto.PolicySnapshot;
import com.sentinel.shared.dto.ReplayReport;
import com.sentinel.shared.dto.StepDecision;
import com.sentinel.shared.dto.VerificationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * ReplayController — REST API for forensic replay and what-if simulation.
 * Endpoints for reconstructing sessions and analyzing policy decision divergence.
 *
 * PRD Section 9: Forensic Replay Engine
 */
@Slf4j
@RestController
@RequestMapping("/forensics/replay")
@RequiredArgsConstructor
public class ReplayController {

    private final ReplayEngine replayEngine;

    /**
     * POST /forensics/replay
     * Reconstruct a session and replay with the original frozen policy snapshot.
     * Returns step-by-step decision trace showing original vs reconstructed decisions.
     *
     * Request body:
     * {
     *   "events": [...],
     *   "policySnapshot": {...}
     * }
     */
    @PostMapping
    public ResponseEntity<ReplayReport> replaySession(@RequestBody ReplayRequest request) {
        log.info("Starting session replay with {} events", request.getEvents() != null ? request.getEvents().size() : 0);

        if (request.getEvents() == null || request.getEvents().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        // Verify event integrity
        List<String> tamperedEvents = HashVerificationUtil.verifyEventHashes(request.getEvents());
        boolean hasTampering = !tamperedEvents.isEmpty();

        if (hasTampering) {
            log.warn("Tampering detected in {} events", tamperedEvents.size());
        }

        // Reconstruct the session
        List<StepDecision> steps = replayEngine.reconstruct(request.getEvents(), request.getPolicySnapshot());

        // Count divergences
        long divergences = steps.stream().filter(StepDecision::isDiverged).count();

        // Build replay report
        ReplayReport report = ReplayReport.builder()
                .sessionId(request.getEvents().get(0).getSessionId())
                .totalEvents(request.getEvents().size())
                .divergenceCount(Math.toIntExact(divergences))
                .tamperingDetected(hasTampering)
                .tamperedEventIds(tamperedEvents)
                .steps(steps)
                .generatedAt(LocalDateTime.now())
                .build();

        log.info("Session replay complete: {} events, {} divergences, tampering={}",
                report.getTotalEvents(), report.getDivergenceCount(), hasTampering);

        return ResponseEntity.ok(report);
    }

    /**
     * POST /forensics/simulate
     * What-if simulation: replay session with modified policy rules.
     * Compares original decisions vs decisions with alternate ruleset.
     *
     * Request body:
     * {
     *   "events": [...],
     *   "originalSnapshot": {...},
     *   "modifiedSnapshot": {...}
     * }
     */
    @PostMapping("/simulate")
    public ResponseEntity<ReplayReport> simulateAlternateRules(@RequestBody SimulationRequest request) {
        log.info("Starting what-if simulation with {} events", request.getEvents() != null ? request.getEvents().size() : 0);

        if (request.getEvents() == null || request.getEvents().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        // Run what-if simulation
        List<StepDecision> steps = replayEngine.simulateWithModifiedRules(
                request.getEvents(),
                request.getOriginalSnapshot(),
                request.getModifiedSnapshot()
        );

        // Count divergences
        long divergences = steps.stream().filter(StepDecision::isDiverged).count();

        // Build report
        ReplayReport report = ReplayReport.builder()
                .sessionId(request.getEvents().get(0).getSessionId())
                .totalEvents(request.getEvents().size())
                .divergenceCount(Math.toIntExact(divergences))
                .steps(steps)
                .isSimulation(true)
                .generatedAt(LocalDateTime.now())
                .build();

        log.info("What-if simulation complete: {} events, {} divergences",
                report.getTotalEvents(), report.getDivergenceCount());

        return ResponseEntity.ok(report);
    }

    /**
     * POST /forensics/verify
     * Verify integrity of stored events using canonical hashing.
     * Detects tampering by comparing stored hashes.
     *
     * Request body:
     * {
     *   "events": [...]
     * }
     */
    @PostMapping("/verify")
    public ResponseEntity<VerificationResult> verifyIntegrity(@RequestBody List<EventDTO> events) {
        log.info("Starting integrity verification of {} events", events != null ? events.size() : 0);

        if (events == null || events.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        // Verify all events
        List<String> tamperedEventIds = HashVerificationUtil.verifyEventHashes(events);
        boolean isClean = tamperedEventIds.isEmpty();

        VerificationResult result = VerificationResult.builder()
                .eventsChecked(events.size())
                .isClean(isClean)
                .tamperedCount(tamperedEventIds.size())
                .tamperedEventIds(tamperedEventIds)
                .verifiedAt(LocalDateTime.now())
                .build();

        log.info("Integrity verification complete: {} clean, {} tampered",
                isClean ? events.size() : events.size() - tamperedEventIds.size(),
                tamperedEventIds.size());

        return ResponseEntity.ok(result);
    }

    /**
     * POST /forensics/hash
     * Compute canonical hash for a single event.
     */
    @PostMapping("/hash")
    public ResponseEntity<String> computeEventHash(@RequestBody EventDTO event) {
        if (event == null) {
            return ResponseEntity.badRequest().build();
        }

        String hash = ReplayHashUtil.computeEventHash(event);
        return ResponseEntity.ok(hash);
    }

    // =========================================================================
    // Request/Response DTOs
    // =========================================================================

    /**
     * Request for session replay
     */
    public static class ReplayRequest {
        private List<EventDTO> events;
        private PolicySnapshot policySnapshot;

        public List<EventDTO> getEvents() { return events; }
        public void setEvents(List<EventDTO> events) { this.events = events; }

        public PolicySnapshot getPolicySnapshot() { return policySnapshot; }
        public void setPolicySnapshot(PolicySnapshot policySnapshot) { this.policySnapshot = policySnapshot; }
    }

    /**
     * Request for what-if simulation
     */
    public static class SimulationRequest {
        private List<EventDTO> events;
        private PolicySnapshot originalSnapshot;
        private PolicySnapshot modifiedSnapshot;

        public List<EventDTO> getEvents() { return events; }
        public void setEvents(List<EventDTO> events) { this.events = events; }

        public PolicySnapshot getOriginalSnapshot() { return originalSnapshot; }
        public void setOriginalSnapshot(PolicySnapshot originalSnapshot) { this.originalSnapshot = originalSnapshot; }

        public PolicySnapshot getModifiedSnapshot() { return modifiedSnapshot; }
        public void setModifiedSnapshot(PolicySnapshot modifiedSnapshot) { this.modifiedSnapshot = modifiedSnapshot; }
    }
}
