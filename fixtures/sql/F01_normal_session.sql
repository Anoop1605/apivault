DELETE FROM security_events
WHERE session_id = '11111111-1111-1111-1111-111111111111';

INSERT INTO security_events (
    id, session_id, timestamp_ns, event_type, decision, rule_matched, source_ip,
    endpoint, user_id, roles_json, http_method, policy_rule_id, policy_rule_version,
    policy_rule_snapshot_id, policy_rule_snapshot, risk_score, gateway_version
) VALUES
(
    gen_random_uuid(),
    '11111111-1111-1111-1111-111111111111',
    1707825800000000000,
    'REQUEST_RECEIVED',
    NULL,
    NULL,
    '10.0.0.11',
    '/api/users/profile',
    'user-1001',
    '["user"]'::jsonb,
    'GET',
    NULL,
    NULL,
    NULL,
    '["RULE-RISK-STRICT-01","RULE-ALLOW-USER-PROFILE-01","RULE-ALLOW-ADMIN-READ-01","RULE-ALLOW-PAYMENT-DELETE-01"]',
    0.12,
    '2.0.0'
),
(
    gen_random_uuid(),
    '11111111-1111-1111-1111-111111111111',
    1707825800100000000,
    'POLICY_ALLOWED',
    'ALLOW',
    'RULE-ALLOW-USER-PROFILE-01',
    '10.0.0.11',
    '/api/users/profile',
    'user-1001',
    '["user"]'::jsonb,
    'GET',
    'RULE-ALLOW-USER-PROFILE-01',
    1,
    'aaaaaaaa-0000-0000-0000-000000000001',
    '["RULE-RISK-STRICT-01","RULE-ALLOW-USER-PROFILE-01","RULE-ALLOW-ADMIN-READ-01","RULE-ALLOW-PAYMENT-DELETE-01"]',
    0.12,
    '2.0.0'
),
(
    gen_random_uuid(),
    '11111111-1111-1111-1111-111111111111',
    1707825800200000000,
    'REQUEST_FORWARDED',
    'ALLOW',
    'RULE-ALLOW-USER-PROFILE-01',
    '10.0.0.11',
    '/api/users/profile',
    'user-1001',
    '["user"]'::jsonb,
    'GET',
    'RULE-ALLOW-USER-PROFILE-01',
    1,
    'aaaaaaaa-0000-0000-0000-000000000001',
    '["RULE-RISK-STRICT-01","RULE-ALLOW-USER-PROFILE-01","RULE-ALLOW-ADMIN-READ-01","RULE-ALLOW-PAYMENT-DELETE-01"]',
    0.12,
    '2.0.0'
);
