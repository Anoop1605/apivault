import os
import sys
import sqlite3
from pathlib import Path
import logging

# ------------------------------------------------------------
# Configuration & validation
# ------------------------------------------------------------
REQUIRED_ENV = [
    "GATEWAY_BASE_URL",
    "USER_TOKEN",
    "ADMIN_TOKEN",
    "ATTACKER_TOKEN",
    "FORENSICS_ADMIN_TOKEN",
]
missing = [var for var in REQUIRED_ENV if not os.getenv(var)]
if missing:
    raise RuntimeError(
        f"Cannot run reset because the following environment variables are missing: {', '.join(missing)}"
    )

# Resolve DB path (project root -> sentinel_events.db)
BASE_DIR = Path(__file__).parent
DB_PATH = BASE_DIR / "sentinel_events.db"
if not DB_PATH.is_file():
    raise FileNotFoundError(f"SQLite DB not found at {DB_PATH}")

logging.basicConfig(level=logging.INFO, format="[%(asctime)s] %(levelname)s: %(message)s")
logger = logging.getLogger(__name__)

# ------------------------------------------------------------
# Helper: wipe all tables while preserving schema
# ------------------------------------------------------------
def truncate_tables(conn: sqlite3.Connection) -> None:
    cursor = conn.cursor()
    # List of tables we know exist (adjust if schema changes)
    tables = ["event", "policy", "session", "audit_log"]
    for tbl in tables:
        logger.info(f"Clearing table: {tbl}")
        cursor.execute(f"DELETE FROM {tbl};")
    conn.commit()
    # Optional: reclaim space
    cursor.execute("VACUUM;")
    logger.info("Database vacuumed – free space reclaimed.")

# ------------------------------------------------------------
# Main execution flow
# ------------------------------------------------------------
def main() -> None:
    logger.info("--- Resetting Sentinel event store ---")
    with sqlite3.connect(str(DB_PATH)) as conn:
        truncate_tables(conn)
    logger.info("All existing events removed. Starting fresh traffic generation.")

    # Ensure the project root is in PYTHONPATH for imports
    sys.path.insert(0, str(BASE_DIR))
    try:
        from main import main as run_demo
    except Exception as exc:
        logger.error("Failed to import the demo entry point: %s", exc)
        raise

    # Run the traffic generator – this will populate the DB with fresh events
    run_demo()
    logger.info("Demo completed – new events should now be present with no empty columns.")

if __name__ == "__main__":
    main()
