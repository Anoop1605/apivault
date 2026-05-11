-- Baseline schema for Sentinel event storage.
CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS security_events (
	id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
	session_id UUID NOT NULL,
	timestamp_ns BIGINT NOT NULL,
	event_type VARCHAR(64) NOT NULL,
	decision VARCHAR(32),
	rule_matched VARCHAR(255),
	source_ip VARCHAR(64),
	endpoint TEXT,

	user_id VARCHAR(255),
	roles_json JSONB,
	http_method VARCHAR(16),
	user_agent TEXT,
	policy_rule_id VARCHAR(255),
	policy_rule_version INTEGER,
	policy_rule_snapshot_id UUID,
	risk_score DOUBLE PRECISION,
	risk_signals JSONB,
	request_context JSONB,
	body_hash VARCHAR(128),
	event_hash VARCHAR(128),
	gateway_version VARCHAR(64),
	created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Legacy compatibility table used by the older com.apivault package.
CREATE TABLE IF NOT EXISTS events (
	event_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
	timestamp_ns BIGINT NOT NULL,
	event_type VARCHAR(64),
	session_id UUID,
	user_id VARCHAR(255),
	endpoint TEXT,
	http_method VARCHAR(16),
	source_ip VARCHAR(64),
	decision VARCHAR(32),
	risk_score DOUBLE PRECISION,
	gateway_version VARCHAR(32),
	previous_hash TEXT,
	current_hash TEXT,
	created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS sessions_summary (
	session_id UUID PRIMARY KEY,
	first_seen_ns BIGINT,
	last_seen_ns BIGINT,
	event_count INTEGER NOT NULL DEFAULT 0,
	max_risk_score DOUBLE PRECISION,
	deny_count INTEGER NOT NULL DEFAULT 0,
	user_id VARCHAR(255),
	flagged BOOLEAN NOT NULL DEFAULT FALSE,
	endpoints_accessed JSONB,
	hash VARCHAR(128),
	updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
