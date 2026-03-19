#!/bin/bash
# ============================================================
# Sentinel Demo Fixture Loader
# PRD §12.2 AC-17: loads 3 pre-built sessions into event store
#
# Usage:
#   ./fixtures/load_demo_sessions.sh
#
# Environment variables (with defaults):
#   DB_HOST     — PostgreSQL host       (default: localhost)
#   DB_PORT     — PostgreSQL port       (default: 5432)
#   DB_NAME     — database name         (default: sentinel_events)
#   DB_USER     — database user         (default: sentinel)
#   DB_PASS     — database password     (default: sentinel)
# ============================================================

set -e

DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-5432}"
DB_NAME="${DB_NAME:-sentinel_events}"
DB_USER="${DB_USER:-sentinel}"
DB_PASS="${DB_PASS:-sentinel}"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SQL_DIR="$SCRIPT_DIR/sql"

export PGPASSWORD="$DB_PASS"

echo "============================================"
echo "  Sentinel Demo Fixture Loader"
echo "  Target: $DB_USER@$DB_HOST:$DB_PORT/$DB_NAME"
echo "============================================"

run_sql() {
    local file="$1"
    local label="$2"
    echo ""
    echo ">> Loading: $label"
    psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -f "$file"
    echo "   Done."
}

run_sql "$SQL_DIR/F01_normal_session.sql"    "F01 — Normal Session"
run_sql "$SQL_DIR/F02_policy_violation.sql"  "F02 — Policy Violation"
run_sql "$SQL_DIR/F03_attack_sequence.sql"   "F03 — Attack Sequence"

echo ""
echo "============================================"
echo "  All 3 demo sessions loaded successfully."
echo ""
echo "  Session IDs:"
echo "  F01 (normal):    aaaaaaaa-0000-0000-0000-000000000001"
echo "  F02 (violation): bbbbbbbb-0000-0000-0000-000000000002"
echo "  F03 (attack):    cccccccc-0000-0000-0000-000000000003"
echo ""
echo "  Open dashboard: http://localhost:8084/dashboard"
echo "============================================"
