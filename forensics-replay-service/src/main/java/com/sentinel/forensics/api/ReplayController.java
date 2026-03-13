package com.sentinel.forensics.api;

import com.sentinel.forensics.service.ReplayService;
import com.sentinel.shared.dto.EventDTO;
import com.sentinel.shared.dto.ReplayReport;
import com.sentinel.shared.dto.StepDecision;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.*;

@RestController
@RequestMapping("/forensics/sessions")
public class ReplayController {

    private final ReplayService replayService;

    @Autowired
    public ReplayController(ReplayService replayService) {
        this.replayService = replayService;
    }

    /**
     * What-If Simulation API
     * POST /forensics/sessions/{sessionId}/whatif
     * Request body: PolicySnapshot (alternate rules)
     * Response: ReplayReport
     * Example curl:
     * curl -X POST "http://localhost:8080/forensics/sessions/{sessionId}/whatif" -H "Content-Type: application/json" -d '{"rules": ["ruleA", "ruleB"]}'
     */
    @PostMapping("/{sessionId}/whatif")
    public ReplayReport simulateWhatIf(@PathVariable UUID sessionId, @RequestBody com.sentinel.shared.dto.PolicySnapshot alternateSnapshot) {
        return replayService.simulateWhatIf(sessionId, alternateSnapshot);
    }

    // GET /forensics/sessions/{sessionId}/timeline
    @GetMapping("/{sessionId}/timeline")
    public ReplayReport getSessionTimeline(@PathVariable UUID sessionId) {
        return replayService.replaySession(sessionId);
    }
    // mockEvents removed; events now fetched via EventStoreClient
}
