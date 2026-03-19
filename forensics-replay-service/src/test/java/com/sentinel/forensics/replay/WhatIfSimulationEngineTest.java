package com.sentinel.forensics.replay;

import com.sentinel.shared.dto.EventDTO;
import com.sentinel.shared.dto.PolicySnapshot;
import com.sentinel.shared.dto.ReplayReport;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class WhatIfSimulationEngineTest {

    @Test
    void simulate_acceptsImmutableInputAndSortsWithoutMutatingInput() {
        EventDTO late = event(20L);
        EventDTO early = event(10L);
        List<EventDTO> immutableEvents = List.of(late, early);

        WhatIfSimulationEngine engine = new WhatIfSimulationEngine();
        ReplayReport report = assertDoesNotThrow(() -> engine.simulate(
                UUID.randomUUID(),
                immutableEvents,
                new PolicySnapshot(List.of("ALLOW_ALL")),
                new PolicySnapshot(List.of("ALLOW_ALL"))
        ));

        assertEquals(2, report.getSteps().size());
        assertEquals(10L, report.getSteps().get(0).getEvent().getTimestampNs());
        assertEquals(20L, report.getSteps().get(1).getEvent().getTimestampNs());
        assertEquals(20L, immutableEvents.get(0).getTimestampNs());
        assertEquals(10L, immutableEvents.get(1).getTimestampNs());
    }

    private static EventDTO event(long timestampNs) {
        EventDTO event = new EventDTO();
        event.setEventId(UUID.randomUUID());
        event.setSessionId(UUID.randomUUID());
        event.setTimestampNs(timestampNs);
        event.setHttpMethod("GET");
        event.setEventType("ACCESS");
        event.setPolicyRuleId("ALLOW_ALL");
        return event;
    }
}
