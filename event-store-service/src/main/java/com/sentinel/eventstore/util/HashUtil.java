package com.sentinel.eventstore.util;

import com.sentinel.eventstore.model.SecurityEvent;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;
import java.util.TreeMap;

public class HashUtil {

    public static String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));

            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                String s = Integer.toHexString(0xff & b);
                if (s.length() == 1) hex.append('0');
                hex.append(s);
            }
            return hex.toString();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String generateHash(SecurityEvent e) {

        StringBuilder sb = new StringBuilder();

        sb.append("session=").append(val(e.getSessionId())).append("|");
        sb.append("ts=").append(e.getTimestampNs()).append("|");
        sb.append("type=").append(val(e.getEventType())).append("|");
        sb.append("user=").append(val(e.getUserId())).append("|");
        sb.append("endpoint=").append(val(e.getEndpoint())).append("|");
        sb.append("method=").append(val(e.getHttpMethod())).append("|");
        sb.append("ip=").append(val(e.getSourceIp())).append("|");

        sb.append("rule=").append(val(e.getPolicyRuleId())).append("|");
        sb.append("ver=").append(val(e.getPolicyRuleVersion())).append("|");

        // ✅ SNAPSHOT INCLUDED
        sb.append("snapshot=").append(val(e.getPolicyRuleSnapshot())).append("|");

        sb.append("decision=").append(val(e.getDecision())).append("|");

        sb.append("risk=").append(e.getRiskScore() == null ? "null" : String.format("%.6f", e.getRiskScore())).append("|");

        sb.append("body=").append(val(e.getBodyHash())).append("|");
        sb.append("gw=").append(val(e.getGatewayVersion())).append("|");

        // ✅ SORTED MAPS
        sb.append("signals=").append(sortedMap(e.getRiskSignals())).append("|");
        sb.append("ctx=").append(sortedMap(e.getRequestContext())).append("|");

        sb.append("prev=").append(val(e.getPreviousHash()));

        return sha256(sb.toString());
    }

    private static String val(Object o) {
        return o == null ? "" : o.toString();
    }

    private static String sortedMap(Map<?, ?> map) {
        if (map == null) return "";

        TreeMap<?, ?> sorted = new TreeMap<>(map);
        StringBuilder sb = new StringBuilder();

        for (Map.Entry<?, ?> e : sorted.entrySet()) {
            sb.append(e.getKey()).append("=").append(e.getValue()).append(";");
        }

        return sb.toString();
    }
}