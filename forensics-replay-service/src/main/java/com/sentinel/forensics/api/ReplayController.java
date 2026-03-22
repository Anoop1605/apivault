package com.sentinel.forensics.api;

import com.sentinel.forensics.service.ReplayService;
import com.sentinel.shared.dto.PolicySnapshot;
import com.sentinel.shared.dto.ReplayReport;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/forensics/sessions")
@Tag(name = "Replay", description = "Session replay and what-if simulation")
public class ReplayController {

    private final ReplayService replayService;

    @Autowired
    public ReplayController(ReplayService replayService) {
        this.replayService = replayService;
    }

    @Operation(
        summary = "Replay a session",
        description = "Reconstructs the exact decision sequence for a session using the " +
                      "frozen policy snapshot active at the time (PRD FR-RE-01, FR-RE-02)."
    )
    @GetMapping("/{sessionId}/timeline")
    public ReplayReport getSessionTimeline(
            @Parameter(description = "Session UUID to replay")
            @PathVariable UUID sessionId) {
        return replayService.replaySession(sessionId);
    }

    @Operation(
        summary = "What-if simulation",
        description = "Re-evaluates all events in a session against an alternate policy rule set. " +
                      "Returns per-step original vs simulated decisions and firstDivergenceStep " +
                      "(PRD FR-RE-03, FR-RE-04)."
    )
    @PostMapping("/{sessionId}/whatif")
    public ReplayReport simulateWhatIf(
            @Parameter(description = "Session UUID to simulate")
            @PathVariable UUID sessionId,
            @RequestBody PolicySnapshot alternateSnapshot) {
        return replayService.simulateWhatIf(sessionId, alternateSnapshot);
    }
}
