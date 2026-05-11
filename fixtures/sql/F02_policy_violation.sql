-- ============================================================
-- FIXTURE F02 — Policy Violation
-- Session: bbbbbbbb-0000-0000-0000-000000000002
-- User: user-002 (internal engineer attempting destructive ops)
-- Pattern: LOGIN → read → DELETE blocked → retry → LOGOUT
-- Risk score: medium (0.5)
-- Key: BLOCK_DELETE rule fires at step 3
-- ============================================================

INSERT INTO security_events (
    id, session_id, timestamp_ns, event_type, decision,
    rule_matched, source_ip, endpoint
) VALUES

-- Step 1: Login
(
    gen_random_uuid(),
    'bbbbbbbb-0000-0000-0000-000000000002',
    1707825700000000000,
    'LOGIN', 'ALLOW',
    'ALLOW_ALL', '10.0.0.55', '/auth/login'
),

-- Step 2: Read admin users — allowed
(
    gen_random_uuid(),
    'bbbbbbbb-0000-0000-0000-000000000002',
    1707825701000000000,
    'ACCESS', 'ALLOW',
    'ALLOW_GET', '10.0.0.55', '/api/admin/users'
),

-- Step 3: DELETE payment record — BLOCKED by policy
(
    gen_random_uuid(),
    'bbbbbbbb-0000-0000-0000-000000000002',
    1707825702000000000,
    'ACCESS', 'BLOCK',
    'BLOCK_DELETE', '10.0.0.55', '/api/payments/txn-9921'
),

-- Step 4: Retry DELETE — BLOCKED again
(
    gen_random_uuid(),
    'bbbbbbbb-0000-0000-0000-000000000002',
    1707825703000000000,
    'ACCESS', 'BLOCK',
    'BLOCK_DELETE', '10.0.0.55', '/api/payments/txn-9921'
),

-- Step 5: Logout
(
    gen_random_uuid(),
    'bbbbbbbb-0000-0000-0000-000000000002',
    1707825704000000000,
    'LOGOUT', 'ALLOW',
    'ALLOW_ALL', '10.0.0.55', '/auth/logout'
);
