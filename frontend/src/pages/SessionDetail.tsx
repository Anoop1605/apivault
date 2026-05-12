import React, { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { motion } from 'framer-motion'
import { SessionHeader } from '../components/SessionHeader'
import { Timeline } from '../components/Timeline'
import { StepDetails } from '../components/StepDetails'
import { ActionBar } from '../components/ActionBar'
import { TimelineStep } from '../components/StepCard'
import { apiService } from '../services/api'

export const SessionDetail: React.FC = () => {
  const { sessionId } = useParams<{ sessionId: string }>()
  const navigate = useNavigate()
  const [selectedStep, setSelectedStep] = useState<TimelineStep | null>(null)
  const [timeline, setTimeline] = useState<TimelineStep[]>([])
  const [loading, setLoading] = useState(true)
  const [sessionInfo, setSessionInfo] = useState<any>({
    sessionId: sessionId || '',
    userId: 'anonymous',
    status: 'analyzing',
    riskLevel: 0,
    eventCount: 0,
    duration: 0
  })

  useEffect(() => {
    const fetchSessionData = async () => {
      if (!sessionId) return
      setLoading(true)
      try {
        // Fetch real events for this session
        const { data: events } = await apiService.events.getEventsBySession(sessionId)
        
        // Map EventDTO to TimelineStep
        const steps: TimelineStep[] = events.map((event, idx) => ({
          id: event.eventId || String(idx),
          eventType: event.eventType,
          endpoint: event.endpoint,
          method: event.httpMethod,
          decision: event.decision || 'UNKNOWN',
          riskScore: event.riskScore || 0,
          timestamp: Math.floor(event.timestampNs / 1000000), // NS to MS
          userName: event.userId || 'anonymous',
          ipAddress: event.sourceIp || 'Intercepted via Gateway',
          requestBody: event.bodyHash
            ? `SHA-256: ${event.bodyHash}`
            : 'No body / Empty body',
          policyRuleId: event.policyRuleId || 'N/A',
          policyConditions: [
            {
              condition: `Rule [${event.policyRuleId || 'DEFAULT'}] v${event.policyRuleVersion ?? 1}`,
              required: true,
              met: event.decision === 'ALLOW'
            },
            {
              condition: `Decision: ${event.decision || 'UNKNOWN'}`,
              required: false,
              met: event.decision !== 'DENY'
            }
          ]
        }))

        setTimeline(steps)
        
        // Calculate session summary
        if (events.length > 0) {
          const maxRisk = Math.max(...events.map(e => e.riskScore || 0))
          setSessionInfo({
            sessionId,
            userId: events[0].userId || 'anonymous',
            status: maxRisk > 0.7 ? 'compromised' : 'safe',
            riskLevel: maxRisk,
            eventCount: events.length,
            duration: Math.floor((events[events.length-1].timestampNs - events[0].timestampNs) / 1000000)
          })
        }
      } catch (error) {
        console.error('Failed to fetch session timeline:', error)
      } finally {
        setLoading(false)
      }
    }

    fetchSessionData()
  }, [sessionId])

  useEffect(() => {
    // Auto-select the first step
    if (timeline.length > 0 && !selectedStep) {
      setSelectedStep(timeline[0])
    }
  }, [timeline, selectedStep])

  const handleReplay = () => {
    navigate(`/replay/${sessionInfo.sessionId}`)
  }

  const handleSimulate = () => {
    navigate(`/simulation/${sessionInfo.sessionId}`)
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
        sessionId={sessionInfo.sessionId}
        userId={sessionInfo.userId}
        maxRisk={sessionInfo.riskLevel}
        status={sessionInfo.status}
        eventCount={sessionInfo.eventCount}
        duration={sessionInfo.duration}
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
        sessionId={sessionInfo.sessionId}
        onReplay={handleReplay}
        onSimulate={handleSimulate}
      />
    </div>
  )
}
