package com.sentinel.shared.dto;

import java.io.Serializable;
import java.util.List;

// PRD: Frozen policy snapshot for deterministic replay
public class PolicySnapshot implements Serializable {
    private List<String> rules; // Simplified: list of rule strings

    public PolicySnapshot(List<String> rules) {
        this.rules = rules;
    }

    public List<String> getRules() {
        return rules;
    }

    public void setRules(List<String> rules) {
        this.rules = rules;
    }
}
