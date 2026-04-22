# Frontend Quick Start Guide

## ✅ Setup Steps

### 1. Install Dependencies
```bash
cd frontend
npm install
```

### 2. Configure Environment
```bash
# Copy environment template
cp .env.example .env

# Update URLs if needed (defaults work for local development)
```

### 3. Start Development Server
```bash
npm run dev
```
App runs at: `http://localhost:3000`

---

## 🔌 API Integration

### All Endpoints Available
The frontend has pre-configured API services for all backend microservices:

#### **Forensics Service** (Port 8083)
```typescript
import { forensicService } from './services/api'

// Get all sessions
await forensicService.listSessions()

// Get replay report
await forensicService.getReplayReport(sessionId)

// Run what-if simulation
await forensicService.runWhatIf(sessionId, { rules: ['BLOCK_ATTACK', 'ALLOW_GET'] })

// Get timeline
await forensicService.getTimeline(sessionId)

// Get policy trace
await forensicService.getPolicyTrace(sessionId)

// Get alerts
await forensicService.getAlerts(sessionId)

// Verify hash
await forensicService.verifySessionHash(sessionId, hash)
```

#### **Payment Service** (Port 9001)
```typescript
import { paymentService } from './services/api'

await paymentService.getPayments()
await paymentService.createPayment({ amount: 100 })
await paymentService.deletePayment()
```

#### **User Service** (Port 9002)
```typescript
import { userService } from './services/api'

await userService.getProfile()
await userService.getUsers()
await userService.createUser({ name: 'John', email: 'john@example.com' })
```

#### **Admin Service** (Port 9003)
```typescript
import { adminService } from './services/api'

await adminService.getDashboard()
await adminService.getConfig()
```

#### **Event Store** (Port 8081)
```typescript
import { eventStoreService } from './services/api'

await eventStoreService.writeEvent(event)
await eventStoreService.getEventsBySession(sessionId)
```

---

## 📁 Project Structure

```
frontend/
├── src/
│   ├── components/        # Reusable React components
│   ├── pages/             # Page components
│   │   └── Dashboard.tsx  # Example dashboard page
│   ├── services/
│   │   └── api.ts         # ✨ ALL API ENDPOINTS (fully typed)
│   ├── App.tsx            # Main app
│   ├── main.tsx           # Entry point
│   ├── index.css          # Global styles
│   └── vite-env.d.ts      # TypeScript types
├── public/                # Static assets
├── package.json
├── tsconfig.json
├── vite.config.ts
├── .env.example           # Environment template
├── .gitignore
├── API_ENDPOINTS.md       # 📚 Complete API reference
├── QUICK_START.md         # This file
└── README.md
```

---

## 🎯 Example: Building a Session Explorer

```typescript
// src/pages/SessionExplorer.tsx
import { useEffect, useState } from 'react'
import { forensicService } from '../services/api'
import type { SessionSummaryDTO } from '../services/api'

const SessionExplorer = () => {
  const [sessions, setSessions] = useState<SessionSummaryDTO[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    const fetchSessions = async () => {
      try {
        const response = await forensicService.listSessions()
        setSessions(response.data)
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Failed to load sessions')
      } finally {
        setLoading(false)
      }
    }

    fetchSessions()
  }, [])

  if (loading) return <div>Loading sessions...</div>
  if (error) return <div>Error: {error}</div>

  return (
    <div>
      <h2>Sessions ({sessions.length})</h2>
      <table>
        <thead>
          <tr>
            <th>Session ID</th>
            <th>Events</th>
            <th>Start Time</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {sessions.map(session => (
            <tr key={session.sessionId}>
              <td>{session.sessionId}</td>
              <td>{session.eventCount}</td>
              <td>{new Date(session.startTimestampNs / 1000000).toLocaleString()}</td>
              <td>
                <button onClick={() => viewSession(session.sessionId)}>View</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}

const viewSession = (sessionId: string) => {
  window.location.href = `/session/${sessionId}`
}

export default SessionExplorer
```

---

## 🏗️ Build & Deploy

### Development Build
```bash
npm run dev
```

### Production Build
```bash
npm run build
```
Creates optimized bundle in `dist/` directory

### Preview Production Build
```bash
npm run preview
```

### Type Checking
```bash
npm run type-check
```

### Linting
```bash
npm run lint
```

---

## 📋 Service Configuration

All services are pre-configured in `src/services/api.ts` with:

✅ Full TypeScript types
✅ Automatic error handling
✅ Environment-based configuration
✅ Proper CORS headers
✅ Request/response interceptors
✅ UUID support

---

## 🔐 Authentication

For Forensics API endpoints that require Basic Auth:

```typescript
// Add auth header when needed
import axios from 'axios'

const forensicsApi = axios.create({
  baseURL: 'http://localhost:8083',
  auth: {
    username: 'your_username',
    password: 'your_password'
  }
})
```

---

## 🐛 Troubleshooting

### "Cannot reach API"
- Ensure backend services are running: `docker-compose up`
- Check service ports in docker-compose.yml
- Verify `.env` URLs are correct

### "CORS Error"
- Check vite.config.ts proxy settings
- Ensure backend services allow requests from localhost:3000

### "Type errors in api.ts"
- Run `npm run type-check` to verify
- Ensure TypeScript version matches package.json

---

## 📚 Complete API Reference

See **API_ENDPOINTS.md** for:
- All available endpoints
- Request/response examples
- Data model schemas
- Authentication details
- cURL examples

---

## 🚀 Next Steps

1. ✅ Install dependencies
2. ✅ Start backend services (docker-compose up)
3. ✅ Run dev server (npm run dev)
4. ✅ Create your first page in `src/pages/`
5. ✅ Import services from `src/services/api.ts`
6. ✅ Build UI components in `src/components/`

Happy coding! 🎉
