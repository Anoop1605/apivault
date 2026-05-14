ALTER TABLE security_events
    ADD COLUMN IF NOT EXISTS previous_hash TEXT,
    ADD COLUMN IF NOT EXISTS policy_rule_snapshot TEXT;

CREATE TABLE IF NOT EXISTS alerts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    session_id UUID NOT NULL,
    user_id VARCHAR(255),
    endpoint TEXT,
    reason TEXT,
    risk_score DOUBLE PRECISION NOT NULL DEFAULT 0,
    timestamp_ns BIGINT NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'OPEN'
);

CREATE INDEX IF NOT EXISTS idx_alerts_session_ts
    ON alerts (session_id, timestamp_ns DESC);

CREATE INDEX IF NOT EXISTS idx_alerts_status
    ON alerts (status);
