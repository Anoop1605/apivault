package com.sentinel.shared.dto;

import com.sentinel.shared.enums.Decision;

import java.util.List;
import java.util.UUID;

public class PolicyTraceDTO {

    private UUID sessionId;
    private List<String> rulesEvaluated;
    private List<String> rulesMatched;
    private Decision finalDecision;
    private String hash;

    public PolicyTraceDTO() {}

    public PolicyTraceDTO(UUID sessionId, List<String> rulesEvaluated,
                           List<String> rulesMatched, Decision finalDecision, String hash) {
        this.sessionId = sessionId;
        this.rulesEvaluated = rulesEvaluated;
        this.rulesMatched = rulesMatched;
        this.finalDecision = finalDecision;
        this.hash = hash;
    }

    public UUID getSessionId() { return sessionId; }
    public void setSessionId(UUID sessionId) { this.sessionId = sessionId; }

    public List<String> getRulesEvaluated() { return rulesEvaluated; }
    public void setRulesEvaluated(List<String> rulesEvaluated) { this.rulesEvaluated = rulesEvaluated; }

    public List<String> getRulesMatched() { return rulesMatched; }
    public void setRulesMatched(List<String> rulesMatched) { this.rulesMatched = rulesMatched; }

    public Decision getFinalDecision() { return finalDecision; }
    public void setFinalDecision(Decision finalDecision) { this.finalDecision = finalDecision; }

    public String getHash() { return hash; }
    public void setHash(String hash) { this.hash = hash; }
}
