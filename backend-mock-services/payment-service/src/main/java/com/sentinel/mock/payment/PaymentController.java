package com.sentinel.mock.payment;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Mock Payment Service — routing target for the Sentinel gateway.
 * Provides endpoints: GET /api/payments, POST /api/payments, DELETE
 * /api/payments/delete
 */
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getPayments() {
        return ResponseEntity.ok(List.of(
                Map.of("id", UUID.randomUUID().toString(), "amount", 150.00, "currency", "USD",
                        "status", "COMPLETED", "timestamp", Instant.now().toString()),
                Map.of("id", UUID.randomUUID().toString(), "amount", 299.99, "currency", "USD",
                        "status", "PENDING", "timestamp", Instant.now().toString()),
                Map.of("id", UUID.randomUUID().toString(), "amount", 75.50, "currency", "EUR",
                        "status", "COMPLETED", "timestamp", Instant.now().toString())));
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createPayment(@RequestBody(required = false) Map<String, Object> body) {
        return ResponseEntity.status(201).body(Map.of(
                "id", UUID.randomUUID().toString(),
                "status", "CREATED",
                "message", "Payment created successfully",
                "timestamp", Instant.now().toString()));
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Map<String, Object>> deletePayment() {
        return ResponseEntity.ok(Map.of(
                "status", "DELETED",
                "message", "Payment deleted successfully",
                "timestamp", Instant.now().toString()));
    }
}
