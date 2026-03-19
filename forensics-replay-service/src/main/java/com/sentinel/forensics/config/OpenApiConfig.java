package com.sentinel.forensics.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI sentinelOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Sentinel Forensic Replay API")
                        .version("1.0.0")
                        .description("""
                                Sentinel is a forensic-first, zero-trust API gateway.
                                This API provides:
                                - Forensic session replay using frozen policy snapshots
                                - What-if policy simulation
                                - Tamper-evident SHA-256 hash verification
                                - Dashboard data for session explorer, timeline, and alerts
                                """)
                        .contact(new Contact()
                                .name("Team Sentinel — P4 Forensics")
                                .email("sentinel@project.local")))
                .tags(List.of(
                        new Tag().name("Replay").description("Session replay and what-if simulation"),
                        new Tag().name("Forensic Query").description("Query sessions, reports, and hash verification"),
                        new Tag().name("Dashboard").description("Timeline, policy trace, and alert views")
                ));
    }
}
