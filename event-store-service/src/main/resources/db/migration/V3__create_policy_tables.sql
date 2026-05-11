CREATE TABLE IF NOT EXISTS policy_rules (
	id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
	rule_id VARCHAR(120) NOT NULL UNIQUE,
	name VARCHAR(255) NOT NULL,
	description TEXT,
	priority INTEGER NOT NULL DEFAULT 0,
	active BOOLEAN NOT NULL DEFAULT TRUE,
	conditions JSONB NOT NULL DEFAULT '[]'::jsonb,
	decision VARCHAR(32) NOT NULL DEFAULT 'ALLOW',
	version INTEGER NOT NULL DEFAULT 1,
	created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
	updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS policy_rules_history (
	snapshot_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
	rule_id VARCHAR(120) NOT NULL,
	rule_version INTEGER NOT NULL,
	snapshot_json JSONB NOT NULL,
	activated_by VARCHAR(255),
	activated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
	is_active BOOLEAN NOT NULL DEFAULT TRUE,
	reason TEXT,
	CONSTRAINT fk_policy_rules_history_rule
		FOREIGN KEY (rule_id) REFERENCES policy_rules(rule_id) ON DELETE CASCADE
);

GRANT SELECT ON TABLE policy_rules, policy_rules_history TO sentinel_reader;
