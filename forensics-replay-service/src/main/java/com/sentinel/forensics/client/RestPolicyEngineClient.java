package com.sentinel.forensics.client;

import com.sentinel.shared.dto.EventDTO;
import com.sentinel.shared.dto.PolicySnapshot;
import com.sentinel.forensics.replay.EvaluationResult;
import org.springframework.stereotype.Component;

@Component
public class RestPolicyEngineClient implements PolicyEngineClient {

    @Override
    public EvaluationResult evaluate(EventDTO event, PolicySnapshot policySnapshot) {
        // Fallback/Mock implementation just to satisfy the interface for replay
        // In a real scenario, this would call the policy-engine-service via RestTemplate or WebClient
        EvaluationResult result = new EvaluationResult();
        result.setDecision(event.getDecision() != null ? event.getDecision().name() : "ALLOW");
        result.setRuleMatched(event.getPolicyRuleId() != null ? event.getPolicyRuleId() : "DEFAULT_ALLOW");
        result.setRiskScore(event.getRiskScore() != null ? event.getRiskScore() : 0.0);
        return result;
    }
}
