# 🔌 API Endpoints Quick Reference

## Service URLs & Ports

| Service | Port | Base URL |
|---------|------|----------|
| **Forensics Replay** | 8083 | `http://localhost:8083` |
| **Event Store** | 8081 | `http://localhost:8081` |
| **Payment Mock** | 9001 | `http://localhost:9001` |
| **User Mock** | 9002 | `http://localhost:9002` |
| **Admin Mock** | 9003 | `http://localhost:9003` |
| **Forensic Dashboard** | 8084 | `http://localhost:8084` |
| **Frontend (Dev)** | 3000 | `http://localhost:3000` |

---

## 🔍 Forensics API (Port 8083)

### REPLAY
| Method | Endpoint | Purpose |
|--------|----------|---------|
| `GET` | `/forensics/sessions/{sessionId}/timeline` | Get session replay with frozen policy |
| `POST` | `/forensics/sessions/{sessionId}/whatif` | Run what-if simulation with alternate rules |

### QUERY
| Method | Endpoint | Purpose |
|--------|----------|---------|
| `GET` | `/forensics/query/sessions` | List all sessions |
| `GET` | `/forensics/query/sessions/{sessionId}/report` | Get full replay report |
| `GET` | `/forensics/query/sessions/{sessionId}/verify` | Verify session hash (tamper check) |
| `GET` | `/forensics/query/sessions/{sessionId}/verify-hashes` | Verify all event hashes |

### DASHBOARD
| Method | Endpoint | Purpose |
|--------|----------|---------|
| `GET` | `/forensics/dashboard/sessions/{sessionId}/timeline` | Chronological event timeline |
| `GET` | `/forensics/dashboard/sessions/{sessionId}/policy-trace` | Policy evaluation trace |
| `GET` | `/forensics/dashboard/sessions/{sessionId}/alerts` | Session alerts (BLOCK/REVIEW events) |

---

## 📦 Event Store API (Port 8081)

| Method | Endpoint | Purpose |
|--------|----------|---------|
| `POST` | `/api/events` | Write event to store |
| `GET` | `/events/sessions/{sessionId}` | Get all events for session |

---

## 💰 Payment Service (Port 9001)

| Method | Endpoint | Purpose |
|--------|----------|---------|
| `GET` | `/api/payments` | List all payments |
| `POST` | `/api/payments` | Create payment |
| `DELETE` | `/api/payments/delete` | Delete payment |

---

## 👥 User Service (Port 9002)

| Method | Endpoint | Purpose |
|--------|----------|---------|
| `GET` | `/api/users/profile` | Get user profile |
| `GET` | `/api/users` | List all users |
| `POST` | `/api/users` | Create user |

---

## ⚙️ Admin Service (Port 9003)

| Method | Endpoint | Purpose |
|--------|----------|---------|
| `GET` | `/api/admin/dashboard` | Get dashboard metrics |
| `GET` | `/api/admin/config` | Get system config |

---

## 🎯 React Usage Examples

### Get All Sessions
```typescript
import { forensicService } from './services/api'

const sessions = await forensicService.listSessions()
// Returns: SessionSummaryDTO[]
```

### Get Session Details
```typescript
const report = await forensicService.getReplayReport(sessionId)
// Returns: ReplayReport { sessionId, steps, decisions, hash, ... }
```

### Run What-If Simulation
```typescript
const result = await forensicService.runWhatIf(sessionId, {
  rules: ['BLOCK_ATTACK', 'BLOCK_DELETE', 'ALLOW_GET']
})
// Returns: ReplayReport with simulated vs original decisions
```

### Get Timeline Events
```typescript
const timeline = await forensicService.getTimeline(sessionId)
// Returns: TimelineEventDTO[] - chronological events
```

### Get Alerts
```typescript
const alerts = await forensicService.getAlerts(sessionId)
// Returns: AlertDTO[] - HIGH severity for BLOCK, MEDIUM for REVIEW
```

### Verify Hash
```typescript
const isValid = await forensicService.verifySessionHash(sessionId, hash)
// Returns: boolean - true if untampered
```

### Get Payment History
```typescript
import { paymentService } from './services/api'

const payments = await paymentService.getPayments()
// Returns: List of payment objects
```

### Get User Profile
```typescript
import { userService } from './services/api'

const profile = await userService.getProfile()
// Returns: User profile object
```

### Admin Dashboard
```typescript
import { adminService } from './services/api'

const dashboard = await adminService.getDashboard()
// Returns: { totalUsers, activeServices, systemStatus, uptime, ... }
```

---

## 🧪 cURL Examples

### Get Sessions
```bash
curl -u user:pass http://localhost:8083/forensics/query/sessions
```

### Get Replay Report
```bash
curl -u user:pass http://localhost:8083/forensics/query/sessions/{sessionId}/report
```

### Run What-If
```bash
curl -X POST -u user:pass \
  -H "Content-Type: application/json" \
  -d '{"rules":["BLOCK_ATTACK"]}' \
  http://localhost:8083/forensics/sessions/{sessionId}/whatif
```

### Get Payments
```bash
curl http://localhost:9001/api/payments
```

### Get Admin Dashboard
```bash
curl http://localhost:9003/api/admin/dashboard
```

---

## 🔑 Auth & Headers

| Header | Value | Required |
|--------|-------|----------|
| `Content-Type` | `application/json` | All POST/PUT |
| `X-Session-ID` | Session UUID | User Service (optional) |
| `Authorization` | `Basic base64(user:pass)` | Forensics API (port 8083) |

---

## 📊 Common Response Types

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

### ReplayReport
```json
{
  "sessionId": "uuid",
  "originalDecisions": ["ALLOW", "BLOCK"],
  "simulatedDecisions": ["ALLOW", "BLOCK"],
  "firstDivergenceStep": -1,
  "hash": "sha256...",
  "steps": [...]
}
```

### AlertDTO
```json
{
  "sessionId": "uuid",
  "timestampNs": 1707825800300000000,
  "triggerEventType": "ATTACK",
  "severity": "HIGH",
  "message": "Event blocked by policy: BLOCK_DELETE"
}
```

### TimelineEventDTO
```json
{
  "sessionId": "uuid",
  "timestampNs": 1707825800300000000,
  "eventType": "ACCESS",
  "decision": "BLOCK",
  "ruleMatched": "BLOCK_DELETE"
}
```

---

## ✅ Enums & Constants

### Event Types
- `LOGIN`
- `LOGOUT`
- `ACCESS`
- `ATTACK`

### Decisions
- `ALLOW`
- `BLOCK`
- `REVIEW`

### HTTP Methods
- `GET`
- `POST`
- `PUT`
- `DELETE`
- `PATCH`

### Severity Levels
- `LOW`
- `MEDIUM`
- `HIGH`
- `CRITICAL`

---

## 🔗 Related Files

- Full documentation: [API_ENDPOINTS.md](./API_ENDPOINTS.md)
- Quick start guide: [QUICK_START.md](./QUICK_START.md)
- API service code: [src/services/api.ts](./src/services/api.ts)
- Environment config: [.env.example](./.env.example)

---

## 💡 Tips

1. **All endpoints are typed** - Use TypeScript for auto-completion
2. **Error handling included** - API service handles 401, 502 errors
3. **Environment-based URLs** - Configure in `.env` file
4. **Session ID format** - Valid UUID v4 (e.g., `550e8400-e29b-41d4-a716-446655440000`)
5. **Timestamps in nanoseconds** - Divide by 1,000,000 to get milliseconds

---

Generated for API Vault Frontend v1.0.0
