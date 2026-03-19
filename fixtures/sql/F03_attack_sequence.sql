-- ============================================================
-- FIXTURE F03 — Attack Sequence
-- Session: cccccccc-0000-0000-0000-000000000003
-- User: unknown (no valid JWT — external attacker)
-- Pattern: rapid scanning → injection attempt → privilege escalation
-- Risk score: high (0.85) — triggers RISK_FLAGGED
-- Key forensic demo: what-if shows attack blocked at step 2
--                    with stricter rules (currently blocked at step 4)
-- ============================================================

INSERT INTO security_events (
    id, session_id, timestamp_ns, event_type, decision,
    rule_matched, source_ip, endpoint
) VALUES

-- Step 1: Initial probe — allowed (looks like normal access)
(
    gen_random_uuid(),
    'cccccccc-0000-0000-0000-000000000003',
    1707825800000000000,
    'ACCESS', 'ALLOW',
    'ALLOW_GET', '185.220.101.45', '/api/users'
),

-- Step 2: Rapid endpoint scan — allowed (rate limit not yet triggered)
(
    gen_random_uuid(),
    'cccccccc-0000-0000-0000-000000000003',
    1707825800100000000,
    'ACCESS', 'ALLOW',
    'ALLOW_GET', '185.220.101.45', '/api/admin'
),

-- Step 3: SQL injection attempt on payments endpoint — allowed (no injection rule)
(
    gen_random_uuid(),
    'cccccccc-0000-0000-0000-000000000003',
    1707825800200000000,
    'ACCESS', 'ALLOW',
    'ALLOW_GET', '185.220.101.45', '/api/payments?id=1 OR 1=1'
),

-- Step 4: ATTACK event — risk scorer flags this, BLOCKED
(
    gen_random_uuid(),
    'cccccccc-0000-0000-0000-000000000003',
    1707825800300000000,
    'ATTACK', 'BLOCK',
    'BLOCK_ATTACK', '185.220.101.45', '/api/admin/users/delete-all'
),

-- Step 5: Privilege escalation attempt — BLOCKED
(
    gen_random_uuid(),
    'cccccccc-0000-0000-0000-000000000003',
    1707825800400000000,
    'ATTACK', 'BLOCK',
    'BLOCK_ATTACK', '185.220.101.45', '/api/admin/roles/assign'
),

-- Step 6: Final DELETE attempt — BLOCKED
(
    gen_random_uuid(),
    'cccccccc-0000-0000-0000-000000000003',
    1707825800500000000,
    'ATTACK', 'BLOCK',
    'BLOCK_DELETE', '185.220.101.45', '/api/payments/delete-all'
);

-- NOTE for what-if demo:
-- With rule BLOCK_ATTACK added at position 1 in the snapshot,
-- firstDivergenceStep = 4 (attack blocked at step 4 in original,
-- but would be blocked at step 2 — the rapid scan — with stricter rules).
