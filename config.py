import os
import json
from dataclasses import dataclass
from typing import List
from dotenv import load_dotenv

# Load variables from .env file
load_dotenv()

@dataclass(frozen=True)
class Config:
    gateway_base_url: str
    user_token: str
    admin_token: str
    attacker_token: str
    forensics_admin_token: str
    # Optional endpoint lists – can be overridden via env vars as comma‑separated strings
    user_endpoints: List[str]
    admin_endpoints: List[str]
    payment_endpoints: List[str]
    forensics_endpoints: List[str]

def _list_from_env(name: str, default: str) -> List[str]:
    val = os.getenv(name, default)
    return [v.strip() for v in val.split(',') if v.strip()]

def load_config() -> Config:
    """Load configuration from environment variables.
    Raises RuntimeError if any required variable is missing.
    """
    required = [
        "GATEWAY_BASE_URL",
        "USER_TOKEN",
        "ADMIN_TOKEN",
        "ATTACKER_TOKEN",
        "FORENSICS_ADMIN_TOKEN",
    ]
    missing = [var for var in required if not os.getenv(var)]
    if missing:
        raise RuntimeError(f"Missing required environment variables: {', '.join(missing)}")

    return Config(
        gateway_base_url=os.getenv("GATEWAY_BASE_URL").rstrip('/'),
        user_token=os.getenv("USER_TOKEN"),
        admin_token=os.getenv("ADMIN_TOKEN"),
        attacker_token=os.getenv("ATTACKER_TOKEN"),
        forensics_admin_token=os.getenv("FORENSICS_ADMIN_TOKEN"),
        user_endpoints=_list_from_env("USER_ENDPOINTS", "/api/users/profile"),
        admin_endpoints=_list_from_env("ADMIN_ENDPOINTS", "/api/admin/policies,/admin/policies"),
        payment_endpoints=_list_from_env("PAYMENT_ENDPOINTS", "/api/payments,/api/payments/*"),
        forensics_endpoints=_list_from_env("FORENSICS_ENDPOINTS", "/forensics/sessions,/forensics/events,/forensics/replay,/forensics/simulate"),
    )

# Export a singleton for convenient imports
CONFIG = load_config()
