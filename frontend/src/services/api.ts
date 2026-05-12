import axios from 'axios'

// Service URLs from environment or defaults
const FORENSICS_API_URL = import.meta.env.VITE_FORENSICS_API_URL || 'http://localhost:8083'
const MOCK_API_URL = import.meta.env.VITE_MOCK_API_URL || 'http://localhost:8080'
const EVENT_STORE_URL = import.meta.env.VITE_EVENT_STORE_URL || 'http://localhost:8081'

// Create axios instances for different services
const api = axios.create({
  headers: { 'Content-Type': 'application/json' },
})

const forensicsApi = axios.create({
  baseURL: FORENSICS_API_URL,
  headers: { 'Content-Type': 'application/json' },
})

const mockApi = axios.create({
  baseURL: MOCK_API_URL,
  headers: { 'Content-Type': 'application/json' },
})

const eventStoreApi = axios.create({
  baseURL: EVENT_STORE_URL,
  headers: { 'Content-Type': 'application/json' },
})

// ────────────────────────────────────────────────────────────
// TYPE DEFINITIONS
// ────────────────────────────────────────────────────────────

export interface EventDTO {
  eventId: string
  sessionId: string
  timestampNs: number
  eventType: 'LOGIN' | 'LOGOUT' | 'ACCESS' | 'ATTACK'
  userId?: string
  endpoint: string
  httpMethod: 'GET' | 'POST' | 'PUT' | 'DELETE' | 'PATCH'
  decision?: 'ALLOW' | 'BLOCK' | 'REVIEW'
  policyRuleId?: string
  policyRuleVersion?: number
  policyRuleSnapshotId?: string
  riskScore: number
  bodyHash?: string
  gatewayVersion: string
}

export interface StepDecision {
  event: EventDTO
  originalDecision: 'ALLOW' | 'BLOCK' | 'REVIEW'
  simulatedDecision: 'ALLOW' | 'BLOCK' | 'REVIEW'
  ruleMatched?: string
  diverged: boolean
}

export interface ReplayReport {
  sessionId: string
  steps: StepDecision[]
  originalDecisions: ('ALLOW' | 'BLOCK' | 'REVIEW')[]
  simulatedDecisions: ('ALLOW' | 'BLOCK' | 'REVIEW')[]
  firstDivergenceStep: number
  snapshotIdUsed?: string
  hash: string
}

export interface SessionSummaryDTO {
  sessionId: string
  startTimestampNs: number
  endTimestampNs: number
  eventCount: number
  hash: string
}

export interface TimelineEventDTO {
  sessionId: string
  timestampNs: number
  eventType: 'LOGIN' | 'LOGOUT' | 'ACCESS' | 'ATTACK'
  decision: 'ALLOW' | 'BLOCK' | 'REVIEW'
  ruleMatched?: string
}

export interface PolicyTraceDTO {
  sessionId: string
  rulesEvaluated: string[]
  rulesMatched: string[]
  finalDecision: 'ALLOW' | 'BLOCK' | 'REVIEW'
  hash: string
}

export interface AlertDTO {
  sessionId: string
  timestampNs: number
  triggerEventType: 'LOGIN' | 'LOGOUT' | 'ACCESS' | 'ATTACK'
  severity: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL'
  message: string
}

export interface VerificationResult {
  eventId: string
  tampered: boolean
  expectedHash: string
  actualHash: string
}

export interface PolicySnapshot {
  rules: string[]
}

// ────────────────────────────────────────────────────────────
// FORENSICS API SERVICES (Port 8083)
// ────────────────────────────────────────────────────────────

export const forensicService = {
  // ── REPLAY ENDPOINTS ──
  getSessionTimeline: (sessionId: string) =>
    forensicsApi.get<ReplayReport>(`/forensics/sessions/${sessionId}/timeline`),

  runWhatIf: (sessionId: string, policySnapshot: PolicySnapshot) =>
    forensicsApi.post<ReplayReport>(
      `/forensics/sessions/${sessionId}/whatif`,
      policySnapshot
    ),

  getReplay: (sessionId: string) =>
    forensicsApi.post<any>('/forensics/replay', { session_id: sessionId }),

  // ── FORENSIC QUERY ENDPOINTS ──
  listSessions: () =>
    forensicsApi.get<SessionSummaryDTO[]>('/forensics/query/sessions'),

  getReplayReport: (sessionId: string) =>
    forensicsApi.get<ReplayReport>(`/forensics/query/sessions/${sessionId}/report`),

  verifySessionHash: (sessionId: string, hash: string) =>
    forensicsApi.get<boolean>(`/forensics/query/sessions/${sessionId}/verify`, {
      params: { hash },
    }),

  verifyEventHashes: (sessionId: string) =>
    forensicsApi.get<VerificationResult[]>(
      `/forensics/query/sessions/${sessionId}/verify-hashes`
    ),

  // ── DASHBOARD ENDPOINTS ──
  getTimeline: (sessionId: string) =>
    forensicsApi.get<TimelineEventDTO[]>(
      `/forensics/dashboard/sessions/${sessionId}/timeline`
    ),

  getPolicyTrace: (sessionId: string) =>
    forensicsApi.get<PolicyTraceDTO>(
      `/forensics/dashboard/sessions/${sessionId}/policy-trace`
    ),

  getAlerts: (sessionId: string) =>
    forensicsApi.get<AlertDTO[]>(
      `/forensics/dashboard/sessions/${sessionId}/alerts`
    ),
}

// ────────────────────────────────────────────────────────────
// PAYMENT SERVICE (Port 9001)
// ────────────────────────────────────────────────────────────

export const paymentService = {
  getPayments: () =>
    mockApi.get('/api/payments'),

  createPayment: (data: Record<string, unknown>) =>
    mockApi.post('/api/payments', data),

  deletePayment: () =>
    mockApi.delete('/api/payments/delete'),
}

// ────────────────────────────────────────────────────────────
// USER SERVICE (Port 9002)
// ────────────────────────────────────────────────────────────

export const userService = {
  getProfile: (sessionId?: string) =>
    mockApi.get('/api/users/profile', {
      headers: sessionId ? { 'X-Session-ID': sessionId } : {},
    }),

  getUsers: () =>
    mockApi.get('/api/users'),

  createUser: (data: Record<string, unknown>) =>
    mockApi.post('/api/users', data),
}

// ────────────────────────────────────────────────────────────
// ADMIN SERVICE (Port 9003)
// ────────────────────────────────────────────────────────────

export const adminService = {
  getDashboard: () =>
    mockApi.get('/api/admin/dashboard'),

  getConfig: () =>
    mockApi.get('/api/admin/config'),
}

// ────────────────────────────────────────────────────────────
// EVENT STORE SERVICE (Port 8081)
// ────────────────────────────────────────────────────────────

export const eventStoreService = {
  writeEvent: (event: EventDTO) =>
    eventStoreApi.post<EventDTO>('/api/events', event),

  getEventsBySession: (sessionId: string) =>
    eventStoreApi.get<EventDTO[]>(`/events/sessions/${sessionId}`),
}

// ────────────────────────────────────────────────────────────
// MOCK API (unified from all mock services)
// ────────────────────────────────────────────────────────────

export const mockServiceAPI = {
  // Payments
  payments: paymentService,
  // Users
  users: userService,
  // Admin
  admin: adminService,
}

// ────────────────────────────────────────────────────────────
// CENTRALIZED API SERVICE
// ────────────────────────────────────────────────────────────

export const apiService = {
  forensics: forensicService,
  payments: paymentService,
  users: userService,
  admin: adminService,
  events: eventStoreService,
}

// ────────────────────────────────────────────────────────────
// ERROR HANDLER
// ────────────────────────────────────────────────────────────

api.interceptors.response.use(
  response => response,
  error => {
    if (error.response?.status === 401) {
      console.error('Unauthorized: Please log in')
    }
    if (error.response?.status === 502) {
      console.error('Bad Gateway: Backend service unavailable')
    }
    return Promise.reject(error)
  }
)

export default api
