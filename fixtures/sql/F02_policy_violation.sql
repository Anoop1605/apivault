DELETE FROM security_events
WHERE session_id = '22222222-2222-2222-2222-222222222222';

INSERT INTO security_events (
    id, session_id, timestamp_ns, event_type, decision, rule_matched, source_ip,
    endpoint, user_id, roles_json, http_method, policy_rule_id, policy_rule_version,
    policy_rule_snapshot_id, policy_rule_snapshot, risk_score, gateway_version
) VALUES
(
    gen_random_uuid(),
    '22222222-2222-2222-2222-222222222222',
    1707868800000000000,
    'REQUEST_RECEIVED',
    NULL,
    NULL,
    '10.0.1.50',
    '/api/payments/delete',
    'user-9921',
    '["finance-admin"]'::jsonb,
    'DELETE',
    NULL,
    NULL,
    NULL,
    '["RULE-RISK-STRICT-01","RULE-ALLOW-USER-PROFILE-01","RULE-ALLOW-ADMIN-READ-01","RULE-ALLOW-PAYMENT-DELETE-01"]',
    0.40,
    '2.0.0'
),
(
    gen_random_uuid(),
    '22222222-2222-2222-2222-222222222222',
    1707868800100000000,
    'POLICY_DENIED',
    'DENY',
    'RULE-ALLOW-PAYMENT-DELETE-01',
    '10.0.1.50',
    '/api/payments/delete',
    'user-9921',
    '["finance-admin"]'::jsonb,
    'DELETE',
    'RULE-ALLOW-PAYMENT-DELETE-01',
    1,
    'bbbbbbbb-0000-0000-0000-000000000002',
    '["RULE-RISK-STRICT-01","RULE-ALLOW-USER-PROFILE-01","RULE-ALLOW-ADMIN-READ-01","RULE-ALLOW-PAYMENT-DELETE-01"]',
    0.40,
    '2.0.0'
);
