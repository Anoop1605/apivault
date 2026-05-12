import logging
import threading
import time
from typing import List

from attack_scenarios import SCENARIOS
from replay_client import trigger_forensic_workflow

# Configure top‑level logger
logging.basicConfig(level=logging.INFO, format="[%(asctime)s] %(levelname)s: %(message)s")
logger = logging.getLogger(__name__)

def run_scenario(scenario_func) -> None:
    """Execute a single scenario and log any exception."""
    name = scenario_func.__name__
    logger.info("Starting scenario: %s", name)
    try:
        scenario_func()
        logger.info("Finished scenario: %s", name)
    except Exception as exc:
        logger.error("Scenario %s raised an exception: %s", name, exc)

def main() -> None:
    """Run all demo scenarios, then invoke forensic APIs.

    * Normal and low‑volume scenarios run sequentially.
    * High‑frequency suspicious traffic runs in a separate thread to simulate
      concurrent attack bursts.
    """
    # Separate the burst scenario for concurrent execution
    burst_func = None
    other_funcs: List[callable] = []
    for fn in SCENARIOS:
        if fn.__name__ == "high_frequency_suspicious_requests":
            burst_func = fn
        else:
            other_funcs.append(fn)

    # Run the non‑burst scenarios sequentially
    for fn in other_funcs:
        run_scenario(fn)
        time.sleep(1)  # small pause between scenarios for readability

    # Run the burst scenario in its own thread (if present)
    if burst_func:
        logger.info("Launching burst scenario in background thread")
        thread = threading.Thread(target=run_scenario, args=(burst_func,))
        thread.start()
        thread.join()

    logger.info("All traffic scenarios completed. Triggering forensic workflow.")
    trigger_forensic_workflow()
    logger.info("Demo run complete.")

if __name__ == "__main__":
    main()
