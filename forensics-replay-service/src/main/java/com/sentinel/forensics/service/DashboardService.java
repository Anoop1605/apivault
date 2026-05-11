package com.sentinel.forensics.service;

import com.sentinel.shared.dto.AlertDTO;
import com.sentinel.shared.dto.PolicyTraceDTO;
import com.sentinel.shared.dto.ReplayReport;
import com.sentinel.shared.dto.TimelineEventDTO;
import com.sentinel.shared.enums.Decision;
import com.sentinel.shared.enums.EventType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {

    private final ForensicQueryService forensicQueryService;

    public DashboardService(ForensicQueryService forensicQueryService) {
        this.forensicQueryService = forensicQueryService;
    }

    /**
     * Returns a chronologically sorted timeline of events for a session,
     * pulled directly from the engine-evaluated ReplayReport.
     */
    public List<TimelineEventDTO> getSessionTimeline(UUID sessionId) {
        ReplayReport report = forensicQueryService.getReplayReportBySessionId(sessionId);

        return report.getSteps().stream()
                .map(step -> new TimelineEventDTO(
                        sessionId,
                        step.getTimestampNs(),
                        EventType.valueOf(step.getEventType()),
                        step.getOriginalDecision(),
                        step.getRuleId()))
                .sorted((a, b) -> Long.compare(a.getTimestampNs(), b.getTimestampNs()))
                .toList();
    }

    /**
     * Returns the policy trace for a session directly from the ReplayReport.
     */
    public PolicyTraceDTO getPolicyTrace(UUID sessionId) {
        ReplayReport report = forensicQueryService.getReplayReportBySessionId(sessionId);

        List<String> rulesMatched = report.getSteps().stream()
                .map(ReplayReport.StepDecision::getRuleId)
                .filter(id -> id != null && !id.equals("DEFAULT"))
                .distinct()
                .toList();

        boolean hasBlock = report.getSteps().stream()
                .anyMatch(step -> step.getOriginalDecision() == Decision.BLOCK);

        Decision finalDecision = hasBlock ? Decision.BLOCK : Decision.ALLOW; // or POLICY_NO_MATCH

        // The report natively tracks the snapshot ID used and the session hash!
        return new PolicyTraceDTO(sessionId,
                report.getSnapshotIdUsed() != null ? List.of(report.getSnapshotIdUsed().toString()) : Collections.emptyList(),
                rulesMatched, finalDecision, report.getHash());
    }

    /**
     * Returns alerts for a session — events that resulted in a BLOCK or REVIEW.
     */
    public List<AlertDTO> getAlerts(UUID sessionId) {
        ReplayReport report = forensicQueryService.getReplayReportBySessionId(sessionId);
        List<AlertDTO> alerts = new ArrayList<>();

        for (ReplayReport.StepDecision step : report.getSteps()) {
            if (step.getOriginalDecision() == Decision.BLOCK) {
                alerts.add(new AlertDTO(
                        sessionId,
                        step.getTimestampNs(),
                        EventType.valueOf(step.getEventType()),
                        AlertDTO.Severity.HIGH,
                        "Event blocked by policy: " + step.getRuleId()));
            } else if (step.getOriginalDecision() == Decision.REVIEW) {
                alerts.add(new AlertDTO(
                        sessionId,
                        step.getTimestampNs(),
                        EventType.valueOf(step.getEventType()),
                        AlertDTO.Severity.MEDIUM,
                        "Event flagged for review"));
            }
        }

        return alerts;
    }
}
