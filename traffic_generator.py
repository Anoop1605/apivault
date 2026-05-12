import os
import json
import time
import uuid
import random
import logging
from typing import Optional, Dict

import requests
from dotenv import load_dotenv

# Local imports
from config import CONFIG
from traffic_utils import generate_session_id, random_user_agent, generate_payload

# Configure basic logging
logging.basicConfig(level=logging.INFO, format="[%(asctime)s] %(levelname)s: %(message)s")
logger = logging.getLogger(__name__)

class TrafficGenerator:
    """Encapsulates request logic for interacting with Sentinel gateway services.

    It reuses the existing configuration and utility helpers to construct realistic
    HTTP calls that trigger JWT authentication, ABAC policy checks, risk scoring, and
    event persistence in the backend.
    """

    def __init__(self, base_url: str = CONFIG.gateway_base_url):
        self.base_url = base_url.rstrip('/')
        # Tokens for different roles – kept here for convenience
        self.tokens = {
            "USER": CONFIG.user_token,
            "ADMIN": CONFIG.admin_token,
            "ATTACKER": CONFIG.attacker_token,
            "FORENSICS_ADMIN": CONFIG.forensics_admin_token,
        }

    def _build_headers(
        self,
        role: str,
        session_prefix: str = "normal",
        additional: Optional[Dict[str, str]] = None,
    ) -> Dict[str, str]:
        """Create the mandatory Sentinel headers.

        - ``Authorization`` – Bearer JWT for the chosen role.
        - ``X-Session-ID`` – deterministic session identifier.
        - ``User-Agent`` – realistic browser string.
        - ``X-Request-ID`` – unique request identifier.
        """
        token = self.tokens.get(role, "")
        headers = {
            "Authorization": f"Bearer {token}",
            "X-Session-ID": generate_session_id(session_prefix),
            "User-Agent": random_user_agent(),
            "X-Request-ID": str(uuid.uuid4()),
            "X-Risk-Score": str(round(random.uniform(0.0, 0.9), 2)),
            "Content-Type": "application/json",
        }
        if additional:
            headers.update(additional)
        return headers

    def send(
        self,
        endpoint: str,
        method: str = "GET",
        role: str = "USER",
        session_prefix: str = "normal",
        payload: Optional[Dict] = None,
        extra_headers: Optional[Dict[str, str]] = None,
        timeout: int = 10,
    ) -> requests.Response:
        """Send a request to *endpoint* using the configured gateway.

        Returns the ``requests.Response`` object so callers can inspect status code,
        body and any custom Sentinel response headers (e.g. ``X-Policy-Decision``).
        """
        url = f"{self.base_url}{endpoint}"
        headers = self._build_headers(role, session_prefix, extra_headers)
        logger.info("Sending %s request to %s (role=%s)", method, endpoint, role)
        try:
            if method.upper() == "GET":
                response = requests.get(url, headers=headers, timeout=timeout)
            else:
                json_body = payload if payload is not None else generate_payload()
                response = requests.request(method.upper(), url, headers=headers, json=json_body, timeout=timeout)
            # Log concise outcome for demo visibility
            logger.info(
                "Response: %s %s (policy=%s)",
                response.status_code,
                response.reason,
                response.headers.get("X-Policy-Decision", "N/A"),
            )
            return response
        except requests.RequestException as exc:
            logger.error("Request to %s failed: %s", url, exc)
            raise

# Helper function for quick one‑off usage (e.g. from the REPL)
def quick_send(endpoint: str, method: str = "GET", role: str = "USER") -> None:
    tg = TrafficGenerator()
    tg.send(endpoint, method, role)
