package com.sentinel.forensics.engine;

import com.sentinel.shared.dto.EventDTO;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Canonical SHA-256 event hash computation.
 *
 * Implements the exact spec from PRD §10.3:
 * Fields (in this exact order):
 *   event_id, timestamp_ns, event_type, session_id, user_id,
 *   endpoint, http_method, decision, policy_rule_id,
 *   policy_rule_version, risk_score, body_hash, gateway_version
 *
 * Format: field_name=field_value| (pipe-separated, NULL -> literal "null")
 * Encoding: UTF-8 bytes -> SHA-256 -> lowercase hex (64 chars)
 */
public class ReplayHashUtil {

    private ReplayHashUtil() {}

    /**
     * Computes the canonical SHA-256 hash for a single event per PRD §10.3.
     */
    public static String computeEventHash(EventDTO event) {
        String canonical = buildCanonicalString(event);
        return sha256Hex(canonical);
    }

    /**
     * Computes a session-level hash by chaining all event hashes.
     * Used for replay report tamper detection.
     */
    public static String computeSessionHash(java.util.List<EventDTO> events,
                                            com.sentinel.shared.dto.PolicySnapshot snapshot) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            for (EventDTO event : events) {
                String eventHash = computeEventHash(event);
                digest.update(eventHash.getBytes(StandardCharsets.UTF_8));
            }
            // Include snapshot rules in session hash
            if (snapshot != null && snapshot.getRules() != null) {
                for (String rule : snapshot.getRules()) {
                    digest.update(rule.getBytes(StandardCharsets.UTF_8));
                }
            }
            return toHex(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    // -------------------------------------------------------------------------
    // PRD §10.3 canonical string builder
    // -------------------------------------------------------------------------

    static String buildCanonicalString(EventDTO e) {
        StringBuilder sb = new StringBuilder();

        // 1. event_id
        sb.append("event_id=").append(nullStr(e.getEventId())).append("|");
        // 2. timestamp_ns
        sb.append("timestamp_ns=").append(e.getTimestampNs()).append("|");
        // 3. event_type
        sb.append("event_type=").append(nullStr(e.getEventType())).append("|");
        // 4. session_id
        sb.append("session_id=").append(nullStr(e.getSessionId())).append("|");
        // 5. user_id
        sb.append("user_id=").append(nullStr(e.getUserId())).append("|");
        // 6. endpoint
        sb.append("endpoint=").append(nullStr(e.getEndpoint())).append("|");
        // 7. http_method
        sb.append("http_method=").append(nullStr(e.getHttpMethod())).append("|");
        // 8. decision
        sb.append("decision=").append(nullStr(e.getDecision())).append("|");
        // 9. policy_rule_id
        sb.append("policy_rule_id=").append(nullStr(e.getPolicyRuleId())).append("|");
        // 10. policy_rule_version
        sb.append("policy_rule_version=").append(nullStr(e.getPolicyRuleVersion())).append("|");
        // 11. risk_score — 6 decimal places or "null"
        sb.append("risk_score=").append(riskScoreStr(e.getRiskScore())).append("|");
        // 12. body_hash
        sb.append("body_hash=").append(nullStr(e.getBodyHash())).append("|");
        // 13. gateway_version
        sb.append("gateway_version=").append(nullStr(e.getGatewayVersion())).append("|");

        return sb.toString();
    }

    private static String nullStr(Object value) {
        return value == null ? "null" : value.toString();
    }

    private static String riskScoreStr(Float riskScore) {
        if (riskScore == null) return "null";
        return String.format("%.6f", riskScore);
    }

    static String sha256Hex(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return toHex(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
