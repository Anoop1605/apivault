package com.sentinel.forensics.replay;

import com.sentinel.forensics.client.PolicyEngineClient;
import com.sentinel.shared.dto.EventDTO;
import com.sentinel.shared.dto.PolicySnapshot;
import com.sentinel.shared.enums.EventType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class ReplayEngineTest {

    @Test
    void reconstruct_acceptsImmutableInputAndPreservesOrder() {
        EventDTO late = event(2L);
        EventDTO early = event(1L);
        List<EventDTO> immutableEvents = List.of(late, early);

        PolicyEngineClient client = (event, snapshot) -> new EvaluationResult("ALLOW", event.getPolicyRuleId(),
                event.getRiskScore());
        ReplayEngine engine = new ReplayEngine(client);

        List<EvaluationResult> results = assertDoesNotThrow(
                () -> engine.reconstruct(immutableEvents, new PolicySnapshot(List.of("ALLOW_ALL"))));

        assertEquals(2, results.size());
        assertEquals("RULE_2", results.get(0).getRuleMatched());
        assertEquals("RULE_1", results.get(1).getRuleMatched());
        assertEquals(2L, immutableEvents.get(0).getTimestampNs());
        assertEquals(1L, immutableEvents.get(1).getTimestampNs());
    }

    private static EventDTO event(long timestampNs) {
        EventDTO event = new EventDTO();
        event.setEventId(UUID.randomUUID());
        event.setSessionId(UUID.randomUUID());
        event.setTimestampNs(timestampNs);
        event.setHttpMethod("GET");
        event.setEventType(EventType.ACCESS);
        event.setPolicyRuleId("RULE_" + timestampNs);
        event.setRiskScore(0.2d);
        return event;
    }
}
