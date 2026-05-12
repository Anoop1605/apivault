package com.sentinel.eventstore.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.sentinel.eventstore.model.SecurityEvent;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;
import java.util.TreeMap;

/**
 * CanonicalHasher — Implements PRD Section 5.5 (Tamper-Proofing).
 * Ensures deterministic SHA-256 hashing by using alphabetically sorted keys
 * and compact JSON serialization.
 */
@Component
public class CanonicalHasher {

    private final ObjectMapper objectMapper;

    public CanonicalHasher() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
    }

    /**
     * Generates a SHA-256 hash of the security event using canonical serialization (PRD Section 10.3).
     * Format: field1=val1|field2=val2|...
     */
    public String calculateHash(SecurityEvent event) {
        try {
            StringBuilder sb = new StringBuilder();
            
            // PRD Section 10.3 fields in exact order
            appendField(sb, "event_id", event.getId());
            appendField(sb, "timestamp_ns", event.getTimestampNs());
            appendField(sb, "event_type", event.getEventType());
            appendField(sb, "session_id", event.getSessionId());
            appendField(sb, "user_id", event.getUserId());
            appendField(sb, "endpoint", event.getEndpoint());
            appendField(sb, "http_method", event.getHttpMethod());
            appendField(sb, "decision", event.getDecision());
            appendField(sb, "policy_rule_id", event.getPolicyRuleId());
            appendField(sb, "policy_rule_version", event.getPolicyRuleVersion());
            appendField(sb, "risk_score", event.getRiskScore());
            appendField(sb, "body_hash", event.getBodyHash());
            appendField(sb, "gateway_version", event.getGatewayVersion());

            String canonicalString = sb.toString();
            
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(canonicalString.getBytes(StandardCharsets.UTF_8));
            
            return bytesToHex(hashBytes);
        } catch (Exception e) {
            throw new RuntimeException("Canonical hashing failed", e);
        }
    }

    private void appendField(StringBuilder sb, String name, Object value) {
        sb.append(name).append("=");
        if (value == null) {
            sb.append("null");
        } else if (value instanceof Double) {
            // PRD: "risk_score (6 decimal place string)"
            sb.append(String.format("%.6f", (Double) value));
        } else {
            sb.append(value.toString());
        }
        sb.append("|");
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
