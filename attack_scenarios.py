import random
import time
import uuid
from typing import List

from traffic_utils import generate_session_id, random_user_agent, generate_payload
from config import CONFIG
import requests
import hashlib
import json

# Helper to build common headers for a given token and session id
def _base_headers(token: str, session_id: str, payload: dict = None) -> dict:
    headers = {
        "Authorization": f"Bearer {token}" if token else None,
        "X-Session-ID": session_id,
        "User-Agent": random_user_agent(),
        "X-Request-ID": str(uuid.uuid4()),
        "X-Risk-Score": str(round(random.uniform(0.0, 0.9), 2)),
        "Content-Type": "application/json",
    }
    # We NO LONGER send X-Policy-Rule-ID, X-Body-Hash, etc.
    # The Gateway must generate these!
    return headers

def normal_user_traffic():
    """Simulate regular user activity across typical endpoints."""
    session_id = str(uuid.uuid4())
    headers = _base_headers(CONFIG.user_token, session_id)
    # Profile view
    requests.get(f"{CONFIG.gateway_base_url}/api/users/profile", headers=headers)
    time.sleep(random.uniform(0.5, 2.0))
    # Payments list
    requests.get(f"{CONFIG.gateway_base_url}/api/payments", headers=headers)
    time.sleep(random.uniform(0.5, 2.0))
    # Make a payment (POST) with random payload
    payload = generate_payload()
    headers_post = _base_headers(CONFIG.user_token, session_id, payload)
    requests.post(f"{CONFIG.gateway_base_url}/api/payments", json=payload, headers=headers_post)

def invalid_jwt_requests():
    """Send requests with a broken or missing JWT to trigger AUTH_FAILED."""
    session_id = str(uuid.uuid4())
    # Missing token (but still send forensic headers for audit)
    headers_missing = _base_headers(None, session_id)
    requests.get(f"{CONFIG.gateway_base_url}/api/users/profile", headers=headers_missing)
    # Corrupted token
    headers_bad = _base_headers("this.is.not.a.valid.jwt", session_id)
    requests.get(f"{CONFIG.gateway_base_url}/api/payments", headers=headers_bad)

def unauthorized_admin_attempts():
    """User token tries to access admin APIs – expect POLICY_DENIED / AUTH_FAILED."""
    session_id = str(uuid.uuid4())
    headers = _base_headers(CONFIG.user_token, session_id)
    admin_endpoints = [e.strip() for e in CONFIG.admin_endpoints]
    for ep in admin_endpoints:
        requests.get(f"{CONFIG.gateway_base_url}{ep}", headers=headers)
        time.sleep(0.3)

def high_frequency_suspicious_requests():
    """Burst of requests using attacker token to simulate credential‑stuffing."""
    session_id = str(uuid.uuid4())
    headers = _base_headers(CONFIG.attacker_token, session_id)
    for i in range(20):
        # Randomly pick an endpoint from user & payment pools
        endpoint = random.choice([
            "/api/users/profile",
            "/api/payments",
            "/api/payments/checkout",
        ])
        if random.random() < 0.5:
            requests.get(f"{CONFIG.gateway_base_url}{endpoint}", headers=headers)
        else:
            payload = generate_payload()
            headers_post = _base_headers(CONFIG.attacker_token, session_id, payload)
            requests.post(f"{CONFIG.gateway_base_url}{endpoint}", json=payload, headers=headers_post)
        time.sleep(random.uniform(0.05, 0.2))

def multi_step_attack_chain():
    """A chained attack: probe, then exploit, then exfiltrate data."""
    # Step 1 – probing (unauth admin)
    unauthorized_admin_attempts()
    time.sleep(1)
    # Step 2 – privilege escalation using attacker token on admin login
    session_id = str(uuid.uuid4())
    login_payload = {"username": "admin", "password": "guessme"}
    headers = _base_headers(CONFIG.attacker_token, session_id, login_payload)
    requests.post(f"{CONFIG.gateway_base_url}/api/admin/login", json=login_payload, headers=headers)
    time.sleep(1)
    # Step 3 – data exfiltration (read all users)
    exfil_session = str(uuid.uuid4())
    exfil_headers = _base_headers(CONFIG.attacker_token, exfil_session)
    requests.get(f"{CONFIG.gateway_base_url}/api/users", headers=exfil_headers)

# Export a list of callable scenarios for the generator to iterate over
SCENARIOS: List[callable] = [
    normal_user_traffic,
    invalid_jwt_requests,
    unauthorized_admin_attempts,
    high_frequency_suspicious_requests,
    multi_step_attack_chain,
]
