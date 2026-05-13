CREATE INDEX IF NOT EXISTS idx_security_events_session_ts
	ON security_events (session_id, timestamp_ns);
CREATE INDEX IF NOT EXISTS idx_security_events_timestamp
	ON security_events (timestamp_ns);
CREATE INDEX IF NOT EXISTS idx_security_events_event_type
	ON security_events (event_type);
CREATE INDEX IF NOT EXISTS idx_security_events_decision
	ON security_events (decision);
CREATE INDEX IF NOT EXISTS idx_security_events_endpoint
	ON security_events (endpoint);
CREATE INDEX IF NOT EXISTS idx_security_events_source_ip
	ON security_events (source_ip);
CREATE INDEX IF NOT EXISTS idx_security_events_risk_score
	ON security_events (risk_score);
CREATE INDEX IF NOT EXISTS idx_security_events_risk_signals_gin
	ON security_events USING GIN (risk_signals);
CREATE INDEX IF NOT EXISTS idx_security_events_request_context_gin
	ON security_events USING GIN (request_context);

CREATE INDEX IF NOT EXISTS idx_events_session_ts
	ON events (session_id, timestamp_ns);
CREATE INDEX IF NOT EXISTS idx_events_timestamp
	ON events (timestamp_ns);
CREATE INDEX IF NOT EXISTS idx_events_user_id
	ON events (user_id);
CREATE INDEX IF NOT EXISTS idx_events_decision
	ON events (decision);
CREATE INDEX IF NOT EXISTS idx_events_endpoint
	ON events (endpoint);

CREATE INDEX IF NOT EXISTS idx_sessions_summary_user_id
	ON sessions_summary (user_id);
CREATE INDEX IF NOT EXISTS idx_sessions_summary_last_seen_ns
	ON sessions_summary (last_seen_ns);
CREATE INDEX IF NOT EXISTS idx_sessions_summary_flagged
	ON sessions_summary (flagged);

CREATE INDEX IF NOT EXISTS idx_policy_rules_active_priority
	ON policy_rules (active, priority DESC);
CREATE INDEX IF NOT EXISTS idx_policy_rules_rule_id
	ON policy_rules (rule_id);
CREATE INDEX IF NOT EXISTS idx_policy_rules_conditions_gin
	ON policy_rules USING GIN (conditions);

CREATE INDEX IF NOT EXISTS idx_policy_rules_history_rule_version
	ON policy_rules_history (rule_id, rule_version DESC);
CREATE INDEX IF NOT EXISTS idx_policy_rules_history_activated_at
	ON policy_rules_history (activated_at DESC);
