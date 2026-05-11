# 🔐 Sentinel Project - End-to-End Demo Testing Guide

## ✅ Project Status: ALL SERVICES RUNNING

```
✅ Policy Engine (8082)    - ABAC policy & risk scoring
✅ Forensics Service (8083) - Forensics & replay
✅ Event Store (8081)       - Tamper-proof audit log
✅ Gateway (8080)          - Orchestrates calls
✅ PostgreSQL (5432)       - Database
✅ Mock Services           - User, Payment, Admin
```

---

## 🎯 Core Features (Main Demo Path)

### 1. **Event Store - Tamper-Proof Audit Log** (Core Feature)
   - Hash-chained blockchain-like integrity verification
   - Events cannot be tampered without breaking chain
   
### 2. **Session Timeline - Forensic View** (Core Feature)
   - View sequence of events in a session
   - See policies applied at each step
   - Understand attack progression

### 3. **What-If Analysis / Policy Replay** (Core Feature)
   - Replay same session with DIFFERENT policies
   - See how different policies would have blocked the attack
   - Show proof that tampering is detectable

### 4. **Alerts & Risk Scoring** (Core Feature)
   - Risk scores for each event
   - Security alerts for policy violations

---

## 🧪 Quick Testing Checklist

### Phase 1: Verify Backend Connection (5 mins)
```bash
# Test Event Store API
curl http://localhost:8081/events

# Test Policy Engine API  
curl http://localhost:8082/policies

# Test Forensics Service API
curl http://localhost:8083/forensics/query/sessions

# Test Gateway
curl http://localhost:8080/gateway/health
```

### Phase 2: Frontend Load & Core Features (10 mins)

**Start Frontend:**
```bash
cd frontend
npm install  # if not done
npm run dev
# Opens at http://localhost:5173
```

**Test Sequence:**

1. **Dashboard Page** ✅
   - Should load session list from backend
   - Shows real forensic sessions
   - **Action:** Scroll and look for sessions

2. **Sessions Page** ⚠️  
   - **CURRENTLY USING MOCK DATA** - needs real data
   - Should load from backend instead
   - **Action:** Check if real sessions appear

3. **Select a Session → Click View** 
   - Should navigate to SessionDetail page
   - Shows event timeline from backend

4. **Click "Replay Session"**
   - **ReplayPage - This is CONNECTED ✅**
   - Shows forensic replay with real data
   - Displays step-by-step event decisions
   - Shows risk progression
   - **Action:** Play through the replay

5. **Click "What-If Simulation"**  
   - **SimulationPage - Uses MOCK DATA** ⚠️
   - Should apply what-if policy changes
   - Shows how events would be re-evaluated
   - **Action:** Try changing policy rules

---

## 🔧 Backend Connection Points

```
Frontend                    Backend Service         Port
─────────────────────────────────────────────────
Dashboard.tsx        ──→    Forensics Service     8083
Sessions.tsx         ──→    ❌ MOCK DATA
SessionDetail.tsx    ──→    Forensics Service     8083
ReplayPage.tsx       ──→    Forensics Service     8083
SimulationPage.tsx   ──→    ❌ MOCK DATA
Alerts.tsx           ──→    ❌ MOCK DATA
```

---

## 📊 Demo Flow (For Presentation - 15 mins)

1. **Show Dashboard**
   - Real sessions loading from database
   - Risk scores and event counts
   - "This data is tamper-proof and hash-verified"

2. **Click on High-Risk Session**
   - Show session details
   - Explain the attack sequence

3. **Click "Replay Session"** ⭐ MAIN DEMO
   - Show forensic replay in action
   - "Watch the policy engine evaluate each event"
   - Highlight CRITICAL decisions
   - Show risk progression
   - **Key Point:** "Every event is hash-chained and cannot be altered"

4. **Click "What-If Simulation"** ⭐ SECONDARY DEMO
   - Adjust policy rules
   - Show how same session would have different outcome
   - "Better policy would have caught attack at step 3"

5. **Show Alerts Page**
   - Security alerts generated
   - Risk scores for violations

---

## ⚠️ Known Issues & Workarounds

### Issue 1: Sessions Page Using Mock Data
**Problem:** Sessions.tsx hardcoded with mock sessions
**Impact:** Doesn't reflect real database data
**Workaround:** 
- Manually click on dashboard session cards instead
- Or update Sessions.tsx to call backend (see fix below)

### Issue 2: Alerts Using Mock Data
**Problem:** Alerts.tsx hardcoded with mock alerts
**Impact:** Doesn't show real security alerts
**Workaround:**
- Focus on ReplayPage for real attack visualization
- Alerts can be demo'd later with real data

### Issue 3: Simulation Using Mock Data
**Problem:** SimulationPage.tsx uses hardcoded events
**Impact:** Doesn't replay actual session data
**Workaround:**
- Show concept with mock data
- Explain "In production, uses real events from database"

---

## 🔍 How to Verify Services Are Connected

### Check Event Store Chain
```bash
curl http://localhost:8081/events | jq '.[] | {eventId, hash, previousHash}'
```

### Check Session Forensics
```bash
curl http://localhost:8083/forensics/query/sessions | jq '.[0]'
```

### Check Frontend Logs
Open browser DevTools (F12) → Console
- Should see successful API calls
- No 404 or connection errors
- Real data in network tab

---

## 🚀 To Run Complete Demo

```bash
# Terminal 1: Start frontend
cd frontend && npm run dev

# Terminal 2: Keep backend running (already running in Docker)
docker ps  # verify containers

# Terminal 3: Optional - Watch logs
docker logs -f sentinel-forensics-service
```

**Navigate in browser:**
1. Go to http://localhost:5173
2. Dashboard auto-loads (real data) ✅
3. Click a session
4. Click "Replay Session" ⭐
5. Show the forensic timeline
6. Explain hash-chaining & tampering detection

---

## 📋 Success Criteria

- [ ] Dashboard loads real sessions
- [ ] Can navigate to session details
- [ ] Replay page shows real event timeline
- [ ] Risk scores visible and changing
- [ ] Hash verification working (no tampering alerts)
- [ ] API responses visible in browser DevTools
- [ ] No console errors in frontend

---

## 🎓 Key Talking Points for Demo

1. **Event Store Integrity**
   - "Events are hash-chained like blockchain"
   - "Any tampering breaks the chain"
   - "Proves forensic audit log is tamper-proof"

2. **Policy Replay**
   - "Reconstruct exact decision flow"
   - "Apply new policies to past events"
   - "See how new policies would have performed"

3. **Risk Scoring**
   - "Real-time risk evaluation"
   - "ABAC rules determine access"
   - "Shows attack progression"

4. **Forensic Analysis**
   - "Timeline view of attack"
   - "Each event decision visible"
   - "Understand attacker behavior"

---

## 📞 Troubleshooting

**Frontend not loading?**
```bash
cd frontend
npm install
npm run dev
```

**Backend not responding?**
```bash
docker ps  # Check all containers running
docker logs sentinel-forensics-service  # Check logs
```

**API returning 404?**
```bash
# Verify services are on correct ports
docker port sentinel-forensics-service
docker port sentinel-event-store
```

**Session data not showing?**
```bash
# Check if database has data
docker exec sentinel-postgres psql -U sentinel -d sentinel_db -c "SELECT COUNT(*) FROM events;"
```

---

## ✅ Next Steps

1. **Quick Test:** Run through Phase 1 & 2 checklist
2. **Demo Flow:** Follow the 15-minute demo path above
3. **Time Crunch:** Skip Alerts & Simulation pages, focus on Replay
4. **Extended:** Add more sessions/attacks to make demo more impressive

**Estimated Time:** 20-30 minutes for full demo, 10-15 for quick version
