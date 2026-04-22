import React, { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { motion } from 'framer-motion'
import { SessionHeader } from '../components/SessionHeader'
import { Timeline } from '../components/Timeline'
import { StepDetails } from '../components/StepDetails'
import { ActionBar } from '../components/ActionBar'
import { TimelineStep } from '../components/StepCard'

// Mock comprehensive timeline data
const generateMockTimeline = (sessionId: string): TimelineStep[] => [
  {
    id: '1',
    eventType: 'REQUEST_RECEIVED',
    endpoint: '/api/sessions/start',
    method: 'POST',
    decision: 'ALLOW',
    riskScore: 0.15,
    timestamp: Date.now() - 30000,
    userName: 'user_john_smith',
    ipAddress: '192.168.1.10',
    requestBody: '{"userId":"user_123","sessionType":"web"}',
    headers: {
      'Authorization': 'Bearer eyJhbGc...',
      'Content-Type': 'application/json',
      'User-Agent': 'Mozilla/5.0...',
    },
    policyRuleId: 'SESSION-START-01',
    policyConditions: [
      { condition: 'role == authenticated', required: true, met: true },
      { condition: 'ip in allowed_ranges', required: true, met: true },
      { condition: 'session_count < max_limit', required: false, met: true },
    ],
  },
  {
    id: '2',
    eventType: 'POLICY_EVALUATED',
    endpoint: '/api/user/profile',
    method: 'GET',
    decision: 'ALLOW',
    riskScore: 0.25,
    timestamp: Date.now() - 25000,
    userName: 'user_john_smith',
    ipAddress: '192.168.1.10',
    policyRuleId: 'USER-READ-01',
    policyConditions: [
      { condition: 'method == GET', required: true, met: true },
      { condition: 'endpoint matches /api/user/*', required: true, met: true },
      { condition: 'user authenticated', required: true, met: true },
    ],
  },
  {
    id: '3',
    eventType: 'POLICY_ALLOWED',
    endpoint: '/api/payments/list',
    method: 'GET',
    decision: 'ALLOW',
    riskScore: 0.35,
    timestamp: Date.now() - 20000,
    userName: 'user_john_smith',
    ipAddress: '192.168.1.10',
    policyRuleId: 'PAYMENTS-READ-01',
    policyConditions: [
      { condition: 'role contains payment_viewer', required: true, met: true },
      { condition: 'endpoint == /api/payments/list', required: true, met: true },
      { condition: 'request_rate < threshold', required: true, met: true },
    ],
  },
  {
    id: '4',
    eventType: 'POLICY_ALLOWED',
    endpoint: '/api/payments/export',
    method: 'POST',
    decision: 'ALLOW',
    riskScore: 0.52,
    timestamp: Date.now() - 15000,
    userName: 'user_john_smith',
    ipAddress: '192.168.1.10',
    requestBody: '{"format":"csv","startDate":"2026-04-01","endDate":"2026-04-22"}',
    policyRuleId: 'PAYMENTS-EXPORT-02',
    policyConditions: [
      { condition: 'role == finance-admin', required: true, met: true },
      { condition: 'endpoint == /api/payments/export', required: true, met: true },
      { condition: 'time_of_day in business_hours', required: false, met: true },
    ],
  },
  {
    id: '5',
    eventType: 'RISK_FLAGGED',
    endpoint: '/api/payments/delete',
    method: 'DELETE',
    decision: 'FLAG',
    riskScore: 0.75,
    timestamp: Date.now() - 8000,
    userName: 'user_john_smith',
    ipAddress: '192.168.1.10',
    requestBody: '{"paymentIds":["pay_001","pay_002","pay_003","pay_004","pay_005"]}',
    policyRuleId: 'PAYMENTS-DELETE-CRITICAL-01',
    policyConditions: [
      { condition: 'role == finance-admin', required: true, met: true },
      { condition: 'endpoint == /api/payments/delete', required: true, met: true },
      { condition: 'time_of_day in business_hours', required: false, met: false },
      { condition: 'previous_delete_count < 5', required: true, met: false },
      { condition: 'risk_score < 0.7', required: true, met: false },
    ],
    failedConditions: [
      'Deleted 5+ payment records (high volume)',
      'Request time outside business hours (00:34 UTC)',
      'Risk score exceeded threshold',
    ],
  },
  {
    id: '6',
    eventType: 'POLICY_DENIED',
    endpoint: '/api/payments/delete',
    method: 'DELETE',
    decision: 'DENY',
    riskScore: 0.88,
    timestamp: Date.now() - 2000,
    userName: 'user_john_smith',
    ipAddress: '192.168.1.10',
    requestBody: '{"paymentIds":["pay_001","pay_002","pay_003","pay_004","pay_005"]}',
    policyRuleId: 'FIN-DELETE-01',
    policyConditions: [
      { condition: 'role == finance-admin', required: true, met: true },
      { condition: 'endpoint == /api/payments/delete', required: true, met: true },
      { condition: 'time_of_day in business_hours', required: true, met: false },
      { condition: 'risk_score < 0.5', required: true, met: false },
      { condition: 'multi_factor_auth verified', required: true, met: false },
    ],
    failedConditions: [
      'Multi-factor authentication not verified',
      'Risk score 0.88 exceeds maximum allowed 0.50',
      'Operation outside business hours (policy requires 9 AM - 5 PM UTC)',
    ],
  },
]

export const SessionDetail: React.FC = () => {
  const { sessionId } = useParams<{ sessionId: string }>()
  const navigate = useNavigate()
  const [selectedStep, setSelectedStep] = useState<TimelineStep | null>(null)
  const [timeline, setTimeline] = useState<TimelineStep[]>([])
  const [loading, setLoading] = useState(true)

  // Mock session data
  const mockSessionData = {
    sessionId: sessionId || 'sess_abc123def456',
    userId: 'user_john_smith',
    status: 'compromised',
    riskLevel: 0.92,
    eventCount: 6,
    duration: 30000,
  }

  useEffect(() => {
    // Simulate API fetch delay
    const timer = setTimeout(() => {
      setTimeline(generateMockTimeline(mockSessionData.sessionId))
      setLoading(false)
    }, 600)

    return () => clearTimeout(timer)
  }, [sessionId])

  useEffect(() => {
    // Auto-select the first step
    if (timeline.length > 0 && !selectedStep) {
      setSelectedStep(timeline[0])
    }
  }, [timeline, selectedStep])

  const handleReplay = () => {
    navigate(`/replay/${mockSessionData.sessionId}`)
  }

  const handleSimulate = () => {
    navigate(`/simulation/${mockSessionData.sessionId}`)
  }

  if (loading) {
    return (
      <div className="h-screen bg-slate-950 flex items-center justify-center">
        <motion.div
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          className="text-center"
        >
          <motion.div
            animate={{ rotate: 360 }}
            transition={{ duration: 2, repeat: Infinity }}
            className="w-16 h-16 rounded-full border-4 border-slate-700 border-t-cyan-400 mx-auto mb-4"
          />
          <p className="text-slate-400">Loading session details...</p>
        </motion.div>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-slate-950 flex flex-col">
      {/* Header */}
      <SessionHeader
        sessionId={mockSessionData.sessionId}
        userId={mockSessionData.userId}
        maxRisk={mockSessionData.riskLevel}
        status={mockSessionData.status}
        eventCount={mockSessionData.eventCount}
        duration={mockSessionData.duration}
      />

      {/* Main Content - Two Column Layout */}
      <motion.div
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        transition={{ delay: 0.2 }}
        className="flex-1 flex overflow-hidden gap-0 relative"
      >
        {/* Left: Timeline */}
        <div className="w-full lg:w-3/5 lg:border-r border-slate-700/50 flex flex-col overflow-hidden bg-slate-950/50 relative z-0">
          <div className="flex-1 overflow-y-auto">
            <div className="p-8">
              <Timeline
                steps={timeline}
                selectedStepId={selectedStep?.id || null}
                onSelectStep={setSelectedStep}
              />
            </div>
          </div>
        </div>

        {/* Right: Step Details */}
        <div className="hidden lg:flex lg:w-2/5 flex-col overflow-hidden bg-slate-900/40 border-l border-slate-700/50 relative z-10">
          <div className="flex-1 overflow-y-auto">
            <div className="p-8">
              <StepDetails step={selectedStep} />
            </div>
          </div>
        </div>
      </motion.div>

      {/* Mobile: Step Details Below Timeline */}
      {selectedStep && (
        <motion.div
          initial={{ opacity: 0, y: 10 }}
          animate={{ opacity: 1, y: 0 }}
          className="lg:hidden bg-slate-900/60 border-t-2 border-slate-700/50 p-6 max-h-96 overflow-y-auto"
        >
          <h3 className="text-lg font-bold text-white mb-4 pb-3 border-b border-slate-700/30">Step Details</h3>
          <StepDetails step={selectedStep} />
        </motion.div>
      )}

      {/* Action Bar */}
      <ActionBar
        sessionId={mockSessionData.sessionId}
        onReplay={handleReplay}
        onSimulate={handleSimulate}
      />
    </div>
  )
}
