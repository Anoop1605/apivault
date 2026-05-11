# 📊 Complete API Endpoints Summary - API Vault Frontend

## 🎯 Overview

Your frontend now has **23 fully-typed API endpoints** ready to use across 6 microservices:

---

## 📈 Endpoint Breakdown by Service

### 1️⃣ Forensics Replay Service (Port 8083) - **8 Endpoints**

**Replay Endpoints:**
- `GET /forensics/sessions/{sessionId}/timeline` 
  - Reconstructs exact decision sequence with frozen policy
  
- `POST /forensics/sessions/{sessionId}/whatif`
  - Re-evaluates events against alternate policy rules

**Query Endpoints:**
- `GET /forensics/query/sessions`
  - Returns all session summaries
  
- `GET /forensics/query/sessions/{sessionId}/report`
  - Full replay report with per-step decisions
  
- `GET /forensics/query/sessions/{sessionId}/verify`
  - Verify session hash (tamper check)
  
- `GET /forensics/query/sessions/{sessionId}/verify-hashes`
  - Verify individual event hashes

**Dashboard Endpoints:**
- `GET /forensics/dashboard/sessions/{sessionId}/timeline`
  - Chronological event timeline
  
- `GET /forensics/dashboard/sessions/{sessionId}/policy-trace`
  - Policy evaluation trace
  
- `GET /forensics/dashboard/sessions/{sessionId}/alerts`
  - Session alerts (BLOCK/REVIEW events)

---

### 2️⃣ Event Store Service (Port 8081) - **2 Endpoints**

- `POST /api/events`
  - Write event to tamper-proof store
  
- `GET /events/sessions/{sessionId}`
  - Get all events for a session (ordered by timestamp)

---

### 3️⃣ Payment Service (Port 9001) - **3 Endpoints**

- `GET /api/payments`
  - List all payments
  
- `POST /api/payments`
  - Create new payment
  
- `DELETE /api/payments/delete`
  - Delete payment

---

### 4️⃣ User Service (Port 9002) - **3 Endpoints**

- `GET /api/users/profile`
  - Get current user profile
  
- `GET /api/users`
  - List all users
  
- `POST /api/users`
  - Create new user

---

### 5️⃣ Admin Service (Port 9003) - **2 Endpoints**

- `GET /api/admin/dashboard`
  - System dashboard metrics
  
- `GET /api/admin/config`
  - System configuration

---

### 6️⃣ Forensic Dashboard (Port 8084) - **HTML Views**

- `GET /dashboard` - Session explorer
- `GET /dashboard/sessions/{sessionId}/timeline` - Timeline view
- `GET /dashboard/sessions/{sessionId}/policy-trace` - Policy trace
- `GET /dashboard/alerts` - Alerts list

---

## 🔌 How to Use Each Service

### Using Forensics Service

```typescript
import { forensicService } from './services/api'

// Get all sessions
const { data: sessions } = await forensicService.listSessions()
// Returns: SessionSummaryDTO[]

// Get session details
const { data: report } = await forensicService.getReplayReport(sessionId)
// Returns: ReplayReport

// Run what-if simulation
const { data: simulation } = await forensicService.runWhatIf(sessionId, {
  rules: ['BLOCK_ATTACK', 'ALLOW_GET']
})
// Returns: ReplayReport with simulated decisions

// Get timeline
const { data: timeline } = await forensicService.getTimeline(sessionId)
// Returns: TimelineEventDTO[]

// Get alerts
const { data: alerts } = await forensicService.getAlerts(sessionId)
// Returns: AlertDTO[]

// Verify hash
const { data: isValid } = await forensicService.verifySessionHash(sessionId, hash)
// Returns: boolean
```

### Using Event Store

```typescript
import { eventStoreService } from './services/api'

// Write event
const { data: savedEvent } = await eventStoreService.writeEvent({
  eventId: 'uuid',
  sessionId: 'uuid',
  timestampNs: Date.now() * 1000000,
  eventType: 'ACCESS',
  endpoint: '/api/payments/123',
  httpMethod: 'GET',
  decision: 'ALLOW',
  riskScore: 0.2,
  gatewayVersion: '1.0.0'
})

// Get session events
const { data: events } = await eventStoreService.getEventsBySession(sessionId)
// Returns: EventDTO[]
```

### Using Payment Service

```typescript
import { paymentService } from './services/api'

// Get payments
const { data: payments } = await paymentService.getPayments()

// Create payment
const { data: newPayment } = await paymentService.createPayment({
  amount: 100.00,
  currency: 'USD'
})

// Delete payment
const { data: result } = await paymentService.deletePayment()
```

### Using User Service

```typescript
import { userService } from './services/api'

// Get profile
const { data: profile } = await userService.getProfile()
// Returns: UserProfile

// Get all users
const { data: users } = await userService.getUsers()
// Returns: User[]

// Create user
const { data: newUser } = await userService.createUser({
  name: 'John Doe',
  email: 'john@example.com'
})
```

### Using Admin Service

```typescript
import { adminService } from './services/api'

// Get dashboard
const { data: dashboard } = await adminService.getDashboard()
// Returns: AdminDashboard

// Get config
const { data: config } = await adminService.getConfig()
// Returns: SystemConfig
```

---

## 📦 Response Data Models

### SessionSummaryDTO
```json
{
  "sessionId": "550e8400-e29b-41d4-a716-446655440000",
  "startTimestampNs": 1707825800300000000,
  "endTimestampNs": 1707825900300000000,
  "eventCount": 6,
  "hash": "a3f1c2d4e5b67890abcdef1234567890abcdef1234567890abcdef1234567890"
}
```

### ReplayReport
```json
{
  "sessionId": "550e8400-e29b-41d4-a716-446655440000",
  "originalDecisions": ["ALLOW", "ALLOW", "BLOCK"],
  "simulatedDecisions": ["ALLOW", "ALLOW", "BLOCK"],
  "firstDivergenceStep": -1,
  "hash": "...",
  "steps": [
    {
      "event": { ...EventDTO },
      "originalDecision": "ALLOW",
      "simulatedDecision": "ALLOW",
      "ruleMatched": "ALLOW_GET",
      "diverged": false
    }
  ]
}
```

### AlertDTO
```json
{
  "sessionId": "550e8400-e29b-41d4-a716-446655440000",
  "timestampNs": 1707825800300000000,
  "triggerEventType": "ATTACK",
  "severity": "HIGH",
  "message": "Event blocked by policy: BLOCK_DELETE"
}
```

### TimelineEventDTO
```json
{
  "sessionId": "550e8400-e29b-41d4-a716-446655440000",
  "timestampNs": 1707825800300000000,
  "eventType": "ACCESS",
  "decision": "BLOCK",
  "ruleMatched": "BLOCK_DELETE"
}
```

### EventDTO
```json
{
  "eventId": "550e8400-e29b-41d4-a716-446655440000",
  "sessionId": "550e8400-e29b-41d4-a716-446655440000",
  "timestampNs": 1707825800300000000,
  "eventType": "ACCESS",
  "userId": "user-001",
  "endpoint": "/api/payments/txn-9921",
  "httpMethod": "GET",
  "decision": "ALLOW",
  "policyRuleId": "ALLOW_GET",
  "riskScore": 0.2,
  "gatewayVersion": "1.0.0"
}
```

### UserProfile
```json
{
  "userId": "user-9921",
  "name": "Priya Sharma",
  "email": "priya@sentinel.dev",
  "role": "finance-admin",
  "department": "Finance",
  "lastLogin": "2024-02-13T10:30:00Z"
}
```

### AdminDashboard
```json
{
  "totalUsers": 156,
  "activeServices": 3,
  "systemStatus": "HEALTHY",
  "uptime": "72h 15m",
  "lastUpdated": "2024-02-13T10:30:00Z"
}
```

---

## 🔑 Key Features

✅ **23 Fully Typed Endpoints** - Complete TypeScript support
✅ **Automatic Error Handling** - HTTP 401, 502 errors handled
✅ **Environment Configuration** - URLs from `.env` file
✅ **Type Safety** - All responses have types
✅ **Helper Functions** - Timestamp conversion, validation, formatting
✅ **Constants & Labels** - Ready-to-use UI labels and mappings
✅ **Session Management** - SessionId type branding
✅ **Hash Validation** - SHA-256 format checking
✅ **Color Mapping** - Severity and decision colors for UI
✅ **Icon Mapping** - Event type emoji icons

---

## 🎨 UI Helper Functions

```typescript
import {
  nsToDate,
  formatTimestamp,
  getSeverityColor,
  getDecisionColor,
  getEventIcon,
  isValidUUID,
  isValidSHA256
} from './types/api.types'

// Format timestamp for display
const displayTime = formatTimestamp(eventDto.timestampNs)

// Get colors for styling
const alertColor = getSeverityColor('HIGH')        // "#dc3545"
const decisionBg = getDecisionColor('BLOCK')       // "#dc3545"

// Get icons for event type
const icon = getEventIcon('ATTACK')                // "⚠️"

// Validate data
const valid = isValidUUID(sessionId)
const validHash = isValidSHA256(hash)
```

---

## 📋 Service Ports Summary

| Service | Port | Base URL |
|---------|------|----------|
| Forensics Replay | 8083 | http://localhost:8083 |
| Event Store | 8081 | http://localhost:8081 |
| Payments | 9001 | http://localhost:9001 |
| Users | 9002 | http://localhost:9002 |
| Admin | 9003 | http://localhost:9003 |
| Forensic Dashboard | 8084 | http://localhost:8084 |
| Frontend Dev | 3000 | http://localhost:3000 |

---

## 🚀 Getting Started

1. **Install & Start:**
   ```bash
   cd frontend
   npm install
   npm run dev
   ```

2. **Import & Use:**
   ```typescript
   import { forensicService } from './services/api'
   const sessions = await forensicService.listSessions()
   ```

3. **Build Pages:**
   - Create new file in `src/pages/`
   - Use API services
   - Build your UI

4. **Deploy:**
   ```bash
   npm run build
   ```

---

## 📚 Documentation Files

| File | Purpose |
|------|---------|
| `SETUP_COMPLETE.md` | Setup overview (👈 Start here) |
| `QUICK_START.md` | Getting started guide |
| `API_ENDPOINTS.md` | Complete API reference (400+ lines) |
| `ENDPOINTS_QUICK_REF.md` | Quick reference card |

---

## ✨ You're Ready!

All **23 endpoints** are integrated, typed, and documented.

Start building! 🎉
