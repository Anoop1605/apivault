package com.sentinel.forensics.simulation;

import com.sentinel.forensics.replay.WhatIfSimulationEngine;
import com.sentinel.shared.dto.EventDTO;
import com.sentinel.shared.dto.PolicySnapshot;
import com.sentinel.shared.dto.ReplayReport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;

/**
 * WhatIfSimulator — Facade class for what-if policy simulation.
 *
 * Delegates to the WhatIfSimulationEngine for the actual simulation logic.
 * This class provides a simplified API for controllers and services that
 * need to run what-if simulations without managing event fetching themselves.
 *
 * PRD FR-RE-03: "Admin can supply alternate PolicySnapshot and re-run
 * the entire session to see where outcomes diverge."
 */
@Slf4j
@RequiredArgsConstructor
public class WhatIfSimulator {

    private final WhatIfSimulationEngine whatIfSimulationEngine;

    /**
     * Runs a what-if simulation for a session given pre-fetched events.
     *
     * @param sessionId         The session to simulate
     * @param events            The events for the session
     * @param originalSnapshot  The original frozen policy snapshot
     * @param alternateSnapshot The modified rule set to test
     * @return A ReplayReport with original vs simulated decisions
     */
    public ReplayReport simulate(UUID sessionId,
                                  List<EventDTO> events,
                                  PolicySnapshot originalSnapshot,
                                  PolicySnapshot alternateSnapshot) {
        log.info("WhatIfSimulator: Running simulation for session {} with {} alternate rules",
                sessionId, alternateSnapshot.getRules() != null ? alternateSnapshot.getRules().size() : 0);

        return whatIfSimulationEngine.simulate(sessionId, events, originalSnapshot, alternateSnapshot);
    }
}
