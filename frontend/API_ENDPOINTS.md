# API Vault - Complete API Endpoints Reference

## Overview
This document lists all available API endpoints across the microservices architecture. The frontend can consume these endpoints through the gateway or directly.

---

## 🔌 Service Ports

| Service | Port | URL |
|---------|------|-----|
| Payment Service (Mock) | 9001 | `http://localhost:9001` |
| User Service (Mock) | 9002 | `http://localhost:9002` |
| Admin Service (Mock) | 9003 | `http://localhost:9003` |
| Event Store Service | 8081 | `http://localhost:8081` |
| Policy Engine Service | 8082 | `http://localhost:8082` |
| Forensics Replay Service | 8083 | `http://localhost:8083` |
| Forensic Dashboard | 8084 | `http://localhost:8084` |
| Gateway Service | 8080 | `http://localhost:8080` |
| Frontend (Vite Dev) | 3000 | `http://localhost:3000` |

---

## 📋 API Endpoints by Service

### 1. 💰 Payment Service (Mock Backend)
**Base URL:** `http://localhost:9001`

#### Get All Payments
```
GET /api/payments
Description: List all payments
Response: List of payment objects with id, amount, currency, status, timestamp
```

#### Create Payment
```
POST /api/payments
Description: Create a new payment
Body: { "amount": 100.00, "currency": "USD", ... }
Response: { "id": "uuid", "status": "CREATED", "timestamp": "..." }
```

#### Delete Payment
```
DELETE /api/payments/delete
Description: Delete a payment
Response: { "status": "DELETED", "message": "...", "timestamp": "..." }
```

---

### 2. 👥 User Service (Mock Backend)
**Base URL:** `http://localhost:9002`

#### Get User Profile
```
GET /api/users/profile
Description: Get current user profile
Headers: X-Session-ID (optional)
Response: { "userId": "user-9921", "name": "...", "email": "...", "role": "...", "department": "...", "lastLogin": "..." }
```

#### Get All Users
```
GET /api/users
Description: List all users
Response: List of user objects with userId, name, role
```

#### Create User
```
POST /api/users
Description: Create a new user
Body: { "name": "...", "email": "...", "role": "..." }
Response: { "userId": "uuid", "status": "CREATED", "message": "...", "timestamp": "..." }
```

---

### 3. ⚙️ Admin Service (Mock Backend)
**Base URL:** `http://localhost:9003`

#### Get Dashboard
```
GET /api/admin/dashboard
Description: Get admin dashboard metrics
Response: { "totalUsers": 156, "activeServices": 3, "systemStatus": "HEALTHY", "uptime": "72h 15m", "lastUpdated": "..." }
```

#### Get Config
```
GET /api/admin/config
Description: Get system configuration
Response: { "gatewayVersion": "2.0.0", "policyEngineActive": true, "riskScorerEnabled": true, "eventStoreStatus": "CONNECTED", "maxConcurrentRequests": 500 }
```

---

### 4. 📦 Event Store Service
**Base URL:** `http://localhost:8081`

#### Write Event
```
POST /api/events
Description: Persist a single event to the event store (called by gateway)
Body: EventDTO { eventType, sessionId, timestampNs, decision, endpoint, ... }
Response: { "eventId": "uuid", "status": "CREATED", "timestamp": "..." }
HTTP Status: 201 (Created) or 502 (Bad Gateway if write fails)
```

#### Get Events by Session
```
GET /events/sessions/{sessionId}
Description: Returns all events for a session, ordered by timestamp ascending
Parameters: sessionId (UUID)
Response: List<EventDTO> - array of events
HTTP Status: 200 (OK) or 404 (Not Found)
```

---

### 5. 🔐 Policy Engine Service
**Base URL:** `http://localhost:8082`

#### Policy Admin Controller
```
(Endpoints available but implementation pending)
Intended for policy management and rule updates
```

---

### 6. 🔍 Forensics Replay Service (Main Forensics API)
**Base URL:** `http://localhost:8083`

#### ➤ REPLAY ENDPOINTS

##### Get Session Timeline (Replay)
```
GET /forensics/sessions/{sessionId}/timeline
Description: Reconstructs exact decision sequence using frozen policy snapshot
Parameters: sessionId (UUID)
Response: ReplayReport with full decision trace
Security: Basic Auth
```

##### What-If Simulation
```
POST /forensics/sessions/{sessionId}/whatif
Description: Re-evaluates session events against alternate policy rules
Parameters: sessionId (UUID)
Body: PolicySnapshot { "rules": ["BLOCK_ATTACK", "BLOCK_DELETE", "ALLOW_GET"] }
Response: ReplayReport with divergence analysis showing original vs simulated decisions
Security: Basic Auth
```

#### ➤ FORENSIC QUERY ENDPOINTS

##### List All Sessions
```
GET /forensics/query/sessions
Description: Returns summary metadata for all known replay sessions
Response: List<SessionSummaryDTO>
Security: Basic Auth
```

##### Get Replay Report
```
GET /forensics/query/sessions/{sessionId}/report
Description: Returns the full replay report for a session
Parameters: sessionId (UUID)
Response: ReplayReport with per-step decisions and tamper-evident hash
Security: Basic Auth
```

##### Verify Session Hash
```
GET /forensics/query/sessions/{sessionId}/verify
Description: Re-computes session hash and compares to provided value
Parameters: 
  - sessionId (UUID)
  - hash (query param): Expected SHA-256 hash (64 hex chars)
Response: boolean (true = untampered, false = hash mismatch)
Security: Basic Auth
```

##### Verify Individual Event Hashes
```
GET /forensics/query/sessions/{sessionId}/verify-hashes
Description: Re-computes canonical SHA-256 for every event in session
Parameters: sessionId (UUID)
Response: List<VerificationResult> - only tampered events (empty = all clean)
Security: Basic Auth
```

#### ➤ DASHBOARD ENDPOINTS

##### Get Session Timeline (Dashboard View)
```
GET /forensics/dashboard/sessions/{sessionId}/timeline
Description: Chronological list of events for a session with event type and policy decision
Parameters: sessionId (UUID)
Response: List<TimelineEventDTO>
Security: Basic Auth
```

##### Get Policy Trace
```
GET /forensics/dashboard/sessions/{sessionId}/policy-trace
Description: Full policy evaluation trace: rules evaluated, rules matched, final decision, hash
Parameters: sessionId (UUID)
Response: PolicyTraceDTO
Security: Basic Auth
```

##### Get Session Alerts
```
GET /forensics/dashboard/sessions/{sessionId}/alerts
Description: All BLOCK and REVIEW events as alerts with severity levels
Parameters: sessionId (UUID)
Response: List<AlertDTO> - HIGH severity for BLOCK, MEDIUM for REVIEW
Security: Basic Auth
```

---

### 7. 📊 Forensic Dashboard Service (Server-Side Rendered Views)
**Base URL:** `http://localhost:8084`

#### Dashboard Home
```
GET /dashboard
Description: List of all sessions (HTML view)
Response: HTML page with session explorer
```

#### Session Timeline
```
GET /dashboard/sessions/{sessionId}/timeline
Description: Chronological event timeline for a session (HTML view)
Parameters: sessionId (UUID)
Response: HTML page with timeline visualization
```

#### Session Policy Trace
```
GET /dashboard/sessions/{sessionId}/policy-trace
Description: Policy decision trace for a session (HTML view)
Parameters: sessionId (UUID)
Response: HTML page with policy trace
```

#### What-If Launcher
```
GET /dashboard/sessions/{sessionId}/whatif
Description: What-if simulation launcher page (HTML view)
Parameters: sessionId (UUID)
Response: HTML form to enter simulation rules
```

#### Run What-If Simulation
```
POST /dashboard/sessions/{sessionId}/whatif
Description: Execute what-if simulation with alternate rules
Parameters: 
  - sessionId (UUID)
  - rules (query param): Comma-separated rules
Response: HTML page with simulation results
```

#### List Alerts
```
GET /dashboard/alerts
Description: All sessions with BLOCK/REVIEW decisions (HTML view)
Response: HTML page with alerts list
```

#### Session Alerts
```
GET /dashboard/alerts/{sessionId}
Description: Alerts for a specific session (HTML view)
Parameters: sessionId (UUID)
Response: HTML page with session alerts
```

---

## 📊 Data Models

### EventDTO
```json
{
  "eventId": "uuid",
  "sessionId": "uuid",
  "timestampNs": 1707825800300000000,
  "eventType": "LOGIN|LOGOUT|ACCESS|ATTACK",
  "userId": "user-001",
  "endpoint": "/api/payments/txn-9921",
  "httpMethod": "GET|POST|PUT|DELETE|PATCH",
  "decision": "ALLOW|BLOCK|REVIEW",
  "policyRuleId": "BLOCK_DELETE",
  "policyRuleVersion": 1,
  "policyRuleSnapshotId": "uuid",
  "riskScore": 0.85,
  "bodyHash": "sha256...",
  "gatewayVersion": "1.0.0"
}
```

### ReplayReport
```json
{
  "sessionId": "uuid",
  "steps": [
    {
      "event": { ...EventDTO },
      "originalDecision": "ALLOW|BLOCK|REVIEW",
      "simulatedDecision": "ALLOW|BLOCK|REVIEW",
      "ruleMatched": "BLOCK_DELETE",
      "diverged": false
    }
  ],
  "originalDecisions": ["ALLOW", "ALLOW", "BLOCK"],
  "simulatedDecisions": ["ALLOW", "ALLOW", "BLOCK"],
  "firstDivergenceStep": -1,
  "snapshotIdUsed": "uuid",
  "hash": "a3f1c2d4e5b67890abcdef1234567890abcdef1234567890abcdef1234567890"
}
```

### SessionSummaryDTO
```json
{
  "sessionId": "uuid",
  "startTimestampNs": 1707825800300000000,
  "endTimestampNs": 1707825900300000000,
  "eventCount": 6,
  "hash": "sha256..."
}
```

### AlertDTO
```json
{
  "sessionId": "uuid",
  "timestampNs": 1707825800300000000,
  "triggerEventType": "LOGIN|LOGOUT|ACCESS|ATTACK",
  "severity": "LOW|MEDIUM|HIGH|CRITICAL",
  "message": "Event blocked by policy: BLOCK_DELETE"
}
```

### PolicyTraceDTO
```json
{
  "sessionId": "uuid",
  "rulesEvaluated": ["BLOCK_ATTACK", "BLOCK_DELETE", "ALLOW_GET"],
  "rulesMatched": ["BLOCK_DELETE"],
  "finalDecision": "BLOCK",
  "hash": "sha256..."
}
```

### TimelineEventDTO
```json
{
  "sessionId": "uuid",
  "timestampNs": 1707825800300000000,
  "eventType": "LOGIN|LOGOUT|ACCESS|ATTACK",
  "decision": "ALLOW|BLOCK|REVIEW",
  "ruleMatched": "BLOCK_DELETE"
}
```

---

## 🔑 Authentication

- **Forensics APIs (8083)**: Basic Auth required
- **Mock Services (9001-9003)**: No authentication required
- **Event Store (8081)**: No authentication required
- **Dashboard (8084)**: Session-based

---

## 🚀 Quick Integration Guide

### 1. In `src/services/api.ts`, add service methods:

```typescript
// Forensic Query Services
export const forensicService = {
  // Replay
  getSessionTimeline: (sessionId: string) => 
    api.get(`/forensics/sessions/${sessionId}/timeline`),
  
  runWhatIf: (sessionId: string, rules: string[]) =>
    api.post(`/forensics/sessions/${sessionId}/whatif`, { rules }),
  
  // Query
  listSessions: () => api.get('/forensics/query/sessions'),
  getReplayReport: (sessionId: string) =>
    api.get(`/forensics/query/sessions/${sessionId}/report`),
  verifyHash: (sessionId: string, hash: string) =>
    api.get(`/forensics/query/sessions/${sessionId}/verify`, { params: { hash } }),
  verifyEventHashes: (sessionId: string) =>
    api.get(`/forensics/query/sessions/${sessionId}/verify-hashes`),
  
  // Dashboard
  getTimeline: (sessionId: string) =>
    api.get(`/forensics/dashboard/sessions/${sessionId}/timeline`),
  getPolicyTrace: (sessionId: string) =>
    api.get(`/forensics/dashboard/sessions/${sessionId}/policy-trace`),
  getAlerts: (sessionId: string) =>
    api.get(`/forensics/dashboard/sessions/${sessionId}/alerts`),
}

// Mock Services
export const mockService = {
  // Payments
  getPayments: () => api.get('/api/payments'),
  createPayment: (data: any) => api.post('/api/payments', data),
  deletePayment: () => api.delete('/api/payments/delete'),
  
  // Users
  getProfile: (sessionId?: string) => api.get('/api/users/profile', {
    headers: sessionId ? { 'X-Session-ID': sessionId } : {}
  }),
  getUsers: () => api.get('/api/users'),
  createUser: (data: any) => api.post('/api/users', data),
  
  // Admin
  getDashboard: () => api.get('/api/admin/dashboard'),
  getConfig: () => api.get('/api/admin/config'),
}

// Event Store
export const eventStoreService = {
  writeEvent: (event: any) => api.post('/api/events', event),
  getEvents: (sessionId: string) => api.get(`/events/sessions/${sessionId}`),
}
```

### 2. Update `.env` with API base URLs:

```env
VITE_FORENSICS_API_URL=http://localhost:8083
VITE_MOCK_API_URL=http://localhost:9001
VITE_EVENT_STORE_URL=http://localhost:8081
```

### 3. Use in React components:

```typescript
import { forensicService, mockService } from './services/api'

const Dashboard = () => {
  const [sessions, setSessions] = useState([])
  
  useEffect(() => {
    forensicService.listSessions()
      .then(res => setSessions(res.data))
      .catch(err => console.error(err))
  }, [])
  
  return (
    <div>
      {sessions.map(session => (
        <div key={session.sessionId}>{session.sessionId}</div>
      ))}
    </div>
  )
}
```

---

## 🧪 Testing Endpoints

### cURL Examples

```bash
# Get all sessions
curl -u username:password http://localhost:8083/forensics/query/sessions

# Get session timeline
curl -u username:password http://localhost:8083/forensics/query/sessions/{sessionId}/report

# Run what-if simulation
curl -X POST -u username:password \
  -H "Content-Type: application/json" \
  -d '{"rules":["BLOCK_ATTACK","BLOCK_DELETE"]}' \
  http://localhost:8083/forensics/sessions/{sessionId}/whatif

# Get payments
curl http://localhost:9001/api/payments

# Get user profile
curl -H "X-Session-ID: session123" http://localhost:9002/api/users/profile
```

---

## 📝 Notes

- All UUIDs should be valid UUID v4 format
- Timestamps are in nanoseconds since Unix epoch
- Risk scores range from 0.0 to 1.0
- Event types: LOGIN, LOGOUT, ACCESS, ATTACK
- Decisions: ALLOW, BLOCK, REVIEW
- Severity levels: LOW, MEDIUM, HIGH, CRITICAL
- Hash verification uses canonical SHA-256 format (64 hex characters)
