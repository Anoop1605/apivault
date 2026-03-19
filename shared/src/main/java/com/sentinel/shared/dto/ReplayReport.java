package com.sentinel.shared.dto;

import java.util.List;
import java.util.UUID;

/**
 * Full output of a replay or what-if simulation.
 * PRD FR-RE-04: step-by-step trace, original vs simulated decisions,
 * first divergence point, and snapshot ID used.
 */
public class ReplayReport {

    private UUID sessionId;

    /** All steps in chronological order. Each step holds original + simulated decision. */
    private List<StepDecision> steps;

    /** Original decisions in order (ALLOW/BLOCK per step). */
    private List<String> originalDecisions;

    /** Simulated decisions using alternate rules (ALLOW/BLOCK per step). */
    private List<String> simulatedDecisions;

    /**
     * 1-based index of the first step where original and simulated decisions diverge.
     * -1 means no divergence (same outcome throughout).
     */
    private int firstDivergenceStep = -1;

    /** The frozen policy snapshot ID used for this replay (PRD FR-RE-02). */
    private UUID snapshotIdUsed;

    /** Tamper-evident SHA-256 hash of all events + snapshot (PRD §10.3). */
    private String hash;

    public UUID getSessionId() { return sessionId; }
    public void setSessionId(UUID sessionId) { this.sessionId = sessionId; }

    public List<StepDecision> getSteps() { return steps; }
    public void setSteps(List<StepDecision> steps) { this.steps = steps; }

    public List<String> getOriginalDecisions() { return originalDecisions; }
    public void setOriginalDecisions(List<String> originalDecisions) {
        this.originalDecisions = originalDecisions;
    }

    public List<String> getSimulatedDecisions() { return simulatedDecisions; }
    public void setSimulatedDecisions(List<String> simulatedDecisions) {
        this.simulatedDecisions = simulatedDecisions;
    }

    public int getFirstDivergenceStep() { return firstDivergenceStep; }
    public void setFirstDivergenceStep(int firstDivergenceStep) {
        this.firstDivergenceStep = firstDivergenceStep;
    }

    public UUID getSnapshotIdUsed() { return snapshotIdUsed; }
    public void setSnapshotIdUsed(UUID snapshotIdUsed) { this.snapshotIdUsed = snapshotIdUsed; }

    public String getHash() { return hash; }
    public void setHash(String hash) { this.hash = hash; }
}
