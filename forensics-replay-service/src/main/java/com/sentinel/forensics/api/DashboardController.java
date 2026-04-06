package com.sentinel.forensics.api;

import com.sentinel.forensics.service.DashboardService;
import com.sentinel.shared.dto.AlertDTO;
import com.sentinel.shared.dto.PolicyTraceDTO;
import com.sentinel.shared.dto.TimelineEventDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/forensics/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/sessions/{sessionId}/timeline")
    public ResponseEntity<List<TimelineEventDTO>> getSessionTimeline(
            @PathVariable UUID sessionId) {
        return ResponseEntity.ok(dashboardService.getSessionTimeline(sessionId));
    }

    @GetMapping("/sessions/{sessionId}/policy-trace")
    public ResponseEntity<PolicyTraceDTO> getPolicyTrace(
            @PathVariable UUID sessionId) {
        return ResponseEntity.ok(dashboardService.getPolicyTrace(sessionId));
    }

    @GetMapping("/sessions/{sessionId}/alerts")
    public ResponseEntity<List<AlertDTO>> getAlerts(
            @PathVariable UUID sessionId) {
        return ResponseEntity.ok(dashboardService.getAlerts(sessionId));
    }
}
