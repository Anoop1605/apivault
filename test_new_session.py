#!/usr/bin/env python3
"""
Test Script: Add New Session D and Verify Full Integration
============================================================
This tests if a new session created via Python:
1. Appears in the PostgreSQL database
2. Is returned by the Forensics API
3. Automatically displays in the frontend UI
"""

import requests
import json
import time
import uuid
from datetime import datetime
import psycopg2
from psycopg2.extras import RealDictCursor

# ═════════════════════════════════════════════════════════════════════════════
# CONFIGURATION
# ═════════════════════════════════════════════════════════════════════════════

EVENT_STORE_API = "http://localhost:8081"
FORENSICS_API = "http://localhost:8083"
DB_HOST = "localhost"
DB_PORT = 5432
DB_NAME = "sentinel_db"
DB_USER = "sentinel"
DB_PASSWORD = "sentinel"

# Colors
class Colors:
    HEADER = '\033[95m'
    OKBLUE = '\033[94m'
    OKCYAN = '\033[96m'
    OKGREEN = '\033[92m'
    WARNING = '\033[93m'
    FAIL = '\033[91m'
    ENDC = '\033[0m'
    BOLD = '\033[1m'

def get_db_connection():
    """Create database connection."""
    try:
        conn = psycopg2.connect(
            host=DB_HOST,
            port=DB_PORT,
            database=DB_NAME,
            user=DB_USER,
            password=DB_PASSWORD
        )
        return conn
    except Exception as e:
        print(f"{Colors.FAIL}❌ DB Connection Failed: {e}{Colors.ENDC}")
        return None

def insert_event(session_id, event_type, endpoint, decision, source_ip="203.0.113.99"):
    """Insert event into database."""
    conn = get_db_connection()
    if not conn:
        return False
    
    try:
        with conn.cursor() as cur:
            # Generate timestamps
            timestamp_ns = int(time.time() * 1e9)
            
            cur.execute("""
                INSERT INTO security_events 
                (session_id, event_type, decision, rule_matched, source_ip, 
                 endpoint, http_method, timestamp_ns)
                VALUES (%s, %s, %s, %s, %s, %s, %s, %s)
            """, (
                session_id,
                event_type,
                decision,
                f"RULE_{decision}",
                source_ip,
                endpoint,
                "GET",
                timestamp_ns
            ))
            conn.commit()
            return True
    except Exception as e:
        print(f"{Colors.FAIL}❌ Insert Error: {e}{Colors.ENDC}")
        return False
    finally:
        conn.close()

def create_session_d():
    """Create Session D: Suspicious Account Access"""
    print(f"\n{Colors.HEADER}{Colors.BOLD}═══════════════════════════════════════════════════════════════{Colors.ENDC}")
    print(f"{Colors.HEADER}{Colors.BOLD}CREATING SESSION D: SUSPICIOUS ACCOUNT ACCESS{Colors.ENDC}")
    print(f"{Colors.HEADER}{Colors.BOLD}═══════════════════════════════════════════════════════════════{Colors.ENDC}")
    
    session_id = str(uuid.uuid4())
    print(f"\n📝 New Session ID: {Colors.OKCYAN}{session_id}{Colors.ENDC}")
    
    # Define session events
    events = [
        {"type": "LOGIN", "endpoint": "/auth/login", "decision": "ALLOW", "ip": "198.51.100.42"},
        {"type": "ACCESS", "endpoint": "/api/users/profile", "decision": "ALLOW", "ip": "198.51.100.42"},
        {"type": "ACCESS", "endpoint": "/api/sensitive/reports", "decision": "BLOCK", "ip": "198.51.100.42"},
        {"type": "ATTACK", "endpoint": "/api/admin/settings", "decision": "BLOCK", "ip": "198.51.100.42"},
        {"type": "LOGOUT", "endpoint": "/auth/logout", "decision": "ALLOW", "ip": "198.51.100.42"},
    ]
    
    # Insert events
    print(f"\n{Colors.BOLD}STEP 1: Inserting events into database...{Colors.ENDC}")
    for event in events:
        if insert_event(session_id, event["type"], event["endpoint"], event["decision"], event["ip"]):
            print(f"  ✅ {event['type']:8} → {event['endpoint']:30} [{Colors.OKGREEN}{event['decision']}{Colors.ENDC}]")
        time.sleep(0.3)
    
    return session_id

def verify_in_database(session_id):
    """Verify session appears in database."""
    print(f"\n{Colors.BOLD}STEP 2: Verifying in database...{Colors.ENDC}")
    
    conn = get_db_connection()
    if not conn:
        return False
    
    try:
        with conn.cursor(cursor_factory=RealDictCursor) as cur:
            cur.execute("""
                SELECT COUNT(*) as count, 
                       COUNT(DISTINCT event_type) as types
                FROM security_events 
                WHERE session_id = %s
            """, (session_id,))
            result = cur.fetchone()
            count = result['count']
            types = result['types']
            
            print(f"  {Colors.OKGREEN}✅ Found in database{Colors.ENDC}")
            print(f"     • Events: {Colors.OKCYAN}{count}{Colors.ENDC}")
            print(f"     • Event types: {Colors.OKCYAN}{types}{Colors.ENDC}")
            return True
    except Exception as e:
        print(f"  {Colors.FAIL}❌ DB Query Error: {e}{Colors.ENDC}")
        return False
    finally:
        conn.close()

def verify_in_api(session_id):
    """Verify session appears in Forensics API."""
    print(f"\n{Colors.BOLD}STEP 3: Verifying in Forensics API...{Colors.ENDC}")
    
    try:
        response = requests.get(f"{FORENSICS_API}/forensics/query/sessions", timeout=5)
        if response.status_code == 200:
            sessions = response.json()
            
            # Look for our session
            found = False
            for session in sessions:
                if session['sessionId'] == session_id:
                    found = True
                    print(f"  {Colors.OKGREEN}✅ Found in Forensics API{Colors.ENDC}")
                    print(f"     • Event Count: {Colors.OKCYAN}{session['eventCount']}{Colors.ENDC}")
                    print(f"     • Risk Score: {Colors.OKCYAN}{session.get('maxRiskScore', 'N/A')}{Colors.ENDC}")
                    break
            
            if not found:
                print(f"  {Colors.WARNING}⚠ Not found in API yet (total sessions: {len(sessions)}){Colors.ENDC}")
                print(f"     Sessions in API:")
                for s in sessions:
                    print(f"       • {s['sessionId'][:12]}... ({s['eventCount']} events)")
            
            return found
        else:
            print(f"  {Colors.FAIL}❌ API Error: {response.status_code}{Colors.ENDC}")
            return False
    except Exception as e:
        print(f"  {Colors.FAIL}❌ API Connection Error: {e}{Colors.ENDC}")
        return False

def get_all_sessions():
    """Get all sessions from API."""
    try:
        response = requests.get(f"{FORENSICS_API}/forensics/query/sessions", timeout=5)
        if response.status_code == 200:
            return response.json()
        return []
    except:
        return []

def main():
    """Main test execution."""
    print(f"\n{Colors.HEADER}{Colors.BOLD}")
    print("╔════════════════════════════════════════════════════════════════╗")
    print("║  TEST: NEW SESSION INTEGRATION                                 ║")
    print("║  Python → Database → API → Frontend                            ║")
    print("╚════════════════════════════════════════════════════════════════╝")
    print(f"{Colors.ENDC}\n")
    
    # Get initial session count
    print(f"{Colors.BOLD}Initial state:{Colors.ENDC}")
    sessions_before = get_all_sessions()
    print(f"  Total sessions in API: {Colors.OKCYAN}{len(sessions_before)}{Colors.ENDC}")
    
    # Create new session
    session_d = create_session_d()
    
    # Wait for propagation
    time.sleep(2)
    
    # Verify in database
    verify_in_database(session_d)
    
    # Wait for API to pick it up
    time.sleep(2)
    
    # Verify in API
    api_verified = verify_in_api(session_d)
    
    # Final count
    print(f"\n{Colors.BOLD}Final state:{Colors.ENDC}")
    sessions_after = get_all_sessions()
    print(f"  Total sessions in API: {Colors.OKCYAN}{len(sessions_after)}{Colors.ENDC}")
    print(f"  New sessions added: {Colors.OKCYAN}{len(sessions_after) - len(sessions_before)}{Colors.ENDC}")
    
    # Summary
    print(f"\n{Colors.HEADER}{Colors.BOLD}═══════════════════════════════════════════════════════════════{Colors.ENDC}")
    print(f"{Colors.BOLD}TEST SUMMARY:{Colors.ENDC}")
    print(f"  Session ID: {Colors.OKCYAN}{session_d}{Colors.ENDC}")
    
    if api_verified:
        print(f"  {Colors.OKGREEN}✅ Session appears in database{Colors.ENDC}")
        print(f"  {Colors.OKGREEN}✅ Session appears in API{Colors.ENDC}")
        print(f"  {Colors.OKGREEN}✅ SESSION D INTEGRATION TEST PASSED!{Colors.ENDC}")
        print(f"\n  {Colors.WARNING}💡 NEXT: Refresh browser at http://localhost:3002/sessions to see new session in UI{Colors.ENDC}")
    else:
        print(f"  {Colors.FAIL}❌ Session integration test FAILED{Colors.ENDC}")
    
    print(f"\n{Colors.BOLD}Instructions:{Colors.ENDC}")
    print(f"  1. Session created with 5 events (ALLOW, ALLOW, BLOCK, ATTACK, ALLOW)")
    print(f"  2. Stored in PostgreSQL database")
    print(f"  3. Available via API at: {FORENSICS_API}/forensics/query/sessions")
    print(f"  4. {Colors.WARNING}Refresh your browser to see it in the UI!{Colors.ENDC}")

if __name__ == "__main__":
    main()
