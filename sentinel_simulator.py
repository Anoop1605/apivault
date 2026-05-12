import requests
import time
import uuid
import random

# =================================================================
# SENTINEL: TRAFFIC GENERATOR
# Simply sends requests to the Java Gateway (Port 8080)
# =================================================================

GATEWAY_URL = "http://localhost:8080"

def send_request(endpoint, method="GET", user_id="guest", role="USER", risk=0.1):
    url = f"{GATEWAY_URL}{endpoint}"
    
    # Common headers for Sentinel
    headers = {
        "X-Session-ID": str(uuid.uuid4()),
        "X-User-ID": user_id,
        "X-User-Role": role,
        "X-Risk-Score": str(risk),
        "Content-Type": "application/json"
    }

    print(f"[TRAFFIC] {method} -> {endpoint} (User: {user_id}, Role: {role})")
    
    try:
        if method == "GET":
            response = requests.get(url, headers=headers)
        else:
            response = requests.post(url, headers=headers, json={"data": "sample payload"})
            
        status = "ALLOWED" if response.status_code < 400 else "DENIED"
        print(f"          Result: {status} ({response.status_code})\n")
    except Exception as e:
        print(f"          Error: Could not connect to Gateway at {GATEWAY_URL}\n")

def run_simulation():
    print("Starting Sentinel Traffic Generator...")
    print("Connected to Java Gateway: http://localhost:8080\n")

    # 1. Normal User Activity
    send_request("/api/users/profile", user_id="alice_admin", role="ADMIN", risk=0.05)
    send_request("/api/payments", method="GET", user_id="bob_user", role="USER", risk=0.1)

    # 2. Suspicious Admin Access (No Admin Role)
    send_request("/api/admin/config", method="GET", user_id="bob_user", role="USER", risk=0.4)

    # 3. High Risk Attack (Credential Stuffing/Brute Force)
    for i in range(3):
        send_request("/api/admin/login", method="POST", user_id="attacker_bot", role="GUEST", risk=0.9)
        time.sleep(0.5)

    print("Traffic Generation Complete.")
    print("Check your React Dashboard to see the events in the Audit Log.")

if __name__ == "__main__":
    run_simulation()
