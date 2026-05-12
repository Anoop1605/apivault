import uuid
import random
import time
from typing import Dict

def generate_session_id(prefix: str) -> str:
    """Generate a deterministic session identifier.
    Example: prefix='normal' -> 'normal-session-1234'
    """
    return f"{prefix}-session-{random.randint(1000, 9999)}"

def random_user_agent() -> str:
    """Return a realistic User-Agent string chosen at random."""
    browsers = [
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36",
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.0 Safari/605.1.15",
        "Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.0 Mobile/15E148 Safari/604.1",
        "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36",
    ]
    return random.choice(browsers)

def generate_payload(size: int = 256) -> Dict[str, str]:
    """Create a synthetic JSON‑like payload for POST/PUT requests.
    The content is intentionally random but mimics realistic fields.
    """
    return {
        "request_id": str(uuid.uuid4()),
        "timestamp": int(time.time()),
        "data": ''.join(random.choices('abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789', k=size)),
    }
