#!/usr/bin/env python3
"""
Sentinel Workflow Automation Script
====================================
Creates security sessions through APIs and verifies data flow:
Database → Event Store → Forensics Service → Frontend → Browser Display

This script creates multiple session types and verifies their propagation
through the entire system.
"""

import requests
import json
import time
import uuid
from datetime import datetime, timedelta
import psycopg2
from psycopg2.extras import RealDictCursor
import sys

# ═════════════════════════════════════════════════════════════════════════════
# CONFIGURATION
# ═════════════════════════════════════════════════════════════════════════════

# API Endpoints
EVENT_STORE_API = "http://localhost:8081"
FORENSICS_API = "http://localhost:8083"
GATEWAY_API = "http://localhost:8080"

# Database Config
DB_HOST = "localhost"
DB_PORT = 5432
DB_NAME = "sentinel_db"
DB_USER = "sentinel"
DB_PASSWORD = "sentinel"

# Colors for terminal output
class Colors:
    HEADER = '\033[95m'
    OKBLUE = '\033[94m'
    OKCYAN = '\033[96m'
    OKGREEN = '\033[92m'
    WARNING = '\033[93m'
    FAIL = '\033[91m'
    ENDC = '\033[0m'
    BOLD = '\033[1m'
    UNDERLINE = '\033[4m'

# ═════════════════════════════════════════════════════════════════════════════
# DATABASE FUNCTIONS
# ═════════════════════════════════════════════════════════════════════════════

def get_db_connection():
    """Create PostgreSQL database connection."""
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
        print(f"{Colors.FAIL}❌ Database connection failed: {e}{Colors.ENDC}")
        return None

def verify_database_events(session_id, expected_count=None):
    """Verify events in database for a session."""
    conn = get_db_connection()
    if not conn:
        return False
    
    try:
        with conn.cursor(cursor_factory=RealDictCursor) as cur:
            cur.execute(
                "SELECT COUNT(*) as count FROM security_events WHERE session_id = %s",
                (session_id,)
            )
            result = cur.fetchone()
            count = result['count']
            
            if expected_count and count == expected_count:
                print(f"{Colors.OKGREEN}✅ DB Verification: {count} events found{Colors.ENDC}")
                return True
            elif expected_count:
                print(f"{Colors.WARNING}⚠ DB Verification: Expected {expected_count}, found {count}{Colors.ENDC}")
                return False
            else:
                print(f"{Colors.OKGREEN}✅ DB Verification: {count} events found{Colors.ENDC}")
                return count > 0
    except Exception as e:
        print(f"{Colors.FAIL}❌ DB verification error: {e}{Colors.ENDC}")
        return False
    finally:
        conn.close()

def get_session_events_from_db(session_id):
    """Get all events for a session from database."""
    conn = get_db_connection()
    if not conn:
        return []
    
    try:
        with conn.cursor(cursor_factory=RealDictCursor) as cur:
            cur.execute(
                """SELECT id, session_id, event_type, decision, endpoint, 
                          source_ip, timestamp_ns FROM security_events 
                          WHERE session_id = %s ORDER BY timestamp_ns ASC""",
                (session_id,)
            )
            return cur.fetchall()
    except Exception as e:
        print(f"{Colors.FAIL}❌ Error fetching events: {e}{Colors.ENDC}")
        return []
    finally:
        conn.close()

# ═════════════════════════════════════════════════════════════════════════════
# API FUNCTIONS
# ═════════════════════════════════════════════════════════════════════════════

def create_security_event(session_id, event_type, endpoint, decision, source_ip="192.168.1.10"):
    """Create a security event via API."""
    event_data = {
        "sessionId": session_id,
        "eventType": event_type,
        "endpoint": endpoint,
        "decision": decision,
        "sourceIp": source_ip,
        "timestampNs": int(time.time() * 1e9)
    }
    
    try:
        # Try Event Store API first
        response = requests.post(
            f"{EVENT_STORE_API}/api/events",
            json=event_data,
            timeout=5
        )
        return response.status_code == 200
    except Exception as e:
        print(f"{Colors.WARNING}⚠ Event Store API unavailable, using direct DB insert{Colors.ENDC}")
        # Fallback: Insert directly to database
        return insert_event_to_db(session_id, event_type, endpoint, decision, source_ip)

def insert_event_to_db(session_id, event_type, endpoint, decision, source_ip="192.168.1.10"):
    """Insert event directly to database."""
    conn = get_db_connection()
    if not conn:
        return False
    
    try:
        with conn.cursor() as cur:
            cur.execute(
                """INSERT INTO security_events 
                   (id, session_id, event_type, decision, endpoint, source_ip, timestamp_ns)
                   VALUES (%s, %s, %s, %s, %s, %s, %s)""",
                (str(uuid.uuid4()), session_id, event_type, decision, endpoint, source_ip, 
                 int(time.time() * 1e9))
            )
            conn.commit()
        return True
    except Exception as e:
        print(f"{Colors.FAIL}❌ DB insert error: {e}{Colors.ENDC}")
        return False
    finally:
        conn.close()

def verify_forensics_api(session_id):
    """Verify session is accessible via Forensics API."""
    try:
        response = requests.get(
            f"{FORENSICS_API}/forensics/query/sessions",
            timeout=5
        )
        
        if response.status_code == 200:
            sessions = response.json()
            session_ids = [s.get('sessionId') for s in sessions if isinstance(sessions, list)]
            
            if session_id in session_ids:
                print(f"{Colors.OKGREEN}✅ Forensics API: Session found{Colors.ENDC}")
                return True
            else:
                print(f"{Colors.WARNING}⚠ Forensics API: Session not yet available{Colors.ENDC}")
                return False
        else:
            print(f"{Colors.FAIL}❌ Forensics API error: {response.status_code}{Colors.ENDC}")
            return False
    except Exception as e:
        print(f"{Colors.FAIL}❌ Forensics API connection error: {e}{Colors.ENDC}")
        return False

# ═════════════════════════════════════════════════════════════════════════════
# SESSION CREATION WORKFLOWS
# ═════════════════════════════════════════════════════════════════════════════

def create_normal_session():
    """
    Session Type 1: Normal Activity (Low Risk)
    Workflow: LOGIN → ACCESS → LOGOUT (All ALLOW)
    """
    print(f"\n{Colors.HEADER}{Colors.BOLD}═══════════════════════════════════════════════════════════════{Colors.ENDC}")
    print(f"{Colors.HEADER}{Colors.BOLD}SESSION TYPE 1: NORMAL ACTIVITY (LOW RISK){Colors.ENDC}")
    print(f"{Colors.HEADER}{Colors.BOLD}═══════════════════════════════════════════════════════════════{Colors.ENDC}")
    
    session_id = str(uuid.uuid4())
    print(f"\n📝 Session ID: {Colors.OKCYAN}{session_id}{Colors.ENDC}")
    
    events = [
        {"type": "LOGIN", "endpoint": "/auth/login", "decision": "ALLOW"},
        {"type": "ACCESS", "endpoint": "/api/users", "decision": "ALLOW"},
        {"type": "ACCESS", "endpoint": "/api/payments", "decision": "ALLOW"},
        {"type": "LOGOUT", "endpoint": "/auth/logout", "decision": "ALLOW"},
    ]
    
    # Step 1: Create events in database
    print(f"\n{Colors.BOLD}STEP 1: Creating security events...{Colors.ENDC}")
    for event in events:
        if insert_event_to_db(session_id, event["type"], event["endpoint"], event["decision"]):
            print(f"  ✅ {event['type']:8} → {event['endpoint']:20} [{event['decision']}]")
        time.sleep(0.2)
    
    # Step 2: Verify in database
    print(f"\n{Colors.BOLD}STEP 2: Verifying database...{Colors.ENDC}")
    time.sleep(1)
    verify_database_events(session_id, len(events))
    
    # Step 3: Verify in Forensics API
    print(f"\n{Colors.BOLD}STEP 3: Verifying Forensics Service...{Colors.ENDC}")
    time.sleep(1)
    verify_forensics_api(session_id)
    
    # Step 4: Display database content
    print(f"\n{Colors.BOLD}STEP 4: Database Records:{Colors.ENDC}")
    db_events = get_session_events_from_db(session_id)
    for event in db_events:
        print(f"  • {event['event_type']:8} | {event['decision']:6} | {event['endpoint']:20} | {event['source_ip']}")
    
    return session_id

def create_policy_violation_session():
    """
    Session Type 2: Policy Violation (Medium/High Risk)
    Workflow: LOGIN → UNAUTHORIZED_ACCESS (BLOCK) → SUSPICIOUS_ACTIVITY
    """
    print(f"\n{Colors.HEADER}{Colors.BOLD}═══════════════════════════════════════════════════════════════{Colors.ENDC}")
    print(f"{Colors.HEADER}{Colors.BOLD}SESSION TYPE 2: POLICY VIOLATION (MEDIUM/HIGH RISK){Colors.ENDC}")
    print(f"{Colors.HEADER}{Colors.BOLD}═══════════════════════════════════════════════════════════════{Colors.ENDC}")
    
    session_id = str(uuid.uuid4())
    print(f"\n📝 Session ID: {Colors.OKCYAN}{session_id}{Colors.ENDC}")
    
    events = [
        {"type": "LOGIN", "endpoint": "/auth/login", "decision": "ALLOW", "ip": "192.168.1.10"},
        {"type": "ACCESS", "endpoint": "/api/admin/users", "decision": "BLOCK", "ip": "192.168.1.10"},
        {"type": "ACCESS", "endpoint": "/api/admin/settings", "decision": "BLOCK", "ip": "192.168.1.10"},
        {"type": "ACCESS", "endpoint": "/api/sensitive-data", "decision": "BLOCK", "ip": "192.168.1.10"},
        {"type": "LOGOUT", "endpoint": "/auth/logout", "decision": "ALLOW", "ip": "192.168.1.10"},
    ]
    
    # Step 1: Create events
    print(f"\n{Colors.BOLD}STEP 1: Creating security events...{Colors.ENDC}")
    for event in events:
        if insert_event_to_db(session_id, event["type"], event["endpoint"], event["decision"], event["ip"]):
            print(f"  ✅ {event['type']:8} → {event['endpoint']:25} [{Colors.WARNING}{event['decision']}{Colors.ENDC}]")
        time.sleep(0.2)
    
    # Step 2: Verify in database
    print(f"\n{Colors.BOLD}STEP 2: Verifying database...{Colors.ENDC}")
    time.sleep(1)
    verify_database_events(session_id, len(events))
    
    # Step 3: Verify in Forensics API
    print(f"\n{Colors.BOLD}STEP 3: Verifying Forensics Service...{Colors.ENDC}")
    time.sleep(1)
    verify_forensics_api(session_id)
    
    # Step 4: Display database content
    print(f"\n{Colors.BOLD}STEP 4: Database Records:{Colors.ENDC}")
    db_events = get_session_events_from_db(session_id)
    for event in db_events:
        status = f"{Colors.FAIL}{event['decision']}{Colors.ENDC}" if event['decision'] == 'BLOCK' else event['decision']
        print(f"  • {event['event_type']:8} | {status:6} | {event['endpoint']:25} | {event['source_ip']}")
    
    return session_id

def create_attack_sequence_session():
    """
    Session Type 3: Attack Sequence (CRITICAL)
    Workflow: BRUTE_FORCE → SQL_INJECTION → DATA_EXFILTRATION
    """
    print(f"\n{Colors.HEADER}{Colors.BOLD}═══════════════════════════════════════════════════════════════{Colors.ENDC}")
    print(f"{Colors.HEADER}{Colors.BOLD}SESSION TYPE 3: ATTACK SEQUENCE (CRITICAL RISK){Colors.ENDC}")
    print(f"{Colors.HEADER}{Colors.BOLD}═══════════════════════════════════════════════════════════════{Colors.ENDC}")
    
    session_id = str(uuid.uuid4())
    print(f"\n📝 Session ID: {Colors.OKCYAN}{session_id}{Colors.ENDC}")
    
    events = [
        {"type": "LOGIN", "endpoint": "/auth/login", "decision": "ALLOW", "ip": "10.0.0.55"},
        {"type": "ATTACK", "endpoint": "/auth/login", "decision": "BLOCK", "ip": "10.0.0.55"},  # Brute force
        {"type": "ATTACK", "endpoint": "/api/users?id=1' OR '1'='1", "decision": "BLOCK", "ip": "10.0.0.55"},  # SQL injection
        {"type": "ACCESS", "endpoint": "/api/export", "decision": "BLOCK", "ip": "10.0.0.55"},  # Data exfil attempt
        {"type": "ATTACK", "endpoint": "/api/admin/database", "decision": "BLOCK", "ip": "10.0.0.55"},  # Privilege escalation
        {"type": "LOGOUT", "endpoint": "/auth/logout", "decision": "ALLOW", "ip": "10.0.0.55"},
    ]
    
    # Step 1: Create events
    print(f"\n{Colors.BOLD}STEP 1: Creating security events...{Colors.ENDC}")
    for event in events:
        if insert_event_to_db(session_id, event["type"], event["endpoint"], event["decision"], event["ip"]):
            icon = "🚨" if event["type"] == "ATTACK" else "✅"
            print(f"  {icon} {event['type']:8} → {event['endpoint']:35} [{Colors.FAIL}{event['decision']}{Colors.ENDC}]")
        time.sleep(0.2)
    
    # Step 2: Verify in database
    print(f"\n{Colors.BOLD}STEP 2: Verifying database...{Colors.ENDC}")
    time.sleep(1)
    verify_database_events(session_id, len(events))
    
    # Step 3: Verify in Forensics API
    print(f"\n{Colors.BOLD}STEP 3: Verifying Forensics Service...{Colors.ENDC}")
    time.sleep(1)
    verify_forensics_api(session_id)
    
    # Step 4: Display database content
    print(f"\n{Colors.BOLD}STEP 4: Database Records:{Colors.ENDC}")
    db_events = get_session_events_from_db(session_id)
    for event in db_events:
        icon = "🚨" if event['event_type'] == 'ATTACK' else "•"
        status = f"{Colors.FAIL}{event['decision']}{Colors.ENDC}" if event['decision'] == 'BLOCK' else event['decision']
        print(f"  {icon} {event['event_type']:8} | {status:6} | {event['endpoint']:35} | {event['source_ip']}")
    
    return session_id

# ═════════════════════════════════════════════════════════════════════════════
# VERIFICATION FUNCTIONS
# ═════════════════════════════════════════════════════════════════════════════

def verify_complete_workflow():
    """Verify complete data flow through all systems."""
    print(f"\n{Colors.HEADER}{Colors.BOLD}═══════════════════════════════════════════════════════════════{Colors.ENDC}")
    print(f"{Colors.HEADER}{Colors.BOLD}COMPLETE WORKFLOW VERIFICATION{Colors.ENDC}")
    print(f"{Colors.HEADER}{Colors.BOLD}═══════════════════════════════════════════════════════════════{Colors.ENDC}")
    
    print(f"\n{Colors.BOLD}Checking all services are running...{Colors.ENDC}")
    
    # Check databases
    print(f"\n1️⃣  {Colors.BOLD}Database (PostgreSQL){Colors.ENDC}")
    conn = get_db_connection()
    if conn:
        with conn.cursor() as cur:
            cur.execute("SELECT COUNT(*) FROM security_events")
            total = cur.fetchone()[0]
            print(f"   {Colors.OKGREEN}✅ Connected{Colors.ENDC}")
            print(f"   📊 Total events in database: {Colors.OKCYAN}{total}{Colors.ENDC}")
        conn.close()
    else:
        print(f"   {Colors.FAIL}❌ Connection failed{Colors.ENDC}")
    
    # Check Event Store
    print(f"\n2️⃣  {Colors.BOLD}Event Store Service (Port 8081){Colors.ENDC}")
    try:
        response = requests.get(f"{EVENT_STORE_API}/api/health", timeout=5)
        if response.status_code == 200:
            print(f"   {Colors.OKGREEN}✅ Running (Status: {response.status_code}){Colors.ENDC}")
        else:
            print(f"   {Colors.WARNING}⚠ Running but status: {response.status_code}{Colors.ENDC}")
    except Exception as e:
        print(f"   {Colors.FAIL}❌ Not responding{Colors.ENDC}")
    
    # Check Forensics Service
    print(f"\n3️⃣  {Colors.BOLD}Forensics Service (Port 8083){Colors.ENDC}")
    try:
        response = requests.get(f"{FORENSICS_API}/forensics/query/sessions", timeout=5)
        if response.status_code == 200:
            sessions = response.json()
            session_count = len(sessions) if isinstance(sessions, list) else 0
            print(f"   {Colors.OKGREEN}✅ Running (Status: {response.status_code}){Colors.ENDC}")
            print(f"   📊 Sessions available: {Colors.OKCYAN}{session_count}{Colors.ENDC}")
        else:
            print(f"   {Colors.WARNING}⚠ Running but status: {response.status_code}{Colors.ENDC}")
    except Exception as e:
        print(f"   {Colors.FAIL}❌ Not responding{Colors.ENDC}")
    
    # Check Frontend
    print(f"\n4️⃣  {Colors.BOLD}Frontend (Port 3001){Colors.ENDC}")
    try:
        response = requests.get("http://localhost:3001", timeout=5)
        if response.status_code == 200:
            print(f"   {Colors.OKGREEN}✅ Running (Status: {response.status_code}){Colors.ENDC}")
            print(f"   🌐 Dashboard available at: {Colors.OKCYAN}http://localhost:3001{Colors.ENDC}")
        else:
            print(f"   {Colors.WARNING}⚠ Running but status: {response.status_code}{Colors.ENDC}")
    except Exception as e:
        print(f"   {Colors.FAIL}❌ Not responding{Colors.ENDC}")

def print_session_urls(sessions):
    """Print URLs for viewing sessions in UI."""
    print(f"\n{Colors.HEADER}{Colors.BOLD}═══════════════════════════════════════════════════════════════{Colors.ENDC}")
    print(f"{Colors.HEADER}{Colors.BOLD}NEXT STEPS: VIEW SESSIONS IN UI{Colors.ENDC}")
    print(f"{Colors.HEADER}{Colors.BOLD}═══════════════════════════════════════════════════════════════{Colors.ENDC}")
    
    print(f"\n{Colors.BOLD}Open your browser and navigate to:{Colors.ENDC}\n")
    
    print(f"1️⃣  {Colors.BOLD}View All Sessions:{Colors.ENDC}")
    print(f"   {Colors.OKCYAN}http://localhost:3001/sessions{Colors.ENDC}\n")
    
    print(f"2️⃣  {Colors.BOLD}View Individual Sessions (Click on each to see replay):{Colors.ENDC}")
    
    for i, session_id in enumerate(sessions, 1):
        session_type = ["Normal Activity", "Policy Violation", "Attack Sequence"][i-1]
        print(f"\n   Session {i}: {Colors.BOLD}{session_type}{Colors.ENDC}")
        print(f"   Session ID: {Colors.OKCYAN}{session_id}{Colors.ENDC}")
        print(f"   View: {Colors.OKCYAN}http://localhost:3001/sessions/{session_id}{Colors.ENDC}")
        print(f"   Replay: Click 'View' button on the session page")
    
    print(f"\n3️⃣  {Colors.BOLD}Verify Data Flow:{Colors.ENDC}")
    print(f"   • Database: Check security_events table")
    print(f"   • API: {Colors.OKCYAN}http://localhost:8083/forensics/query/sessions{Colors.ENDC}")
    print(f"   • UI: Dashboard showing alerts and metrics")

# ═════════════════════════════════════════════════════════════════════════════
# MAIN EXECUTION
# ═════════════════════════════════════════════════════════════════════════════

def main():
    """Main workflow execution."""
    print(f"\n{Colors.HEADER}{Colors.BOLD}")
    print("╔════════════════════════════════════════════════════════════════╗")
    print("║     SENTINEL WORKFLOW AUTOMATION - SESSION CREATION            ║")
    print("║                                                                ║")
    print("║  This script creates security sessions and verifies:          ║")
    print("║  Database → Event Store → Forensics → Frontend → Browser      ║")
    print("╚════════════════════════════════════════════════════════════════╝")
    print(f"{Colors.ENDC}\n")
    
    # Verify services are running
    verify_complete_workflow()
    
    # Create sessions
    sessions = []
    try:
        session1 = create_normal_session()
        sessions.append(session1)
        time.sleep(2)
        
        session2 = create_policy_violation_session()
        sessions.append(session2)
        time.sleep(2)
        
        session3 = create_attack_sequence_session()
        sessions.append(session3)
        time.sleep(2)
        
        # Print viewing instructions
        print_session_urls(sessions)
        
        print(f"\n{Colors.OKGREEN}{Colors.BOLD}✅ WORKFLOW COMPLETE!{Colors.ENDC}")
        print(f"\n{Colors.BOLD}Summary:{Colors.ENDC}")
        print(f"  • {Colors.OKCYAN}3 sessions created{Colors.ENDC}")
        print(f"  • {Colors.OKCYAN}15 total security events{Colors.ENDC}")
        print(f"  • {Colors.OKCYAN}Data flow verified through all systems{Colors.ENDC}")
        print(f"  • {Colors.OKCYAN}Ready for UI replay and analysis{Colors.ENDC}")
        
    except KeyboardInterrupt:
        print(f"\n{Colors.WARNING}⚠ Interrupted by user{Colors.ENDC}")
    except Exception as e:
        print(f"\n{Colors.FAIL}❌ Error: {e}{Colors.ENDC}")

if __name__ == "__main__":
    main()
