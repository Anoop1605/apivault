#!/bin/bash
# ============================================================
# Sentinel Demo Reset Script
# PRD FR-ES-08: manual cleanup script for demo environment
#
# Deletes all events for the 3 fixture sessions so fixtures
# can be re-loaded cleanly.
#
# Usage:
#   ./fixtures/reset_demo.sh
# ============================================================

set -e

DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-5432}"
DB_NAME="${DB_NAME:-sentinel_events}"
DB_USER="${DB_USER:-sentinel}"
DB_PASS="${DB_PASS:-sentinel}"

export PGPASSWORD="$DB_PASS"

echo "============================================"
echo "  Sentinel Demo Reset"
echo "  WARNING: This will delete all fixture data"
echo "============================================"
read -p "  Are you sure? (yes/no): " confirm
if [ "$confirm" != "yes" ]; then
    echo "  Aborted."
    exit 0
fi

psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" <<'SQL'
DELETE FROM security_events WHERE session_id IN (
    'aaaaaaaa-0000-0000-0000-000000000001',
    'bbbbbbbb-0000-0000-0000-000000000002',
    'cccccccc-0000-0000-0000-000000000003'
);
SQL

echo ""
echo "  Demo data cleared. Run load_demo_sessions.sh to reload."
echo "============================================"
