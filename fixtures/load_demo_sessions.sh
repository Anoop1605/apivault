#!/bin/bash

set -e

DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-5432}"
DB_NAME="${DB_NAME:-sentinel_db}"
DB_USER="${DB_USER:-sentinel}"
DB_PASS="${DB_PASS:-sentinel}"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SQL_DIR="$SCRIPT_DIR/sql"

export PGPASSWORD="$DB_PASS"

echo "Loading Sentinel demo sessions into $DB_USER@$DB_HOST:$DB_PORT/$DB_NAME"

psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -f "$SQL_DIR/F01_normal_session.sql"
psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -f "$SQL_DIR/F02_policy_violation.sql"
psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -f "$SQL_DIR/F03_attack_sequence.sql"

echo ""
echo "Loaded sessions:"
echo "  normal   11111111-1111-1111-1111-111111111111"
echo "  denied   22222222-2222-2222-2222-222222222222"
echo "  attack   33333333-3333-3333-3333-333333333333"
echo ""
echo "Key API endpoints:"
echo "  GET  http://localhost:8083/forensics/query/sessions"
echo "  GET  http://localhost:8083/forensics/query/sessions/33333333-3333-3333-3333-333333333333/report"
echo "  POST http://localhost:8083/forensics/replay"
echo "  POST http://localhost:8083/forensics/sessions/33333333-3333-3333-3333-333333333333/whatif"
