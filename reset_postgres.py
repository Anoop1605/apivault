import psycopg2
import os

def clear_postgres():
    # Try common database names and credentials from application.properties / docker-compose
    configs = [
        {"dbname": "apivault", "user": "postgres", "password": "postgres", "host": "localhost"},
        {"dbname": "sentinel_db", "user": "sentinel", "password": "sentinel", "host": "localhost"},
        {"dbname": "sentinel_db", "user": "postgres", "password": "postgres", "host": "localhost"},
    ]

    tables = ["event", "policy", "session", "audit_log", "flyway_schema_history"]

    for config in configs:
        conn = None
        try:
            print(f"Attempting to connect to {config['dbname']}...")
            conn = psycopg2.connect(**config)
            conn.autocommit = True
            cur = conn.cursor()
            
            # Get list of all tables in public schema
            cur.execute("SELECT table_name FROM information_schema.tables WHERE table_schema = 'public'")
            db_tables = [r[0] for r in cur.fetchall()]
            
            print(f"Connected! Found tables: {db_tables}")
            
            for table in db_tables:
                if table == 'flyway_schema_history':
                    continue
                print(f"Truncating table {table}...")
                cur.execute(f'TRUNCATE TABLE "{table}" CASCADE;')
            
            print("Successfully cleared database.")
            conn.close()
            return True
        except Exception as e:
            print(f"Failed for {config['dbname']}: {e}")
            if conn:
                conn.close()
    
    return False

if __name__ == "__main__":
    if clear_postgres():
        print("Database reset complete.")
    else:
        print("Could not reset database. Check if Postgres is running and accessible.")
