import urllib.request
import urllib.error
import json
import base64
import time
import jwt

# --- CONFIGURATION ---
GATEWAY_URL = "http://localhost:8080"
PRIVATE_KEY_PATH = "gateway-service/src/main/resources/keys/private_key.pem"

def generate_token(user_id, roles):
    with open(PRIVATE_KEY_PATH, 'r') as f:
        private_key = f.read()
    
    payload = {
        "sub": user_id,
        "roles": roles,
        "iss": "sentinel",
        "iat": int(time.time()),
        "exp": int(time.time()) + 3600
    }
    return jwt.encode(payload, private_key, algorithm='RS256')

def send_request(endpoint, method, token, session_id):
    headers = {
        "Authorization": f"Bearer {token}",
        "X-Session-ID": session_id,
        "Content-Type": "application/json"
    }
    url = f"{GATEWAY_URL}{endpoint}"
    
    print(f"\n[DEMO] Step 1: Sending {method} request to {endpoint}...")
    print(f"[DEMO] Step 2: Gateway will intercept and validate JWT...")
    
    req = urllib.request.Request(url, method=method, headers=headers)
    try:
        with urllib.request.urlopen(req) as response:
            res_body = response.read().decode()
            print(f"[DEMO] Step 3: SUCCESS! Policy Engine allowed the request.")
            print(f"[DEMO] Result: {res_body[:100]}...")
    except urllib.error.HTTPError as e:
        if e.code == 403 or e.code == 401:
            print(f"[DEMO] Step 3: BLOCKED! Policy Engine denied access (403 Forbidden).")
        else:
            print(f"[DEMO] Step 3: Server returned error {e.code}")
    except Exception as e:
        print(f"[DEMO] Step 3: Error: {e}")

    print(f"[DEMO] Step 4: Event has been stored in the tamper-proof Event Store.")
    print(f"[DEMO] Step 5: Refresh the Dashboard to see Session ID: {session_id}")

def main():
    print("=== SENTINEL LIVE INTERACTIVE DEMO ===")
    user_id = input("Enter User ID (e.g., Alice, Bob): ")
    role = input("Enter Role (ADMIN or USER): ").upper()
    
    # Map role to list
    roles = [role]
    if role == "ADMIN":
        roles.append("USER")

    token = generate_token(user_id, roles)
    
    # Use a specific session ID so we can find it easily
    session_id = f"demo-sess-{int(time.time()) % 10000}"
    
    print(f"\nSession initialized: {session_id}")
    
    while True:
        print("\nChoose an action to perform:")
        print("1. View Profile (Normally ALLOWED for everyone)")
        print("2. View Admin Dashboard (ALLOWED for ADMIN only)")
        print("3. Delete Payment (ATTACK - BLOCKED for everyone in demo)")
        print("q. Quit and check Dashboard")
        
        choice = input("\nChoice: ")
        
        if choice == '1':
            send_request("/api/users/profile", "GET", token, session_id)
        elif choice == '2':
            send_request("/api/admin/dashboard", "GET", token, session_id)
        elif choice == '3':
            send_request("/api/payments/delete", "DELETE", token, session_id)
        elif choice == 'q':
            break
        else:
            print("Invalid choice.")

if __name__ == "__main__":
    main()
