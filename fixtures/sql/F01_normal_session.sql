-- ============================================================
-- FIXTURE F01 — Normal Session
-- Session: aaaaaaaa-0000-0000-0000-000000000001
-- User: user-001 (standard API consumer)
-- Pattern: LOGIN → browse → logout — all ALLOW
-- Risk score: low (0.1)
-- ============================================================

INSERT INTO security_events (
    id, session_id, timestamp_ns, event_type, decision,
    rule_matched, source_ip, endpoint
) VALUES

-- Step 1: Login
(
    gen_random_uuid(),
    'aaaaaaaa-0000-0000-0000-000000000001',
    1707825600000000000,
    'LOGIN', 'ALLOW',
    'ALLOW_ALL', '192.168.1.10', '/auth/login'
),

-- Step 2: Read users
(
    gen_random_uuid(),
    'aaaaaaaa-0000-0000-0000-000000000001',
    1707825601000000000,
    'ACCESS', 'ALLOW',
    'ALLOW_GET', '192.168.1.10', '/api/users'
),

-- Step 3: Read payments
(
    gen_random_uuid(),
    'aaaaaaaa-0000-0000-0000-000000000001',
    1707825602000000000,
    'ACCESS', 'ALLOW',
    'ALLOW_GET', '192.168.1.10', '/api/payments'
),

-- Step 4: Logout
(
    gen_random_uuid(),
    'aaaaaaaa-0000-0000-0000-000000000001',
    1707825603000000000,
    'LOGOUT', 'ALLOW',
    'ALLOW_ALL', '192.168.1.10', '/auth/logout'
);
