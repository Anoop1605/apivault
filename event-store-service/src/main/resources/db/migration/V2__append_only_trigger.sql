CREATE OR REPLACE FUNCTION prevent_security_events_mutation()
RETURNS TRIGGER AS $$
BEGIN
	RAISE EXCEPTION 'security_events is append-only; % is not allowed', TG_OP;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_security_events_append_only ON security_events;

CREATE TRIGGER trg_security_events_append_only
BEFORE UPDATE OR DELETE ON security_events
FOR EACH ROW
EXECUTE FUNCTION prevent_security_events_mutation();

CREATE OR REPLACE FUNCTION prevent_events_mutation()
RETURNS TRIGGER AS $$
BEGIN
	RAISE EXCEPTION 'events is append-only; % is not allowed', TG_OP;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_events_append_only ON events;

CREATE TRIGGER trg_events_append_only
BEFORE UPDATE OR DELETE ON events
FOR EACH ROW
EXECUTE FUNCTION prevent_events_mutation();

DO $$
BEGIN
	IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'event_writer') THEN
		CREATE ROLE event_writer NOLOGIN;
	END IF;

	IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'sentinel_reader') THEN
		CREATE ROLE sentinel_reader NOLOGIN;
	END IF;
END;
$$;

GRANT USAGE ON SCHEMA public TO event_writer, sentinel_reader;
GRANT INSERT ON TABLE security_events TO event_writer;
GRANT INSERT ON TABLE events TO event_writer;
GRANT SELECT ON TABLE security_events, events, sessions_summary TO sentinel_reader;
