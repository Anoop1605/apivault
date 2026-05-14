# ⚡ QUICK START GUIDE - Sentinel Session Management
## Step-by-Step Instructions for Creating and Viewing Sessions

---

## 🚀 QUICK COMMANDS

### Create New Sessions (With Verification)
```bash
cd c:\Users\shreesha\Documents\meticulous\New folder\apivault
python run_workflow.py
```

**What it does:**
- ✅ Creates 3 session types (Normal, Policy Violation, Attack)
- ✅ Inserts 15 security events into database
- ✅ Verifies data in PostgreSQL
- ✅ Verifies data accessible via Forensics API
- ✅ Outputs session IDs and URLs

---

### View All Sessions
```bash
python view_sessions.py
```

**Output:**
- Shows all 6 sessions (3 original + 3 new)
- Displays risk levels and statistics
- Shows navigation guide for UI

---

### View Specific Session Details
```bash
python view_sessions.py --session-id c8783965-4f0e-49ae-8042-3ab20a11c7a2
```

**Output:**
- Session details and risk score
- All events with timeline
- Event types, decisions, endpoints
- Direct UI link

---

### Export Sessions to CSV
```bash
python view_sessions.py --export-csv
```

**Output:**
- Creates `sessions_export.csv`
- Lists all sessions with risk levels

---

## 🌐 UI NAVIGATION

### Dashboard (Overview)
```
http://localhost:3001
```
Shows:
- Real-time threat alerts
- Security metrics
- Recent sessions
- Risk statistics

### Sessions List (All Sessions)
```
http://localhost:3001/sessions
```
Shows:
- All 6 sessions
- Risk levels
- Event counts
- Filter options (Active, Suspicious, Compromised)

### Specific Session (Session 1 - Normal Activity)
```
http://localhost:3001/sessions/c8783965-4f0e-49ae-8042-3ab20a11c7a2
```

### Specific Session (Session 2 - Policy Violation)
```
http://localhost:3001/sessions/a9652ef4-e1d6-4da9-a100-aa1ad2ae371f
```

### Specific Session (Session 3 - Attack Sequence - CRITICAL)
```
http://localhost:3001/sessions/8854ee05-58f9-4f58-b7f6-a263adb98b35
```

### Alerts
```
http://localhost:3001/alerts
```
Shows:
- Security alerts by severity
- Policy violations
- Attack detections

---

## 📊 WORKFLOW VERIFICATION

### Step 1️⃣: Run Workflow Script
```bash
python run_workflow.py
```
**Expected Output:**
```
✅ Database: Connected, 30 total events
✅ Event Store: Running (Status: 404 but data accessible)
✅ Forensics Service: Running (Status: 200), 3+ sessions available
✅ Frontend: Running (Status: 200)
✅ 3 new sessions created
✅ 15 new events inserted
```

### Step 2️⃣: Verify in Database
```bash
# Check total events
docker exec -i sentinel-postgres psql -U sentinel -d sentinel_db -c "SELECT COUNT(*) FROM security_events;"

# Expected: 30 rows
```

### Step 3️⃣: Check Forensics API
```bash
# View all sessions via API
curl http://localhost:8083/forensics/query/sessions
```

### Step 4️⃣: Open Dashboard
```
http://localhost:3001
```
Should show:
- ✅ Threat alerts
- ✅ Session metrics
- ✅ Live activity

### Step 5️⃣: View Sessions
```
http://localhost:3001/sessions
```
Should show:
- ✅ All 6 sessions listed
- ✅ Risk levels calculated
- ✅ Event counts shown

### Step 6️⃣: Replay Session
1. Click on any session from the list
2. View the event timeline
3. See step-by-step forensic details
4. Check policy evaluation results

---

## 🎯 SESSION TYPES CREATED

### 1. NORMAL ACTIVITY (LOW RISK - 0%)
**Session ID:** `c8783965-4f0e-49ae-8042-3ab20a11c7a2`

Events:
- LOGIN → /auth/login → ✅ ALLOW
- ACCESS → /api/users → ✅ ALLOW
- ACCESS → /api/payments → ✅ ALLOW
- LOGOUT → /auth/logout → ✅ ALLOW

**Use for:** Testing normal operation and baseline comparison

---

### 2. POLICY VIOLATION (HIGH RISK - 60%)
**Session ID:** `a9652ef4-e1d6-4da9-a100-aa1ad2ae371f`

Events:
- LOGIN → /auth/login → ✅ ALLOW
- ACCESS → /api/admin/users → ❌ BLOCK
- ACCESS → /api/admin/settings → ❌ BLOCK
- ACCESS → /api/sensitive-data → ❌ BLOCK
- LOGOUT → /auth/logout → ✅ ALLOW

**Use for:** Testing policy enforcement and violation detection

---

### 3. ATTACK SEQUENCE (CRITICAL RISK - 126.7%)
**Session ID:** `8854ee05-58f9-4f58-b7f6-a263adb98b35`

Events:
- LOGIN → /auth/login → ✅ ALLOW
- ATTACK → /auth/login (brute force) → ❌ BLOCK
- ATTACK → /api/users?id=1' OR '1'='1 (SQL injection) → ❌ BLOCK
- ACCESS → /api/export → ❌ BLOCK
- ATTACK → /api/admin/database → ❌ BLOCK
- LOGOUT → /auth/logout → ✅ ALLOW

**Use for:** Testing attack detection and forensic replay

---

## 🔍 VERIFICATION CHECKLIST

### Database
- [ ] 30 events total in database
- [ ] 6 unique sessions
- [ ] New sessions visible in security_events table
- [ ] Timestamps and hashes stored

### Backend APIs
- [ ] Event Store (Port 8081) running
- [ ] Forensics Service (Port 8083) responding
- [ ] Policy Engine (Port 8082) active
- [ ] Gateway (Port 8080) working

### Frontend
- [ ] Dashboard loads (http://localhost:3001)
- [ ] Sessions list displays all 6 sessions
- [ ] Individual sessions load correctly
- [ ] Timeline shows events

### Event Flow
- [ ] Events created in database
- [ ] Events accessible via API
- [ ] Events displayed in UI
- [ ] Risk levels calculated correctly
- [ ] Session statuses (Normal/Suspicious/Compromised) shown

---

## 🛠️ TROUBLESHOOTING

### Sessions not showing in list
```bash
# Refresh the page
# Or restart frontend: Ctrl+C in terminal, then npm run dev
```

### Events not in database
```bash
# Check if docker containers are running
docker ps

# Verify database connection
docker exec -i sentinel-postgres psql -U sentinel -d sentinel_db -c "SELECT * FROM security_events LIMIT 1;"
```

### API returning 404
```bash
# Check if Forensics service is running
curl http://localhost:8083/forensics/query/sessions

# If not responding, restart Docker containers
cd apivault
docker-compose restart forensics-service
```

### Frontend not loading
```bash
# Check if frontend is still running
# Look for "Vite" in the terminal
# If not, restart: npm run dev
```

---

## 📞 QUICK REFERENCE

| Component | URL/Port | Check Command |
|-----------|----------|----------------|
| Frontend | :3001 | Open in browser |
| Gateway | :8080 | curl http://localhost:8080/api/health |
| Event Store | :8081 | curl http://localhost:8081/api/health |
| Policy Engine | :8082 | curl http://localhost:8082/api/health |
| Forensics | :8083 | curl http://localhost:8083/forensics/query/sessions |
| Database | :5432 | psql connection string |

---

## 💡 TIPS

1. **Faster Session Creation:** Run `python run_workflow.py` multiple times to create more sessions
2. **Export Data:** Use `python view_sessions.py --export-csv` to get session data in Excel
3. **Session Replay:** Click any session in the UI and scroll to see the event timeline
4. **Risk Filtering:** Use filter buttons on Sessions page (All, Active, Suspicious, Compromised)
5. **Browser DevTools:** Open F12 to see network requests and debug any issues

---

**Last Updated:** 2026-05-14  
**Status:** ✅ All Systems Operational
