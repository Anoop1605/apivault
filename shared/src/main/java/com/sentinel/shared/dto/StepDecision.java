package com.sentinel.shared.dto;

/**
 * Represents one step in a replay or what-if simulation.
 * PRD FR-RE-04: step-by-step decision trace, original vs simulated decisions.
 */
public class StepDecision {

    private final EventDTO event;

    /** Decision produced by the frozen snapshot (original replay). */
    private String originalDecision;

    /** Decision produced by the alternate rule set (what-if simulation). */
    private String simulatedDecision;

    /** The rule ID that fired and produced the decision. */
    private String ruleMatched;

    /** True if originalDecision != simulatedDecision (divergence point). */
    private boolean diverged;

    public StepDecision(EventDTO event) {
        this.event = event;
    }

    public StepDecision(EventDTO event, String originalDecision,
                        String simulatedDecision, String ruleMatched) {
        this.event = event;
        this.originalDecision = originalDecision;
        this.simulatedDecision = simulatedDecision;
        this.ruleMatched = ruleMatched;
        this.diverged = originalDecision != null
                && !originalDecision.equals(simulatedDecision);
    }

    public EventDTO getEvent() { return event; }

    public String getOriginalDecision() { return originalDecision; }
    public void setOriginalDecision(String originalDecision) {
        this.originalDecision = originalDecision;
        updateDiverged();
    }

    public String getSimulatedDecision() { return simulatedDecision; }
    public void setSimulatedDecision(String simulatedDecision) {
        this.simulatedDecision = simulatedDecision;
        updateDiverged();
    }

    public String getRuleMatched() { return ruleMatched; }
    public void setRuleMatched(String ruleMatched) { this.ruleMatched = ruleMatched; }

    public boolean isDiverged() { return diverged; }
    public void setDiverged(boolean diverged) { this.diverged = diverged; }

    private void updateDiverged() {
        this.diverged = originalDecision != null
                && simulatedDecision != null
                && !originalDecision.equals(simulatedDecision);
    }
}

