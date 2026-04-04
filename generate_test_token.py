import jwt
import time

# Script to generate a valid RS256 JWT token for testing the Sentinel Gateway

# Load the private key generated earlier
with open('gateway-service/src/main/resources/keys/private_key.pem', 'r') as f:
    private_key = f.read()

# Define the payload per Sentinel PRD
payload = {
    "sub": "user-9921",
    "roles": ["finance-admin"],
    "scope": "read write",
    "iss": "sentinel",
    "aud": "sentinel-api",
    "iat": int(time.time()),
    "exp": int(time.time()) + 3600 # expires in 1 hour
}

# Encode token using RS256
token = jwt.encode(payload, private_key, algorithm='RS256')

print("\n=== SENTINEL GATEWAY TEST TOKEN ===")
print("Export this token as an environment variable to use in curl commands:\n")
print(f"export TOKEN=\"{token}\"")
print("\nExample Test Command (Valid Token):")
print("curl -H \"Authorization: Bearer $TOKEN\" http://localhost:8080/api/users/profile")
print("\nExample Test Command (Missing Token -> Should get 401):")
print("curl http://localhost:8080/api/users/profile")
print("===================================\n")
