package com.sentinel.eventstore.util;

import com.sentinel.eventstore.model.SecurityEvent;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class HashUtil {

    // 🔥 SHA-256 HASH FUNCTION (used in EventWriter)
    public static String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));

            StringBuilder hex = new StringBuilder();

            for (byte b : hash) {
                String s = Integer.toHexString(0xff & b);
                if (s.length() == 1) {
                    hex.append('0');
                }
                hex.append(s);
            }

            return hex.toString();

        } catch (Exception e) {
            throw new RuntimeException("Hashing failed", e);
        }
    }

    // 🔥 USED BY verifyChain() (must match EventWriter logic)
    public static String generateHash(SecurityEvent event) {

        String data =
                String.valueOf(event.getSessionId()) +
                event.getTimestampNs() +
                event.getEventType() +
                event.getUserId() +
                event.getEndpoint() +
                event.getSourceIp() +
                event.getPreviousHash();  // 🔥 IMPORTANT (chain linking)

        return sha256(data);
    }
}