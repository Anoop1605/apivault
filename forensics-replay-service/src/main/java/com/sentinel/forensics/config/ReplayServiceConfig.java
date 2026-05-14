package com.sentinel.forensics.config;

import com.sentinel.forensics.client.PolicyEngineClient;
import com.sentinel.forensics.replay.ReplayEngine;
import com.sentinel.forensics.replay.WhatIfSimulationEngine;
import com.sentinel.forensics.event.EventStoreClient;
import com.sentinel.forensics.event.MockEventStoreClient;
import com.sentinel.forensics.event.RestEventStoreClient;
import com.sentinel.forensics.service.DashboardService;
import com.sentinel.forensics.service.ForensicQueryService;
import com.sentinel.forensics.service.ReplayService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ReplayServiceConfig {

    @Value("${event-store.base-url:http://localhost:8081}")
    private String eventStoreBaseUrl;

    /**
     * Set to true in application.properties to use fixture mock data (no DB
     * needed).
     */
    @Value("${event-store.use-mock:false}")
    private boolean useMock;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public ReplayEngine replayEngine(PolicyEngineClient policyEngineClient) {
        return new ReplayEngine(policyEngineClient);
    }

    @Bean
    public WhatIfSimulationEngine whatIfSimulationEngine(ReplayEngine replayEngine) {
        return new WhatIfSimulationEngine(replayEngine);
    }

    @Bean
    public EventStoreClient eventStoreClient(RestTemplate restTemplate) {
        if (useMock)
            return new MockEventStoreClient();
        return new RestEventStoreClient(restTemplate, eventStoreBaseUrl);
    }

    @Bean
    public ReplayService replayService(ReplayEngine replayEngine, WhatIfSimulationEngine whatIfSimulationEngine,
            EventStoreClient eventStoreClient, PolicyEngineClient policyEngineClient) {
        return new ReplayService(policyEngineClient, replayEngine, whatIfSimulationEngine, eventStoreClient);
    }

    @Bean
    public ForensicQueryService forensicQueryService(ReplayService replayService, EventStoreClient eventStoreClient) {
        return new ForensicQueryService(replayService, eventStoreClient);
    }

    @Bean
    public DashboardService dashboardService(ForensicQueryService forensicQueryService) {
        return new DashboardService(forensicQueryService);
    }
}
