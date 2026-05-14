package com.sentinel.forensics.client;

import com.sentinel.shared.dto.EventDTO;
import com.sentinel.shared.dto.PolicySnapshot;
import com.sentinel.forensics.replay.EvaluationResult;
import com.sentinel.shared.context.RequestContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * RestPolicyEngineClient — Communicates with the Policy Engine service.
 * Used during forensic replays and what-if simulations to re-evaluate
 * historical requests against current or alternate policy sets.
 */
@Slf4j
@Component
public class RestPolicyEngineClient implements PolicyEngineClient {

    private final RestTemplate restTemplate;
    private final String policyEngineUrl;

    public RestPolicyEngineClient(RestTemplate restTemplate,
                                  @Value("${policy-engine.base-url:http://localhost:8082}") String policyEngineUrl) {
        this.restTemplate = restTemplate;
        this.policyEngineUrl = policyEngineUrl;
    }

    @Override
    public EvaluationResult evaluate(EventDTO event, PolicySnapshot policySnapshot) {
        log.debug("Calling Policy Engine for snapshot evaluation. Event: {}", event.getEventId());

        // Convert EventDTO back to RequestContext for evaluation
        RequestContext context = RequestContext.builder()
                .userId(event.getUserId())
                .roles(event.getRoles())
                .endpoint(event.getEndpoint())
                .httpMethod(event.getHttpMethod())
                .sourceIp(event.getSourceIp())
                .sessionId(event.getSessionId() != null ? event.getSessionId().toString() : null)
                .userAgent(event.getUserAgent())
                .riskScore(event.getRiskScore())
                .build();

        Map<String, Object> request = Map.of(
                "context", context,
                "snapshot", policySnapshot
        );

        try {
            // Call the /policy/evaluate-snapshot endpoint
            PolicyDecisionResponse response = restTemplate.postForObject(
                    policyEngineUrl + "/policy/evaluate-snapshot",
                    request,
                    PolicyDecisionResponse.class);

            if (response == null) {
                return fallback(event);
            }

            EvaluationResult result = new EvaluationResult();
            result.setDecision(response.decision);
            result.setRuleMatched(response.ruleId);
            result.setRiskScore(event.getRiskScore() != null ? event.getRiskScore() : 0.0);
            return result;

        } catch (Exception e) {
            log.error("Failed to call Policy Engine for simulation: {}", e.getMessage());
            return fallback(event);
        }
    }

    private EvaluationResult fallback(EventDTO event) {
        EvaluationResult res = new EvaluationResult();
        res.setDecision(event.getDecision() != null ? event.getDecision().name() : "ALLOW");
        res.setRuleMatched(event.getPolicyRuleId() != null ? event.getPolicyRuleId() : "FALLBACK");
        res.setRiskScore(event.getRiskScore() != null ? event.getRiskScore() : 0.0);
        return res;
    }

    /**
     * Internal DTO to map the Policy Engine's response without
     * depending on its model classes.
     */
    private static class PolicyDecisionResponse {
        public String ruleId;
        public String decision;
    }
}
