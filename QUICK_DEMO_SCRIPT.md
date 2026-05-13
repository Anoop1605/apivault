# 🚀 Sentinel Project - Quick Demo Script

## FAST TRACK: Show Core Features in 15 Minutes

### Prerequisites ✅
- **All Backend Services Running:** Policy Engine (8082), Forensics (8083), Event Store (8081), Gateway (8080), PostgreSQL (5432)
- **Frontend Running:** http://localhost:3000
- **Database:** Real forensic data from demo sessions

---

## 📋 Demo Sequence (15 mins)

### Step 1: Dashboard Overview (2 mins)
**URL:** http://localhost:3000/
- Shows real-time threat alert
- Click **"🔍 Analyze Now"** or **"View Timeline"** button
- Explain: "This is a tamper-proof forensic system - every event is hash-verified"

### Step 2: Forensic Replay (6 mins) ⭐ MAIN DEMO
**URL:** http://localhost:3000/sessions
- Click on any **high-risk session** (Red/Orange ones)
- Click **"Replay Session"** button
- Shows step-by-step event replay with:
  - Original decision (ALLOW/BLOCK/REVIEW)
  - Risk score progression
  - Policy rules that matched
  - **Key Point:** "See how policy engine evaluates each event in real-time"

**Timeline View Shows:**
- Event sequence (login → access → policy check → decision)
- Risk scores increasing/decreasing
- Security decisions made at each step
- Hash-verified integrity

### Step 3: Policy What-If Simulation (4 mins)
**URL:** Click **"What-If Simulation"** button in session detail
- Adjust policy rules (Block SQL, Rate Limiting, etc.)
- Click **"Run Simulation"**
- Shows: "With improved rules, this attack would have been caught at step X"
- Demonstrate: Better policy = earlier attack detection

### Step 4: Alerts & Review (2 mins)
**URL:** http://localhost:3000/alerts
- Show active security alerts
- Explain severity levels: CRITICAL > HIGH > MEDIUM > LOW
- "These alerts were triggered by policy violations"

### Step 5: Close Out (1 min)
- **Key Takeaway:** "Every event is immutable, hash-verified, and policy-evaluated in real-time"
- "Tamper-proof audit log enables forensic analysis"

---

## 🔑 Key Talking Points

### Event Store (Hash-Chained Integrity)
```
Event 1 → Hash1
Event 2 + Hash1 → Hash2
Event 3 + Hash2 → Hash3
...
If ANY event is modified → entire chain breaks → tampering detected
```
**Demo:** "This proves no event was modified after being logged"

### Policy Replay
"We can replay the exact same events against DIFFERENT policies to see:
- How new policies would have performed
- Where attacks would have been caught earlier
- Impact of policy changes without redeploying"

### Risk Scoring
"Real-time ABAC (Attribute-Based Access Control) scoring:
- User + Resource + Action + Context → Risk Score
- Policies evaluated at every step
- Decisions logged and verified"

---

## 🎯 Success Metrics

- ✅ Dashboard loads real sessions
- ✅ Can navigate to session details
- ✅ Replay page shows real event timeline
- ✅ Risk scores visible and changing
- ✅ Can see policy decisions at each step
- ✅ No console errors in frontend
- ✅ API responses visible in network tab (DevTools)

---

## ⚡ If Time is Short (10 min quick demo)

1. **Dashboard** (1 min) - Show threat overview
2. **Pick 1 Session** (1 min) - Navigate to details
3. **Click Replay** (6 mins) - Main demo - play through timeline
4. **Key Point** (2 mins) - Explain hash-chaining & tampering detection

---

## 🔧 If APIs Not Responding

### Quick Fix 1: Force API Response via Browser Console
```javascript
// In browser console (F12):
fetch('http://localhost:8083/forensics/query/sessions')
  .then(r => r.json())
  .then(d => console.log(d))
```

### Quick Fix 2: Restart Services
```bash
# Terminal
docker-compose down
docker-compose up -d
# Wait 30 seconds for services to start
```

### Quick Fix 3: Check Service Logs
```bash
docker logs sentinel-forensics-service
docker logs sentinel-event-store
```

---

## 📱 UI Navigation Path

```
Dashboard (/) 
  ↓
Click Session Card
  ↓
Session Detail (/sessions/:id)
  ↓
Click "Replay Session" OR "What-If Simulation"
  ↓
Replay Page (/replay/:id) OR Simulation Page (/simulation/:id)
  ↓
Play through timeline / Adjust policy / See results

Or: Click "Alerts" (/alerts) to see security alerts
```

---

## 🎓 Elevator Pitch (30 seconds)

"Sentinel is a **zero-trust forensic system** that:
1. **Records every API call** with tamper-proof hash-chaining (blockchain-like)
2. **Evaluates policies** in real-time with ABAC risk scoring
3. **Enables forensic analysis** by replaying events with different policies
4. **Proves integrity** - any tampering immediately detected

This demo shows:
- A policy violation detected and blocked
- Forensic replay of the entire attack sequence  
- How different policies would have caught the attack earlier"

---

## 🎬 Demo Recording Tips

1. **Pre-record a good session:** Run demo once, screenshot good session IDs
2. **Test APIs first:** Make sure data is loading from backend
3. **Show DevTools Network tab** to prove real API calls
4. **Have backup slides:** In case services are slow

---

## ❓ Common Questions & Answers

**Q: Why is this different from typical WAF?**
A: "Sentinel provides FORENSIC ANALYSIS - it records every decision, allows replay with new policies, and proves no tampering occurred."

**Q: Can an attacker manipulate the logs?**
A: "No - the hash chain would break. We can verify integrity: hash(previous + event) must equal stored hash."

**Q: What if we need to update policies?**
A: "We replay past events with new policies to understand impact before deploying."

**Q: How is performance?**
A: "Real-time event processing - policy evaluation is in milliseconds, shown in demo."

---

## 📞 Troubleshooting Checklist

- [ ] Backend services running: `docker ps | grep sentinel`
- [ ] Frontend running: http://localhost:3000 loads
- [ ] Database has data: Check session count in backend
- [ ] Browser console clean: No CORS/network errors
- [ ] APIs responding: Check DevTools Network tab
- [ ] Pages loading: Dashboard, Sessions, Replay pages all work

---

**Status:** Ready for demo! 🎉
