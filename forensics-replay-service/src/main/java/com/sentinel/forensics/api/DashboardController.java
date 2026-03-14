package com.sentinel.forensics.api;

import com.sentinel.forensics.service.DashboardService;
import com.sentinel.shared.dto.AlertDTO;
import com.sentinel.shared.dto.PolicyTraceDTO;
import com.sentinel.shared.dto.TimelineEventDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/forensics/dashboard")
@Tag(name = "Dashboard", description = "Timeline, policy trace, and alert views")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Operation(summary = "Get session timeline",
               description = "Returns a chronological list of events for a session, " +
                             "each annotated with event type and policy decision.")
    @GetMapping("/sessions/{sessionId}/timeline")
    public ResponseEntity<List<TimelineEventDTO>> getSessionTimeline(
            @Parameter(description = "Session UUID") @PathVariable UUID sessionId) {
        return ResponseEntity.ok(dashboardService.getSessionTimeline(sessionId));
    }

    @Operation(summary = "Get policy trace",
               description = "Returns the full policy evaluation trace: rules evaluated, " +
                             "rules matched, final decision, and tamper-evident hash.")
    @GetMapping("/sessions/{sessionId}/policy-trace")
    public ResponseEntity<PolicyTraceDTO> getPolicyTrace(
            @Parameter(description = "Session UUID") @PathVariable UUID sessionId) {
        return ResponseEntity.ok(dashboardService.getPolicyTrace(sessionId));
    }

    @Operation(summary = "Get alerts",
               description = "Returns all BLOCK and REVIEW events in a session as alerts, " +
                             "with severity levels.")
    @GetMapping("/sessions/{sessionId}/alerts")
    public ResponseEntity<List<AlertDTO>> getAlerts(
            @Parameter(description = "Session UUID") @PathVariable UUID sessionId) {
        return ResponseEntity.ok(dashboardService.getAlerts(sessionId));
    }
}
