/**
 * TypeScript Type Definitions for API Vault Microservices
 * Auto-generated for Frontend Integration
 */

// ────────────────────────────────────────────────────────────
// EVENT TYPES
// ────────────────────────────────────────────────────────────

export type EventType = 'LOGIN' | 'LOGOUT' | 'ACCESS' | 'ATTACK'
export type Decision = 'ALLOW' | 'BLOCK' | 'REVIEW'
export type HttpMethod = 'GET' | 'POST' | 'PUT' | 'DELETE' | 'PATCH'
export type Severity = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL'

// ────────────────────────────────────────────────────────────
// EVENT STORE TYPES
// ────────────────────────────────────────────────────────────

export interface EventDTO {
  eventId: string
  sessionId: string
  timestampNs: number
  eventType: EventType
  userId?: string
  endpoint: string
  httpMethod: HttpMethod
  decision?: Decision
  policyRuleId?: string
  policyRuleVersion?: number
  policyRuleSnapshotId?: string
  riskScore: number
  bodyHash?: string
  gatewayVersion: string
}

// ────────────────────────────────────────────────────────────
// FORENSICS REPLAY TYPES
// ────────────────────────────────────────────────────────────

export interface StepDecision {
  event: EventDTO
  originalDecision: Decision
  simulatedDecision: Decision
  ruleMatched?: string
  diverged: boolean
}

export interface ReplayReport {
  sessionId: string
  steps: StepDecision[]
  originalDecisions: Decision[]
  simulatedDecisions: Decision[]
  firstDivergenceStep: number
  snapshotIdUsed?: string
  hash: string
}

export interface PolicySnapshot {
  rules: string[]
}

// ────────────────────────────────────────────────────────────
// FORENSICS QUERY TYPES
// ────────────────────────────────────────────────────────────

export interface SessionSummaryDTO {
  sessionId: string
  startTimestampNs: number
  endTimestampNs: number
  eventCount: number
  hash: string
}

export interface VerificationResult {
  eventId: string
  tampered: boolean
  expectedHash: string
  actualHash: string
}

// ────────────────────────────────────────────────────────────
// DASHBOARD TYPES
// ────────────────────────────────────────────────────────────

export interface TimelineEventDTO {
  sessionId: string
  timestampNs: number
  eventType: EventType
  decision: Decision
  ruleMatched?: string
}

export interface PolicyTraceDTO {
  sessionId: string
  rulesEvaluated: string[]
  rulesMatched: string[]
  finalDecision: Decision
  hash: string
}

export interface AlertDTO {
  sessionId: string
  timestampNs: number
  triggerEventType: EventType
  severity: Severity
  message: string
}

// ────────────────────────────────────────────────────────────
// PAYMENT SERVICE TYPES
// ────────────────────────────────────────────────────────────

export interface Payment {
  id: string
  amount: number
  currency: string
  status: 'PENDING' | 'COMPLETED' | 'FAILED' | 'CREATED'
  timestamp: string
}

// ────────────────────────────────────────────────────────────
// USER SERVICE TYPES
// ────────────────────────────────────────────────────────────

export interface UserProfile {
  userId: string
  name: string
  email: string
  role: string
  department: string
  lastLogin: string
}

export interface User {
  userId: string
  name: string
  role: string
}

// ────────────────────────────────────────────────────────────
// ADMIN SERVICE TYPES
// ────────────────────────────────────────────────────────────

export interface AdminDashboard {
  totalUsers: number
  activeServices: number
  systemStatus: 'HEALTHY' | 'DEGRADED' | 'DOWN'
  uptime: string
  lastUpdated: string
}

export interface SystemConfig {
  gatewayVersion: string
  policyEngineActive: boolean
  riskScorerEnabled: boolean
  eventStoreStatus: 'CONNECTED' | 'DISCONNECTED'
  maxConcurrentRequests: number
}

// ────────────────────────────────────────────────────────────
// API REQUEST/RESPONSE TYPES
// ────────────────────────────────────────────────────────────

export interface ApiResponse<T> {
  data: T
  status: number
  message?: string
}

export interface ApiError {
  status: number
  message: string
  details?: Record<string, unknown>
}

export interface PaginatedResponse<T> {
  items: T[]
  total: number
  page: number
  pageSize: number
}

// ────────────────────────────────────────────────────────────
// REPLAY SERVICE TYPES
// ────────────────────────────────────────────────────────────

export interface ReplayStep {
  step: number
  event_type: string
  endpoint?: string
  decision: string
  risk_score: number
  rule_id?: string
  timestamp: string
  method?: string
  user_id?: string
  ip_address?: string
  conditions?: {
    passed: string[]
    failed: string[]
  }
}

export interface ReplayResponse {
  steps: ReplayStep[]
  summary: {
    first_denial_step?: number
    total_steps: number
    final_risk_score: number
    session_duration: number
  }
}

export interface ReplayRequest {
  session_id: string
}

// ────────────────────────────────────────────────────────────
// UTILITY TYPES
// ────────────────────────────────────────────────────────────

export type AsyncState<T> = {
  status: 'idle' | 'loading' | 'success' | 'error'
  data?: T
  error?: Error
}

export type SessionId = string & { readonly __brand: 'SessionId' }
export type EventId = string & { readonly __brand: 'EventId' }
export type UserId = string & { readonly __brand: 'UserId' }

// ────────────────────────────────────────────────────────────
// HELPER FUNCTIONS
// ────────────────────────────────────────────────────────────

/**
 * Convert nanoseconds to milliseconds (common conversion)
 */
export function nsToMs(ns: number): number {
  return Math.floor(ns / 1_000_000)
}

/**
 * Convert nanoseconds to Date object
 */
export function nsToDate(ns: number): Date {
  return new Date(nsToMs(ns))
}

/**
 * Format timestamp for display
 */
export function formatTimestamp(ns: number, locale = 'en-US'): string {
  return nsToDate(ns).toLocaleString(locale)
}

/**
 * Check if hash is valid SHA-256 format
 */
export function isValidSHA256(hash: string): boolean {
  return /^[a-f0-9]{64}$/i.test(hash)
}

/**
 * Check if string is valid UUID v4
 */
export function isValidUUID(uuid: string): boolean {
  return /^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i.test(uuid)
}

/**
 * Get severity color for styling
 */
export function getSeverityColor(severity: Severity): string {
  const colors: Record<Severity, string> = {
    LOW: '#28a745',
    MEDIUM: '#ffc107',
    HIGH: '#fd7e14',
    CRITICAL: '#dc3545',
  }
  return colors[severity]
}

/**
 * Get decision color for styling
 */
export function getDecisionColor(decision: Decision): string {
  const colors: Record<Decision, string> = {
    ALLOW: '#28a745',
    BLOCK: '#dc3545',
    REVIEW: '#ffc107',
  }
  return colors[decision]
}

/**
 * Get event type icon
 */
export function getEventIcon(eventType: EventType): string {
  const icons: Record<EventType, string> = {
    LOGIN: '🔓',
    LOGOUT: '🔒',
    ACCESS: '📂',
    ATTACK: '⚠️',
  }
  return icons[eventType]
}

/**
 * Brand functions for type safety
 */
export const brandSessionId = (id: string): SessionId => id as SessionId
export const brandEventId = (id: string): EventId => id as EventId
export const brandUserId = (id: string): UserId => id as UserId

// ────────────────────────────────────────────────────────────
// CONSTANTS
// ────────────────────────────────────────────────────────────

export const EVENT_TYPES: EventType[] = ['LOGIN', 'LOGOUT', 'ACCESS', 'ATTACK']
export const DECISIONS: Decision[] = ['ALLOW', 'BLOCK', 'REVIEW']
export const HTTP_METHODS: HttpMethod[] = ['GET', 'POST', 'PUT', 'DELETE', 'PATCH']
export const SEVERITIES: Severity[] = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL']

export const SEVERITY_LABELS: Record<Severity, string> = {
  LOW: 'Low Severity',
  MEDIUM: 'Medium Severity',
  HIGH: 'High Severity',
  CRITICAL: 'Critical Severity',
}

export const DECISION_LABELS: Record<Decision, string> = {
  ALLOW: 'Allowed',
  BLOCK: 'Blocked',
  REVIEW: 'Needs Review',
}

export const EVENT_TYPE_LABELS: Record<EventType, string> = {
  LOGIN: 'Login',
  LOGOUT: 'Logout',
  ACCESS: 'Access',
  ATTACK: 'Attack',
}
