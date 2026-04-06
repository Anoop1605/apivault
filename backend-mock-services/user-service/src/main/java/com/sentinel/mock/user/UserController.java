package com.sentinel.mock.user;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Mock User Service — routing target for the Sentinel gateway.
 * Provides endpoints: GET /api/users/profile, GET /api/users, POST /api/users
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> getProfile(
            @RequestHeader(value = "X-Session-ID", required = false) String sessionId) {
        return ResponseEntity.ok(Map.of(
                "userId", "user-9921",
                "name", "Priya Sharma",
                "email", "priya@sentinel.dev",
                "role", "finance-admin",
                "department", "Finance",
                "lastLogin", Instant.now().toString()));
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getUsers() {
        return ResponseEntity.ok(List.of(
                Map.of("userId", "user-9921", "name", "Priya Sharma", "role", "finance-admin"),
                Map.of("userId", "user-1042", "name", "Arjun Patel", "role", "platform-engineer"),
                Map.of("userId", "user-5567", "name", "Meera Iyer", "role", "compliance-officer")));
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createUser(@RequestBody(required = false) Map<String, Object> body) {
        return ResponseEntity.status(201).body(Map.of(
                "userId", UUID.randomUUID().toString(),
                "status", "CREATED",
                "message", "User created successfully",
                "timestamp", Instant.now().toString()));
    }
}
