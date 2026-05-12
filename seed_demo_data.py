import urllib.request
import urllib.error
import json
import base64
import time
import jwt
import os

# Configuration
GATEWAY_URL = "http://localhost:8080"
POLICY_URL = "http://localhost:8082/admin/policies"
PRIVATE_KEY_PATH = "gateway-service/src/main/resources/keys/private_key.pem"

# Auth for Policy Engine
POLICY_AUTH = b"Basic " + base64.b64encode(b"admin:admin")

# Known UUIDs for Forensics Dashboard
SESSION_ALICE = "aaaaaaaa-0000-0000-0000-000000000001"
SESSION_BOB = "bbbbbbbb-0000-0000-0000-000000000002"
SESSION_MALORY = "cccccccc-0000-0000-0000-000000000003"

def do_request(url, method="GET", payload=None, headers=None):
    req = urllib.request.Request(url, method=method)
    if headers:
        for k, v in headers.items():
            req.add_header(k, v)
    if payload:
        req.add_header("Content-Type", "application/json")
        data = json.dumps(payload).encode('utf-8')
        req.data = data
    try:
        with urllib.request.urlopen(req) as response:
            return json.loads(response.read().decode())
    except urllib.error.HTTPError as e:
        # 404 is expected for delete, 500 might mean already exists
        if e.code not in [404, 500]:
            print(f"HTTPError: {e.code} for {method} {url}")
        return None
    except Exception as e:
        print(f"Error: {e}")
        return None

def generate_token(user_id, roles):
    with open(PRIVATE_KEY_PATH, 'r') as f:
        private_key = f.read()
    
    payload = {
        "sub": user_id,
        "roles": roles,
        "scope": "read write",
        "iss": "sentinel",
        "aud": "sentinel-api",
        "iat": int(time.time()),
        "exp": int(time.time()) + 3600
    }
    return jwt.encode(payload, private_key, algorithm='RS256')

def seed_policies():
    print("Seeding Policies...")
    
    # 1. Admin Access Rule
    admin_rule = {
        "ruleId": "admin-access-rule",
        "name": "Admin Access Rule",
        "version": 1,
        "description": "Allows admin to access the /admin endpoints",
        "effect": "ALLOW",
        "priority": 100,
        "active": True,
        "conditions": [
            {"type": "ROLE", "operator": "equals", "value": "ADMIN"}
        ]
    }
    do_request(POLICY_URL, "POST", admin_rule, {"Authorization": POLICY_AUTH})

    # 2. Finance Rule
    finance_rule = {
        "ruleId": "finance-access-rule",
        "name": "Finance Access Rule",
        "version": 1,
        "description": "Allows finance-admin to access payments",
        "effect": "ALLOW",
        "priority": 100,
        "active": True,
        "conditions": [
            {"type": "ROLE", "operator": "equals", "value": "finance-admin"}
        ]
    }
    do_request(POLICY_URL, "POST", finance_rule, {"Authorization": POLICY_AUTH})
    print("Policies seeded (or already exist).")

def run_demo_sessions():
    print("Running Demo Sessions with hardcoded UUIDs...")
    
    # --- SESSION 1: GOOD USER (Alice) ---
    print(f"\n[Session 1] Alice (USER) - Normal Activity ({SESSION_ALICE})")
    alice_token = generate_token("alice-123", ["USER"])
    headers = {
        "Authorization": f"Bearer {alice_token}",
        "X-Session-ID": SESSION_ALICE
    }
    do_request(f"{GATEWAY_URL}/api/users/profile", "GET", headers=headers)
    time.sleep(0.5)
    do_request(f"{GATEWAY_URL}/api/users", "GET", headers=headers)

    # --- SESSION 2: ADMIN USER (Bob) ---
    print(f"\n[Session 2] Bob (ADMIN) - Authorized Activity ({SESSION_BOB})")
    bob_token = generate_token("bob-456", ["ADMIN", "USER"])
    headers = {
        "Authorization": f"Bearer {bob_token}",
        "X-Session-ID": SESSION_BOB
    }
    do_request(f"{GATEWAY_URL}/api/admin/dashboard", "GET", headers=headers)
    time.sleep(0.5)
    do_request(f"{GATEWAY_URL}/api/users/profile", "GET", headers=headers)

    # --- SESSION 3: ATTACKER (Malory) ---
    print(f"\n[Session 3] Malory (ATTACKER) - Escalation Attempt ({SESSION_MALORY})")
    malory_token = generate_token("malory-999", ["USER"])
    headers = {
        "Authorization": f"Bearer {malory_token}",
        "X-Session-ID": SESSION_MALORY
    }
    # 1. Normal Profile (Allow)
    do_request(f"{GATEWAY_URL}/api/users/profile", "GET", headers=headers)
    time.sleep(0.5)
    # 2. Attempt Payment Delete (Deny)
    do_request(f"{GATEWAY_URL}/api/payments/delete", "DELETE", headers=headers)
    time.sleep(0.5)
    # 3. Attempt Admin Config (Deny)
    do_request(f"{GATEWAY_URL}/api/admin/config", "GET", headers=headers)

    print("\nDemo sessions completed. Refresh the dashboard at http://localhost:5173/")

if __name__ == "__main__":
    try:
        seed_policies()
        run_demo_sessions()
    except Exception as e:
        print(f"Error during seeding: {e}")
