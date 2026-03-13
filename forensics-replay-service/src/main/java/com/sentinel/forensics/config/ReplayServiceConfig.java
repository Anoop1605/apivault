package com.sentinel.forensics.config;

import com.sentinel.forensics.replay.ReplayEngine;
import com.sentinel.forensics.replay.WhatIfSimulationEngine;
import com.sentinel.forensics.event.EventStoreClient;
import com.sentinel.forensics.event.MockEventStoreClient;
import com.sentinel.forensics.service.ReplayService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ReplayServiceConfig {
    @Bean
    public ReplayEngine replayEngine() {
        return new ReplayEngine();
    }

    @Bean
    public WhatIfSimulationEngine whatIfSimulationEngine() {
        return new WhatIfSimulationEngine();
    }

    @Bean
    public EventStoreClient eventStoreClient() {
        return new MockEventStoreClient();
    }

    @Bean
    public ReplayService replayService(ReplayEngine replayEngine, WhatIfSimulationEngine whatIfSimulationEngine, EventStoreClient eventStoreClient) {
        return new ReplayService(replayEngine, whatIfSimulationEngine, eventStoreClient);
    }
}
