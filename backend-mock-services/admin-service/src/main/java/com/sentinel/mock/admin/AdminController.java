package com.sentinel.mock.admin;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

/**
 * Mock Admin Service — routing target for the Sentinel gateway.
 * Provides endpoints: GET /api/admin/dashboard, GET /api/admin/config
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard() {
        return ResponseEntity.ok(Map.of(
                "totalUsers", 156,
                "activeServices", 3,
                "systemStatus", "HEALTHY",
                "uptime", "72h 15m",
                "lastUpdated", Instant.now().toString()));
    }

    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> getConfig() {
        return ResponseEntity.ok(Map.of(
                "gatewayVersion", "2.0.0",
                "policyEngineActive", true,
                "riskScorerEnabled", true,
                "eventStoreStatus", "CONNECTED",
                "maxConcurrentRequests", 500));
    }
}
