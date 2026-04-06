package com.sentinel.forensics.event;

import com.sentinel.shared.dto.EventDTO;
import com.sentinel.shared.enums.Decision;
import com.sentinel.shared.enums.EventType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Mock event store returning the 3 pre-built demo fixture sessions.
 * Mirrors the data in fixtures/sql/F01, F02, F03.
 *
 * Session IDs:
 *   F01 normal:    aaaaaaaa-0000-0000-0000-000000000001
 *   F02 violation: bbbbbbbb-0000-0000-0000-000000000002
 *   F03 attack:    cccccccc-0000-0000-0000-000000000003
 */
public class MockEventStoreClient implements EventStoreClient {

    private static final UUID SESSION_NORMAL    =
            UUID.fromString("aaaaaaaa-0000-0000-0000-000000000001");
    private static final UUID SESSION_VIOLATION =
            UUID.fromString("bbbbbbbb-0000-0000-0000-000000000002");
    private static final UUID SESSION_ATTACK    =
            UUID.fromString("cccccccc-0000-0000-0000-000000000003");

    @Override
    public List<EventDTO> fetchEvents(UUID sessionId) {
        if (SESSION_NORMAL.equals(sessionId))    return normalSession();
        if (SESSION_VIOLATION.equals(sessionId)) return violationSession();
        if (SESSION_ATTACK.equals(sessionId))    return attackSession();
        // Unknown session — return generic mock events
        return genericEvents(sessionId);
    }

    // ------------------------------------------------------------------
    // F01 — Normal session (4 events, all ALLOW)
    // ------------------------------------------------------------------
    private List<EventDTO> normalSession() {
        return new ArrayList<>(List.of(
            event(SESSION_NORMAL, 1707825600000000000L, "LOGIN",  "ALLOW", "ALLOW_ALL",  "GET",    "/auth/login",      "user-001", 0.1),
            event(SESSION_NORMAL, 1707825601000000000L, "ACCESS", "ALLOW", "ALLOW_GET",  "GET",    "/api/users",       "user-001", 0.1),
            event(SESSION_NORMAL, 1707825602000000000L, "ACCESS", "ALLOW", "ALLOW_GET",  "GET",    "/api/payments",    "user-001", 0.1),
            event(SESSION_NORMAL, 1707825603000000000L, "LOGOUT", "ALLOW", "ALLOW_ALL",  "GET",    "/auth/logout",     "user-001", 0.1)
        ));
    }

    // ------------------------------------------------------------------
    // F02 — Policy violation (5 events, DELETE blocked at step 3 + 4)
    // ------------------------------------------------------------------
    private List<EventDTO> violationSession() {
        return new ArrayList<>(List.of(
            event(SESSION_VIOLATION, 1707825700000000000L, "LOGIN",  "ALLOW", "ALLOW_ALL",   "POST",   "/auth/login",              "user-002", 0.2),
            event(SESSION_VIOLATION, 1707825701000000000L, "ACCESS", "ALLOW", "ALLOW_GET",   "GET",    "/api/admin/users",         "user-002", 0.3),
            event(SESSION_VIOLATION, 1707825702000000000L, "ACCESS", "DENY",  "BLOCK_DELETE","DELETE", "/api/payments/txn-9921",   "user-002", 0.5),
            event(SESSION_VIOLATION, 1707825703000000000L, "ACCESS", "DENY",  "BLOCK_DELETE","DELETE", "/api/payments/txn-9921",   "user-002", 0.6),
            event(SESSION_VIOLATION, 1707825704000000000L, "LOGOUT", "ALLOW", "ALLOW_ALL",   "POST",   "/auth/logout",             "user-002", 0.2)
        ));
    }

    // ------------------------------------------------------------------
    // F03 — Attack sequence (6 events, attack blocked at step 4-6)
    // What-if demo: with BLOCK_ACCESS_RAPID rule, blocked at step 2
    // ------------------------------------------------------------------
    private List<EventDTO> attackSession() {
        return new ArrayList<>(List.of(
            event(SESSION_ATTACK, 1707825800000000000L, "ACCESS", "ALLOW", "ALLOW_GET",   "GET",    "/api/users",               null,       0.4),
            event(SESSION_ATTACK, 1707825800100000000L, "ACCESS", "ALLOW", "ALLOW_GET",   "GET",    "/api/admin",               null,       0.6),
            event(SESSION_ATTACK, 1707825800200000000L, "ACCESS", "ALLOW", "ALLOW_GET",   "GET",    "/api/payments?id=1 OR 1=1",null,       0.7),
            event(SESSION_ATTACK, 1707825800300000000L, "ATTACK", "DENY",  "BLOCK_ATTACK","DELETE", "/api/admin/users/delete-all",null,     0.85),
            event(SESSION_ATTACK, 1707825800400000000L, "ATTACK", "DENY",  "BLOCK_ATTACK","POST",   "/api/admin/roles/assign",  null,       0.85),
            event(SESSION_ATTACK, 1707825800500000000L, "ATTACK", "DENY",  "BLOCK_DELETE","DELETE", "/api/payments/delete-all", null,       0.9)
        ));
    }

    // ------------------------------------------------------------------
    // Generic fallback
    // ------------------------------------------------------------------
    private List<EventDTO> genericEvents(UUID sessionId) {
        return new ArrayList<>(List.of(
            event(sessionId, 1000L, "LOGIN",  "ALLOW", "ALLOW_ALL", "GET", "/auth/login",  "user-generic", 0.1),
            event(sessionId, 2000L, "ACCESS", "ALLOW", "ALLOW_GET", "GET", "/api/resource","user-generic", 0.1),
            event(sessionId, 3000L, "LOGOUT", "ALLOW", "ALLOW_ALL", "GET", "/auth/logout", "user-generic", 0.1)
        ));
    }

    // ------------------------------------------------------------------
    // Builder
    // ------------------------------------------------------------------
    private EventDTO event(UUID sessionId, long timestampNs,
                           String eventTypeStr, String decisionStr,
                           String policyRuleId, String httpMethod,
                           String endpoint, String userId, double riskScore) {
        EventDTO e = new EventDTO();
        e.setEventId(UUID.randomUUID());
        e.setSessionId(sessionId);
        e.setTimestampNs(timestampNs);
        e.setEventType(EventType.valueOf(eventTypeStr));
        e.setDecision(Decision.valueOf(decisionStr));
        e.setPolicyRuleId(policyRuleId);
        e.setPolicyRuleVersion(1);
        e.setHttpMethod(httpMethod);
        e.setEndpoint(endpoint);
        e.setUserId(userId);
        e.setRiskScore(riskScore);
        e.setGatewayVersion("1.0.0");
        return e;
    }
}
