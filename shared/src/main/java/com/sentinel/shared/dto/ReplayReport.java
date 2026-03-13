package com.sentinel.shared.dto;

import java.util.List;
import java.util.UUID;
import com.sentinel.shared.dto.StepDecision;

public class ReplayReport {
    private UUID sessionId;
    private List<StepDecision> steps;

    public UUID getSessionId() {
        return sessionId;
    }
    public void setSessionId(UUID sessionId) {
        this.sessionId = sessionId;
    }
    public List<StepDecision> getSteps() {
        return steps;
    }
    public void setSteps(List<StepDecision> steps) {
        this.steps = steps;
    }
}
