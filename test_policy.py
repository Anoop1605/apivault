import urllib.request
import urllib.error
import json
import base64

BASE_URL = "http://localhost:8082/admin/policies"
AUTH = b"Basic " + base64.b64encode(b"admin:admin")

def do_request(url, method="GET", payload=None):
    req = urllib.request.Request(url, method=method)
    req.add_header("Authorization", AUTH)
    if payload:
        req.add_header("Content-Type", "application/json")
        data = json.dumps(payload).encode('utf-8')
        req.data = data
    try:
        with urllib.request.urlopen(req) as response:
            return json.loads(response.read().decode())
    except urllib.error.HTTPError as e:
        print(f"HTTPError: {e.code} for {method} {url}")
        print(e.read().decode())
        return None

print("1. Creating a new policy rule...")
policy_rule = {
  "ruleId": "admin-access-rule",
  "name": "Admin Access Rule",
  "version": 1,
  "description": "Allows admin to access the /admin endpoints",
  "effect": "ALLOW",
  "priority": 100,
  "active": True,
  "conditions": [
    {
      "type": "ROLE",
      "operator": "equals",
      "value": "ADMIN"
    }
  ]
}

res = do_request(BASE_URL, "POST", policy_rule)
print(f"Response: {json.dumps(res, indent=2)}\n")

print("2. Fetching all policies...")
policies = do_request(BASE_URL, "GET")
print(f"Response: {json.dumps(policies, indent=2)}\n")

print("3. Evaluating an ALLOW case (User has ADMIN role)...")
req_context_allow = {
  "userId": "user-123",
  "roles": ["ADMIN", "USER"],
  "sessionId": "session-1",
  "endpoint": "/admin/dashboard",
  "method": "GET",
  "sourceIp": "192.168.1.1",
  "requestTimestamp": "2026-04-08T10:00:00Z",
  "userAgent": "Mozilla/5.0",
  "riskScore": 0.1,
  "requestBodyHash": None
}

eval_res_allow = do_request(BASE_URL + "/evaluate", "POST", req_context_allow)
print(f"Response: {json.dumps(eval_res_allow, indent=2)}\n")

print("4. Evaluating a DENY case (User is missing ADMIN role)...")
req_context_deny = {
  "userId": "user-456",
  "roles": ["USER"],
  "sessionId": "session-2",
  "endpoint": "/admin/dashboard",
  "method": "GET",
  "sourceIp": "192.168.1.2",
  "requestTimestamp": "2026-04-08T10:05:00Z",
  "userAgent": "Mozilla/5.0",
  "riskScore": 0.5,
  "requestBodyHash": None
}

eval_res_deny = do_request(BASE_URL + "/evaluate", "POST", req_context_deny)
print(f"Response: {json.dumps(eval_res_deny, indent=2)}\n")

