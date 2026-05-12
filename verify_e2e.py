"""
Sentinel End-to-End Verification
Sends known traffic → checks DB → checks API → confirms UI data integrity
"""
import requests
import psycopg2
import uuid
import time
import hashlib
import json
from config import CONFIG

# ─── Send ONE known request via Gateway ───────────────────────────────────────
TEST_SESSION = str(uuid.uuid4())
print(f"\n{'='*60}")
print(f"STEP 1: Sending known request via Gateway")
print(f"  Session ID : {TEST_SESSION}")
print(f"  Endpoint   : GET /api/users/profile")

resp = requests.get(
    f"{CONFIG.gateway_base_url}/api/users/profile",
    headers={
        "Authorization": f"Bearer {CONFIG.user_token}",
        "X-Session-ID": TEST_SESSION,
        "X-Risk-Score": "0.42",
        "X-Request-ID": str(uuid.uuid4()),
        "Content-Type": "application/json",
        "User-Agent": "sentinel-e2e-verify/1.0",
    },
    timeout=5
)
print(f"  Gateway response : HTTP {resp.status_code}")

# Give event store time to persist
time.sleep(1.5)

# ─── Check Event Store API ─────────────────────────────────────────────────────
print(f"\n{'='*60}")
print(f"STEP 2: Querying Event Store API (port 8081)")
api_resp = requests.get(f"http://localhost:8081/events/sessions/{TEST_SESSION}", timeout=5)
api_events = api_resp.json()
print(f"  Events found via API : {len(api_events)}")
for ev in api_events:
    print(f"  ├── eventType    : {ev.get('eventType')}")
    print(f"  ├── decision     : {ev.get('decision')}")
    print(f"  ├── policyRuleId : {ev.get('policyRuleId')}")
    print(f"  ├── bodyHash     : {(ev.get('bodyHash') or '')[:32]}...")
    print(f"  ├── userId       : {ev.get('userId')}")
    print(f"  └── sourceIp     : {ev.get('sourceIp')}")
    print()

# ─── Check Database directly ───────────────────────────────────────────────────
print(f"{'='*60}")
print(f"STEP 3: Querying PostgreSQL directly")
conn = psycopg2.connect(dbname='apivault', user='postgres', password='postgres', host='localhost')
cur = conn.cursor()
cur.execute("""
    SELECT event_type, decision, endpoint, policy_rule_id, policy_rule_version,
           body_hash, user_id, source_ip, risk_score
    FROM events
    WHERE session_id = %s
    ORDER BY timestamp_ns ASC
""", (TEST_SESSION,))
db_rows = cur.fetchall()
print(f"  Events found in DB   : {len(db_rows)}")
for row in db_rows:
    event_type, decision, endpoint, rule_id, rule_ver, body_hash, user_id, source_ip, risk = row
    print(f"  ├── eventType    : {event_type}")
    print(f"  ├── decision     : {decision}")
    print(f"  ├── endpoint     : {endpoint}")
    print(f"  ├── policyRuleId : {rule_id}")
    print(f"  ├── ruleVersion  : {rule_ver}")
    print(f"  ├── bodyHash     : {(body_hash or '')[:32]}...")
    print(f"  ├── userId       : {user_id}")
    print(f"  └── sourceIp     : {source_ip}")
    print()

# ─── Cross-validate: API vs DB ────────────────────────────────────────────────
print(f"{'='*60}")
print(f"STEP 4: Cross-validating API ↔ DB")

if len(api_events) != len(db_rows):
    print(f"  ❌ MISMATCH: API returned {len(api_events)} events, DB has {len(db_rows)}")
else:
    print(f"  ✅ Event count matches: {len(api_events)}")
    all_match = True
    for i, (api_ev, db_row) in enumerate(zip(api_events, db_rows)):
        db_type, db_dec, db_ep, db_rule, db_ver, db_hash, db_user, db_ip, db_risk = db_row
        checks = [
            ("eventType", api_ev.get('eventType'), db_type),
            ("decision",  api_ev.get('decision'),  str(db_dec) if db_dec else None),
            ("endpoint",  api_ev.get('endpoint'),  db_ep),
            ("ruleId",    api_ev.get('policyRuleId'), db_rule),
            ("bodyHash",  api_ev.get('bodyHash'),  db_hash),
        ]
        for field, api_val, db_val in checks:
            if api_val != db_val:
                print(f"  ❌ Event {i+1} [{field}]: API={api_val!r} ≠ DB={db_val!r}")
                all_match = False
            else:
                print(f"  ✅ Event {i+1} [{field}]: {api_val!r}")
    if all_match:
        print(f"\n  ✅ ALL FIELDS MATCH — DB and API are identical")

# ─── What the UI renders ──────────────────────────────────────────────────────
print(f"\n{'='*60}")
print(f"STEP 5: What the UI (SessionDetail) renders for this session")
for ev in api_events:
    bh = ev.get('bodyHash') or ''
    print(f"  eventType   : {ev.get('eventType')}")
    print(f"  endpoint    : {ev.get('endpoint')}")
    print(f"  decision    : {ev.get('decision')}")
    print(f"  policyRule  : {ev.get('policyRuleId') or 'N/A'} v{ev.get('policyRuleVersion') or 1}")
    print(f"  requestBody : SHA-256: {bh[:20]}..." if bh else "  requestBody : No body / Empty body")
    print(f"  userName    : {ev.get('userId') or 'anonymous'}")
    print(f"  ipAddress   : {ev.get('sourceIp') or 'Intercepted via Gateway'}")
    print()

conn.close()
print(f"{'='*60}")
print("✅ End-to-end verification complete")
