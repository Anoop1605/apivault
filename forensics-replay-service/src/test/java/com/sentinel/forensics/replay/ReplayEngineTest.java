package com.sentinel.forensics.replay;

import com.sentinel.shared.dto.EventDTO;
import com.sentinel.shared.dto.PolicySnapshot;
import com.sentinel.shared.dto.StepDecision;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class ReplayEngineTest {

    @Test
    void reconstruct_acceptsImmutableInputAndSortsWithoutMutatingInput() {
        EventDTO late = event(2L);
        EventDTO early = event(1L);
        List<EventDTO> immutableEvents = List.of(late, early);

        ReplayEngine engine = new ReplayEngine();
        List<StepDecision> steps = assertDoesNotThrow(() ->
                engine.reconstruct(immutableEvents, new PolicySnapshot(List.of("ALLOW_ALL"))));

        assertEquals(2, steps.size());
        assertEquals(1L, steps.get(0).getEvent().getTimestampNs());
        assertEquals(2L, steps.get(1).getEvent().getTimestampNs());
        assertEquals(2L, immutableEvents.get(0).getTimestampNs());
        assertEquals(1L, immutableEvents.get(1).getTimestampNs());
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
