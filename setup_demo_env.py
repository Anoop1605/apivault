import jwt
import time
import os
from pathlib import Path

# Script to generate valid RS256 JWT tokens for all Sentinel roles
# This populates the .env file for the traffic generator.

PRIVATE_KEY_PATH = 'gateway-service/src/main/resources/keys/private_key.pem'

def generate_token(sub, roles, private_key):
    payload = {
        "sub": sub,
        "roles": roles,
        "scope": "read write",
        "iss": "sentinel",
        "aud": "sentinel-api",
        "iat": int(time.time()),
        "exp": int(time.time()) + 3600*24 # expires in 24 hours
    }
    return jwt.encode(payload, private_key, algorithm='RS256')

def main():
    if not os.path.exists(PRIVATE_KEY_PATH):
        print(f"Error: Private key not found at {PRIVATE_KEY_PATH}")
        return

    with open(PRIVATE_KEY_PATH, 'r') as f:
        private_key = f.read()

    user_token = generate_token("user-alice", ["user"], private_key)
    admin_token = generate_token("admin-bob", ["admin"], private_key)
    attacker_token = generate_token("attacker-eve", ["guest"], private_key)
    forensics_token = generate_token("forensics-charlie", ["forensics-admin"], private_key)

    env_content = f"""GATEWAY_BASE_URL=http://localhost:8080
USER_TOKEN={user_token}
ADMIN_TOKEN={admin_token}
ATTACKER_TOKEN={attacker_token}
FORENSICS_ADMIN_TOKEN={forensics_token}
"""
    
    with open(".env", "w") as f:
        f.write(env_content)
    
    print("Successfully generated .env file with fresh tokens for all roles.")

if __name__ == "__main__":
    main()
