package com.sentinel.payment;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getPayments() {
        return ResponseEntity.ok(List.of(
                Map.of("id", "pay-1001", "amount", 1499.0, "currency", "INR", "status", "COMPLETED"),
                Map.of("id", "pay-1002", "amount", 299.0, "currency", "INR", "status", "PENDING")));
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createPayment(@RequestBody(required = false) Map<String, Object> body) {
        return ResponseEntity.status(201).body(Map.of(
                "id", UUID.randomUUID().toString(),
                "amount", body != null ? body.getOrDefault("amount", 0) : 0,
                "currency", body != null ? body.getOrDefault("currency", "INR") : "INR",
                "status", "CREATED",
                "timestamp", Instant.now().toString()));
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Map<String, Object>> deletePayment() {
        return ResponseEntity.ok(Map.of(
                "deleted", true,
                "status", "COMPLETED",
                "timestamp", Instant.now().toString()));
    }
}
