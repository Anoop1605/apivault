# 🚀 SENTINEL - START DEMO NOW!

## ✅ Pre-Demo Status
- **All 8 Services:** ✅ Running
- **Frontend:** ✅ http://localhost:3000
- **Database:** ✅ PostgreSQL running
- **CORS Fixed:** ✅ All backends configured
- **Time to Demo:** 🎯 NOW

---

## 📱 DEMO FLOW (15 minutes)

### Step 1: Dashboard Overview (2 min)
```
1. Open http://localhost:3000 in browser
2. Point out the threat alert banner at top
3. Show statistics cards:
   - Total Sessions
   - Threat Alerts
   - Policy Violations
   - Attack Sequences
```

**What to say:** *"This is our Sentinel security dashboard. We use zero-trust architecture with ABAC policies. Every action is logged in a tamper-proof event store with hash-chaining."*

---

### Step 2: Sessions List (1 min)
```
1. Click "Sessions" in navbar (or scroll to Sessions table)
2. Show the list of forensic sessions
3. Point out Risk Level column (High, Medium, Low)
```

**What to say:** *"Each session represents a series of security events. We can replay any session forensically to see exactly what happened."*

---

### Step 3: Select High-Risk Session (1 min)
```
1. Click on a session marked "High Risk" or "CRITICAL"
2. If sessions table doesn't show, click "Replay Session" button
3. Wait for replay data to load
```

**What to say:** *"Let me pick a critical session that shows suspicious activity..."*

---

### Step 4: ⭐ FORENSIC REPLAY (6 min) - **MAIN DEMO**
```
1. You're now in ReplayPage with the forensic timeline
2. Click "Play" button to start replay
3. Watch events execute step-by-step
4. Use controls:
   - Play/Pause: Control replay speed
   - Next: Jump to next event
   - Prev: Go back to previous event
5. Show current step info:
   - Event timestamp
   - Action type (LOGIN, ACCESS, MODIFY, etc.)
   - User/Resource affected
   - Policy evaluation result
```

**What to say:**
*"Here's where Sentinel shines. We're replaying the exact sequence of events that happened during this security incident. Notice each step shows:*
- *The timestamp of when it happened*
- *What action was taken (login, file access, etc.)*
- *Who did it and what resource they touched*
- *Whether our ABAC policy allowed or blocked it*

*This hash-chained, immutable log means an attacker can't cover their tracks - we can see everything."*

---

### Step 5: What-If Simulation (3 min)
```
1. Look for "What-If Scenario" or "Policy Change" button
2. Show different policy evaluation
3. Explain impact on same replay
```

**What to say:** *"If we had a different policy in place, would this attack have been caught? Let's see..."*

---

### Step 6: Alerts & Analytics (2 min)
```
1. Click "Alerts" in navbar
2. Show alert list with threat scores
3. Click an alert to see details
```

**What to say:** *"Our system generates alerts based on policy violations and anomaly detection. Each alert is tied back to the immutable event log."*

---

## 🔑 Key Talking Points

### Architecture
- **Zero-Trust:** All requests validated, no implicit trust
- **ABAC:** Attribute-Based Access Control (not just roles)
- **Immutable Audit Log:** Hash-chained events can't be tampered
- **Forensic Replay:** Reconstruct security incidents exactly

### Why It Matters
- **Compliance:** Audit trail for regulations (SOC2, ISO27001)
- **Investigation:** Replay incidents, see what actually happened
- **Prevention:** Real-time ABAC policy prevents unauthorized access
- **Detection:** Anomalies caught through event correlation

### Demo Highlights
- **Real Data:** Using actual event stream from database
- **Policy Enforcement:** Live ABAC evaluation
- **Tamper-Proof:** Hash-chain prevents modification
- **Speed:** Policy evaluation in <100ms

---

## 🛠️ Troubleshooting

| Issue | Solution |
|-------|----------|
| Frontend blank | Refresh http://localhost:3000 (Ctrl+R) |
| No sessions shown | Check DB: `docker exec sentinel-postgres psql -U sentinel -d sentinel_db -c "SELECT COUNT(*) FROM sessions;"` |
| Replay doesn't load | Wait 3 seconds, then click session again |
| CORS error in console | All fixed - backends reconfigured |
| Service down | Run: `docker-compose up -d` |

---

## 📊 Service Ports

| Service | Port | Purpose |
|---------|------|---------|
| Frontend | 3000 | React UI |
| Gateway | 8080 | API Gateway |
| Event Store | 8081 | Immutable event log |
| Policy Engine | 8082 | ABAC policy evaluation |
| Forensics | 8083 | Forensic replay & analysis |
| Mock Services | 9001-9003 | Admin/Payment/User services |
| PostgreSQL | 5432 | Event database |

---

## ⏱️ Timing Summary

```
Dashboard:        2 min ✓
Sessions:         1 min ✓
Select Session:   1 min ✓
Forensic Replay:  6 min ✓ (MAIN)
What-If:          3 min ✓
Alerts:           2 min ✓
─────────────────────────
Total:           15 min
```

---

## 🎯 Demo Script (Paste-Friendly)

> *"Sentinel is a real-time security forensics system built on zero-trust architecture with ABAC policies and an immutable audit log. Today I'm going to show you how it helps organizations investigate security incidents, prove compliance, and prevent attacks.*
>
> *Everything you see - every login, every file access, every policy decision - is recorded in a tamper-proof, hash-chained event store. This means when a security incident happens, we can replay it forensically and see exactly what went wrong.*
>
> *Let me walk you through a real incident replay..."*

---

## ✨ Pro Tips

1. **Before demo starts:** Open DevTools (F12) and show Network tab - attendees can see real API calls
2. **During replay:** Point to specific events and explain policy evaluation
3. **If stuck:** "Let me refresh this" - never looks bad to troubleshoot
4. **Highlight speed:** "Policy evaluation in <100ms" - shows performance
5. **End with:** "Questions?" - always gives you recovery time

---

## 🚀 GO!
```
1. Open http://localhost:3000
2. Show Dashboard
3. Click Sessions
4. Pick High-Risk session
5. Click Replay
6. Hit Play button
7. Explain what you see
8. Done! 🎉
```

**You're ready. Good luck!** 🎯
