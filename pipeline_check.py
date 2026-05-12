import requests
import psycopg2

# 1. Event Store - sessions
sessions = requests.get('http://localhost:8081/events/sessions').json()
print(f'[Event Store] Sessions: {len(sessions)}')

# 2. Pick first session and check events
sid = sessions[0]
events = requests.get(f'http://localhost:8081/events/sessions/{sid}').json()
print(f'[Event Store] Events for session {sid[:8]}...: {len(events)}')

e = events[0]
print(f'  eventType    : {e.get("eventType")}')
print(f'  decision     : {e.get("decision")}')
print(f'  policyRuleId : {e.get("policyRuleId")}')
body = e.get("bodyHash", "") or ""
print(f'  bodyHash     : {body[:20]}...')
print(f'  userId       : {e.get("userId")}')
print(f'  sourceIp     : {e.get("sourceIp")}')

denies = [ev for ev in events if ev.get('decision') == 'DENY']
print(f'  denyCount    : {len(denies)}')

# 3. DB null check
conn = psycopg2.connect(dbname='apivault', user='postgres', password='postgres', host='localhost')
cur = conn.cursor()
cur.execute("SELECT COUNT(*) FROM events WHERE policy_rule_id IS NULL")
null_rules = cur.fetchone()[0]
cur.execute("SELECT COUNT(*) FROM events WHERE body_hash IS NULL")
null_hashes = cur.fetchone()[0]
cur.execute("SELECT COUNT(*) FROM events")
total = cur.fetchone()[0]
print(f'\n[DB] Total events  : {total}')
print(f'[DB] Null rule_id  : {null_rules}')
print(f'[DB] Null body_hash: {null_hashes}')
conn.close()
