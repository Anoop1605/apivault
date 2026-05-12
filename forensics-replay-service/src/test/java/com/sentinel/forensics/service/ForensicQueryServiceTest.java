package com.sentinel.forensics.service;

import com.sentinel.forensics.client.PolicyEngineClient;
import com.sentinel.forensics.event.EventStoreClient;
import com.sentinel.forensics.replay.EvaluationResult;
import com.sentinel.forensics.replay.ReplayEngine;
import com.sentinel.forensics.replay.WhatIfSimulationEngine;
import com.sentinel.shared.dto.EventDTO;
import com.sentinel.shared.enums.Decision;
import com.sentinel.shared.enums.EventType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ForensicQueryServiceTest {

    @Test
    void verifyReplayHash_usesSameSnapshotRulesAsReplaySession() {
        UUID sessionId = UUID.randomUUID();
        EventStoreClient client = new EventStoreClient() {
            @Override
            public List<EventDTO> fetchEvents(UUID sid) {
                return List.of(
                        event(UUID.fromString("00000000-0000-0000-0000-000000000001"), sid, 1000L, "BLOCK_DELETE", "DELETE",
                                EventType.ACCESS),
                        event(UUID.fromString("00000000-0000-0000-0000-000000000002"), sid, 2000L, "ALLOW_ALL", "GET",
                                EventType.LOGIN));
            }

            @Override
            public List<UUID> fetchSessionIds() {
                return List.of(sessionId);
            }
        };

        PolicyEngineClient policyClient = (event, snapshot) -> new EvaluationResult(event.getDecision().name(),
                event.getPolicyRuleId(), event.getRiskScore());
        ReplayEngine replayEngine = new ReplayEngine(policyClient);
        WhatIfSimulationEngine whatIfSimulationEngine = new WhatIfSimulationEngine(replayEngine);

        ReplayService replayService = new ReplayService(
                replayEngine,
                whatIfSimulationEngine,
                client);
        String expectedHash = replayService.replaySession(sessionId).getHash();

        ForensicQueryService queryService = new ForensicQueryService(replayService, client);
        assertTrue(queryService.verifyReplayHash(sessionId, expectedHash));
    }

    private static EventDTO event(UUID eventId, UUID sessionId, long timestampNs, String ruleId, String method,
            EventType type) {
        EventDTO event = new EventDTO();
        event.setEventId(eventId);
        event.setSessionId(sessionId);
        event.setTimestampNs(timestampNs);
        event.setPolicyRuleId(ruleId);
        event.setHttpMethod(method);
        event.setEventType(type);
        event.setDecision(Decision.ALLOW);
        event.setEndpoint("/api/test");
        event.setRiskScore(0.1d);
        event.setGatewayVersion("1.0.0");
        return event;
    }
}
