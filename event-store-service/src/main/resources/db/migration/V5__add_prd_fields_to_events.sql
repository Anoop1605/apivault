-- V5: Align legacy 'events' table with PRD Section 8.1 and 10.3 requirements.
ALTER TABLE events ADD COLUMN IF NOT EXISTS policy_rule_id VARCHAR(255);
ALTER TABLE events ADD COLUMN IF NOT EXISTS policy_rule_version INTEGER;
ALTER TABLE events ADD COLUMN IF NOT EXISTS policy_rule_snapshot_id UUID;
ALTER TABLE events ADD COLUMN IF NOT EXISTS body_hash VARCHAR(128);
