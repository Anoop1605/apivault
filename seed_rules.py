import psycopg2
import uuid
import json
from datetime import datetime

def seed_rules():
    conn = psycopg2.connect(dbname="apivault", user="postgres", password="postgres", host="localhost")
    cur = conn.cursor()

    # Clear existing rules
    cur.execute("TRUNCATE policy_rules CASCADE")

    rules = [
        {
            "id": str(uuid.uuid4()),
            "rule_id": "GLOBAL-ALLOW-PROFILE",
            "name": "Allow Profile Access",
            "description": "Allow all users to access their profile",
            "priority": 100,
            "active": True,
            "conditions": json.dumps([
                {"type": "ENDPOINT", "operator": "EQUALS", "value": "/api/users/profile"}
            ]),
            "decision": "ALLOW",
            "version": 1
        },
        {
            "id": str(uuid.uuid4()),
            "rule_id": "ADMIN-ONLY-DELETE",
            "name": "Admin Only Payment Delete",
            "description": "Only allow admins to delete payments",
            "priority": 200,
            "active": True,
            "conditions": json.dumps([
                {"type": "ENDPOINT", "operator": "EQUALS", "value": "/api/payments/delete"},
                {"type": "ROLE", "operator": "CONTAINS", "value": "admin"}
            ]),
            "decision": "ALLOW",
            "version": 1
        },
        {
            "id": str(uuid.uuid4()),
            "rule_id": "DENY-HIGH-RISK",
            "name": "Block High Risk",
            "description": "Block any request with risk score > 0.8",
            "priority": 500,
            "active": True,
            "conditions": json.dumps([
                {"type": "RISK", "operator": "GREATER_THAN", "value": 0.8}
            ]),
            "decision": "DENY",
            "version": 1
        },
        {
            "id": str(uuid.uuid4()),
            "rule_id": "ALLOW-PAYMENTS-POST",
            "name": "Allow Payment Creation",
            "description": "Allow all authenticated users to create payments",
            "priority": 50,
            "active": True,
            "conditions": json.dumps([
                {"type": "ENDPOINT", "operator": "EQUALS", "value": "/api/payments"},
                {"type": "METHOD", "operator": "EQUALS", "value": "POST"}
            ]),
            "decision": "ALLOW",
            "version": 1
        },
        {
            "id": str(uuid.uuid4()),
            "rule_id": "FORENSICS-ADMIN-ALLOW",
            "name": "Allow Forensics Admin",
            "description": "Allow forensics admins to access replay and query services",
            "priority": 1000,
            "active": True,
            "conditions": json.dumps([
                {"type": "ENDPOINT", "operator": "CONTAINS", "value": "/forensics"}
            ]),
            "decision": "ALLOW",
            "version": 1
        }
    ]

    for r in rules:
        cur.execute("""
            INSERT INTO policy_rules (id, rule_id, name, description, priority, active, conditions, decision, version, created_at, updated_at)
            VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, NOW(), NOW())
        """, (r['id'], r['rule_id'], r['name'], r['description'], r['priority'], r['active'], r['conditions'], r['decision'], r['version']))

    conn.commit()
    print(f"Seeded {len(rules)} policy rules.")
    cur.close()
    conn.close()

if __name__ == "__main__":
    seed_rules()
