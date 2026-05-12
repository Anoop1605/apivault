import logging
import time
from typing import List
import requests

from config import CONFIG

logger = logging.getLogger(__name__)

FORGE_ENDPOINTS: List[str] = [
    "/forensics/query/sessions",
    "/forensics/events",
    "/forensics/replay",
    "/forensics/simulate",
]


def trigger_forensic_workflow() -> None:
    """Call the Sentinel forensics APIs sequentially.

    This demonstrates that events persisted during traffic generation can be
    retrieved, replayed, and used for "what‑if" simulations.
    """
    base = CONFIG.gateway_base_url.rstrip('/')
    for ep in FORGE_ENDPOINTS:
        url = f"{base}{ep}"
        try:
            method = requests.get if "sessions" in ep else requests.post
            response = method(url, headers={"Authorization": f"Bearer {CONFIG.forensics_admin_token}"}, timeout=10)
            logger.info("%s %s -> %s (%s)", method.__name__.upper(), ep, response.status_code, response.reason)
        except Exception as exc:
            logger.error("Failed to call %s: %s", ep, exc)
        time.sleep(0.5)  # small pause to avoid hammering the service
