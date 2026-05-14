#!/usr/bin/env python3
"""
Sentinel Session Viewer & Replay Helper
========================================
Enhanced script for viewing sessions and their replay status

Usage:
    python view_sessions.py
    python view_sessions.py --session-id <uuid>
    python view_sessions.py --export-csv
"""

import requests
import json
import sys
import csv
from datetime import datetime
import psycopg2
from psycopg2.extras import RealDictCursor
from tabulate import tabulate

# ═════════════════════════════════════════════════════════════════════════════
# CONFIGURATION
# ═════════════════════════════════════════════════════════════════════════════

# API Endpoints
FORENSICS_API = "http://localhost:8083"

# Database Config
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

# ═════════════════════════════════════════════════════════════════════════════
# DATABASE FUNCTIONS
# ═════════════════════════════════════════════════════════════════════════════

def get_db_connection():
    """Create PostgreSQL database connection."""
    try:
        conn = psycopg2.connect(
            host=DB_HOST, port=DB_PORT, database=DB_NAME,
            user=DB_USER, password=DB_PASSWORD
        )
        return conn
    except Exception as e:
        print(f"{Colors.FAIL}❌ Database connection failed: {e}{Colors.ENDC}")
        return None

def get_all_sessions():
    """Get all sessions from database."""
    conn = get_db_connection()
    if not conn:
        return []
    
    try:
        with conn.cursor(cursor_factory=RealDictCursor) as cur:
            cur.execute("""
                SELECT DISTINCT session_id,
                       COUNT(*) as event_count,
                       MIN(timestamp_ns) as first_event,
                       MAX(timestamp_ns) as last_event,
                       COUNT(CASE WHEN decision='BLOCK' THEN 1 END) as blocked_events,
                       COUNT(CASE WHEN event_type='ATTACK' THEN 1 END) as attack_events
                FROM security_events
                GROUP BY session_id
                ORDER BY first_event DESC
            """)
            return cur.fetchall()
    finally:
        conn.close()

def get_session_events(session_id):
    """Get events for specific session."""
    conn = get_db_connection()
    if not conn:
        return []
    
    try:
        with conn.cursor(cursor_factory=RealDictCursor) as cur:
            cur.execute("""
                SELECT event_type, decision, endpoint, source_ip, 
                       timestamp_ns, rule_matched, risk_score
                FROM security_events
                WHERE session_id = %s
                ORDER BY timestamp_ns ASC
            """, (session_id,))
            return cur.fetchall()
    finally:
        conn.close()

# ═════════════════════════════════════════════════════════════════════════════
# DISPLAY FUNCTIONS
# ═════════════════════════════════════════════════════════════════════════════

def get_risk_level(session_id):
    """Calculate risk level based on events."""
    events = get_session_events(session_id)
    if not events:
        return "LOW", 0
    
    total = len(events)
    blocked = sum(1 for e in events if e['decision'] == 'BLOCK')
    attacks = sum(1 for e in events if e['event_type'] == 'ATTACK')
    
    risk_score = (blocked / total * 100) + (attacks * 20)
    
    if risk_score >= 70:
        return "CRITICAL", risk_score
    elif risk_score >= 40:
        return "HIGH", risk_score
    elif risk_score >= 20:
        return "MEDIUM", risk_score
    else:
        return "LOW", risk_score

def print_session_summary():
    """Print summary of all sessions."""
    print(f"\n{Colors.HEADER}{Colors.BOLD}═══════════════════════════════════════════════════════════════{Colors.ENDC}")
    print(f"{Colors.HEADER}{Colors.BOLD}SENTINEL SESSION SUMMARY{Colors.ENDC}")
    print(f"{Colors.HEADER}{Colors.BOLD}═══════════════════════════════════════════════════════════════{Colors.ENDC}\n")
    
    sessions = get_all_sessions()
    if not sessions:
        print(f"{Colors.WARNING}No sessions found{Colors.ENDC}")
        return
    
    table_data = []
    for sess in sessions:
        risk_level, risk_score = get_risk_level(sess['session_id'])
        
        # Color code risk level
        if risk_level == "CRITICAL":
            risk_color = Colors.FAIL + risk_level + Colors.ENDC
        elif risk_level == "HIGH":
            risk_color = Colors.WARNING + risk_level + Colors.ENDC
        else:
            risk_color = Colors.OKGREEN + risk_level + Colors.ENDC
        
        table_data.append([
            sess['session_id'][:8] + "...",
            sess['event_count'],
            sess['blocked_events'],
            sess['attack_events'],
            risk_color,
            f"{risk_score:.1f}%"
        ])
    
    headers = ["Session ID", "Events", "Blocked", "Attacks", "Risk Level", "Score"]
    print(tabulate(table_data, headers=headers, tablefmt="grid"))
    
    total_events = sum(s['event_count'] for s in sessions)
    print(f"\n📊 {Colors.BOLD}Statistics:{Colors.ENDC}")
    print(f"   • Total Sessions: {len(sessions)}")
    print(f"   • Total Events: {total_events}")
    print(f"   • Total Blocked: {sum(s['blocked_events'] for s in sessions)}")
    print(f"   • Total Attacks: {sum(s['attack_events'] for s in sessions)}")

def print_session_details(session_id):
    """Print detailed view of a specific session."""
    print(f"\n{Colors.HEADER}{Colors.BOLD}═══════════════════════════════════════════════════════════════{Colors.ENDC}")
    print(f"{Colors.HEADER}{Colors.BOLD}SESSION DETAILS: {session_id}{Colors.ENDC}")
    print(f"{Colors.HEADER}{Colors.BOLD}═══════════════════════════════════════════════════════════════{Colors.ENDC}\n")
    
    events = get_session_events(session_id)
    if not events:
        print(f"{Colors.FAIL}❌ Session not found{Colors.ENDC}")
        return
    
    risk_level, risk_score = get_risk_level(session_id)
    
    print(f"{Colors.BOLD}Risk Level: {risk_level} ({risk_score:.1f}%){Colors.ENDC}")
    print(f"{Colors.BOLD}Total Events: {len(events)}{Colors.ENDC}\n")
    
    table_data = []
    for i, event in enumerate(events, 1):
        event_icon = "✅" if event['decision'] == "ALLOW" else "❌"
        attack_icon = "🚨" if event['event_type'] == "ATTACK" else ""
        
        table_data.append([
            i,
            f"{attack_icon} {event['event_type']}",
            event['decision'],
            event['endpoint'][:30],
            event['source_ip'],
            event['rule_matched'] or "-"
        ])
    
    headers = ["#", "Type", "Decision", "Endpoint", "Source IP", "Rule"]
    print(tabulate(table_data, headers=headers, tablefmt="grid"))
    
    # UI Link
    print(f"\n{Colors.BOLD}View in UI:{Colors.ENDC}")
    print(f"   {Colors.OKCYAN}http://localhost:3001/sessions/{session_id}{Colors.ENDC}")

def print_ui_navigation_guide():
    """Print guide for navigating the UI."""
    print(f"\n{Colors.HEADER}{Colors.BOLD}═══════════════════════════════════════════════════════════════{Colors.ENDC}")
    print(f"{Colors.HEADER}{Colors.BOLD}UI NAVIGATION GUIDE{Colors.ENDC}")
    print(f"{Colors.HEADER}{Colors.BOLD}═══════════════════════════════════════════════════════════════{Colors.ENDC}\n")
    
    print(f"{Colors.BOLD}1. VIEW ALL SESSIONS:{Colors.ENDC}")
    print(f"   {Colors.OKCYAN}http://localhost:3001/sessions{Colors.ENDC}")
    print(f"   • See all sessions with risk levels")
    print(f"   • Filter by status (Active, Suspicious, Compromised)")
    print(f"   • Click 'View' to see session details\n")
    
    print(f"{Colors.BOLD}2. FORENSIC REPLAY:{Colors.ENDC}")
    print(f"   • Click on any session to see event timeline")
    print(f"   • View step-by-step security events")
    print(f"   • See policy evaluation results")
    print(f"   • Check request context and risk scores\n")
    
    print(f"{Colors.BOLD}3. DASHBOARD:{Colors.ENDC}")
    print(f"   {Colors.OKCYAN}http://localhost:3001{Colors.ENDC}")
    print(f"   • Real-time threat alerts")
    print(f"   • Risk metrics and statistics")
    print(f"   • Recent sessions overview\n")
    
    print(f"{Colors.BOLD}4. ALERTS:{Colors.ENDC}")
    print(f"   {Colors.OKCYAN}http://localhost:3001/alerts{Colors.ENDC}")
    print(f"   • Security alerts by severity")
    print(f"   • Attack detection results")
    print(f"   • Policy violations\n")

def export_to_csv(filename="sessions_export.csv"):
    """Export session data to CSV."""
    sessions = get_all_sessions()
    if not sessions:
        print(f"{Colors.FAIL}❌ No sessions to export{Colors.ENDC}")
        return
    
    try:
        with open(filename, 'w', newline='') as f:
            writer = csv.writer(f)
            writer.writerow(['Session ID', 'Event Count', 'Blocked Events', 'Attack Events', 'Risk Level'])
            
            for sess in sessions:
                risk_level, _ = get_risk_level(sess['session_id'])
                writer.writerow([
                    sess['session_id'],
                    sess['event_count'],
                    sess['blocked_events'],
                    sess['attack_events'],
                    risk_level
                ])
        
        print(f"{Colors.OKGREEN}✅ Exported to {filename}{Colors.ENDC}")
    except Exception as e:
        print(f"{Colors.FAIL}❌ Export failed: {e}{Colors.ENDC}")

# ═════════════════════════════════════════════════════════════════════════════
# MAIN
# ═════════════════════════════════════════════════════════════════════════════

def main():
    """Main entry point."""
    if len(sys.argv) > 1:
        if sys.argv[1] == "--session-id" and len(sys.argv) > 2:
            print_session_details(sys.argv[2])
        elif sys.argv[1] == "--export-csv":
            export_to_csv()
        elif sys.argv[1] == "--help":
            print("Usage: python view_sessions.py [options]")
            print("  --session-id <uuid>   View specific session")
            print("  --export-csv          Export to CSV")
            print("  --help                Show this help")
        else:
            print(f"{Colors.FAIL}Unknown option: {sys.argv[1]}{Colors.ENDC}")
    else:
        print_session_summary()
        print_ui_navigation_guide()

if __name__ == "__main__":
    main()
