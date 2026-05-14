package com.sentinel.forensics.api;

import com.sentinel.forensics.service.ReplayService;
import com.sentinel.shared.dto.PolicySnapshot;
import com.sentinel.shared.dto.ReplayReport;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.UUID;

/**
 * ReplayController — REST endpoints for forensic replay and what-if simulation.
 *
 * PRD FR-RE-01: Replay a session with the frozen policy snapshot.
 * PRD FR-RE-03: What-if simulation with an alternate policy snapshot.
 * PRD FR-RE-04: Step-by-step decision trace with divergence detection.
 */
@Slf4j
@RestController
@RequestMapping("/forensics")
@Tag(name = "Forensic Replay", description = "Session replay and what-if policy simulation")
public class ReplayController {

    private final ReplayService replayService;

    public ReplayController(ReplayService replayService) {
        this.replayService = replayService;
    }

    /**
     * POST /forensics/replay/{sessionId}
     *
     * Replays a session with the original frozen policy snapshot.
     * Reconstructs the exact sequence of policy decisions that occurred in production.
     */
    @Operation(summary = "Replay session",
               description = "Replays all events in a session against the original frozen policy snapshot, " +
                             "reconstructing the exact decision sequence.")
    @PostMapping("/replay/{sessionId}")
    public ResponseEntity<ReplayReport> replaySession(
            @Parameter(description = "Session UUID") @PathVariable UUID sessionId) {
        log.info("Replaying session: {}", sessionId);
        ReplayReport report = replayService.replaySession(sessionId);
        return ResponseEntity.ok(report);
    }

    /**
     * GET /forensics/replay/{sessionId}
     *
     * Same as POST but via GET for easy browser/frontend access.
     */
    @Operation(summary = "Replay session (GET)",
               description = "Convenience GET endpoint for session replay.")
    @GetMapping("/replay/{sessionId}")
    public ResponseEntity<ReplayReport> replaySessionGet(
            @Parameter(description = "Session UUID") @PathVariable UUID sessionId) {
        log.info("Replaying session (GET): {}", sessionId);
        ReplayReport report = replayService.replaySession(sessionId);
        return ResponseEntity.ok(report);
    }

    /**
     * POST /forensics/sessions/{sessionId}/whatif
     *
     * What-if simulation: replays a session against an ALTERNATE set of policy rules.
     * The request body contains the alternate PolicySnapshot (list of rule IDs to apply).
     * Returns a ReplayReport showing original vs simulated decisions and the first
     * divergence point.
     */
    @Operation(summary = "What-if simulation",
               description = "Replays a session against alternate policy rules. " +
                             "Returns side-by-side original vs simulated decisions and first divergence step.")
    @PostMapping("/sessions/{sessionId}/whatif")
    public ResponseEntity<ReplayReport> whatIfSimulation(
            @Parameter(description = "Session UUID") @PathVariable UUID sessionId,
            @RequestBody(required = false) PolicySnapshot alternateSnapshot) {
        log.info("What-if simulation for session: {}", sessionId);

        // If no alternate snapshot provided, use an empty rule set (maximum restriction)
        if (alternateSnapshot == null) {
            alternateSnapshot = new PolicySnapshot(Collections.emptyList());
        }

        ReplayReport report = replayService.simulateWhatIf(sessionId, alternateSnapshot);
        return ResponseEntity.ok(report);
    }

    /**
     * GET /forensics/sessions/{sessionId}/replay
     *
     * Convenience GET endpoint for session replay — used by the frontend's
     * "Replay Session" button.
     */
    @Operation(summary = "Replay session report",
               description = "Returns the full replay report for a session.")
    @GetMapping("/sessions/{sessionId}/replay")
    public ResponseEntity<ReplayReport> getSessionReplay(
            @Parameter(description = "Session UUID") @PathVariable UUID sessionId) {
        log.info("Getting replay report for session: {}", sessionId);
        ReplayReport report = replayService.replaySession(sessionId);
        return ResponseEntity.ok(report);
    }

    /**
     * POST /forensics/replay
     *
     * Frontend-compatible replay endpoint that accepts session_id in the request body.
     */
    @Operation(summary = "Replay session (body)",
               description = "Replays a session. Accepts session_id in request body.")
    @PostMapping("/replay")
    public ResponseEntity<ReplayReport> replaySessionFromBody(@RequestBody java.util.Map<String, String> body) {
        String sessionIdStr = body.get("session_id");
        if (sessionIdStr == null || sessionIdStr.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        UUID sessionId = UUID.fromString(sessionIdStr);
        log.info("Replaying session from body: {}", sessionId);
        ReplayReport report = replayService.replaySession(sessionId);
        return ResponseEntity.ok(report);
    }
}
