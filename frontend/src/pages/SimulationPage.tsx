import React, { useState, useEffect, useRef } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { motion, AnimatePresence } from 'framer-motion'
import { ChevronLeft, Play, ShieldAlert, ShieldCheck, AlertTriangle, CheckCircle, XCircle } from 'lucide-react'
import gsap from 'gsap'
import { forensicService, apiService } from '../services/api'

// --- Data Types ---
type EventDecision = 'ALLOW' | 'BLOCK' | 'REVIEW' | 'FLAG' | 'DENY' | 'POLICY_NO_MATCH'

interface SessionEvent {
  id: string
  step: number
  eventType: string
  endpoint: string
  method: string
  originalDecision: EventDecision
  originalRisk: number
  query?: string
  ip?: string
}

interface SimulatedEvent extends SessionEvent {
  simulatedDecision: EventDecision
  simulatedRisk: number
  diverged: boolean
  divergenceReason?: string
}

export const SimulationPage: React.FC = () => {
  const { sessionId } = useParams<{ sessionId: string }>()
  const navigate = useNavigate()

  // --- Controls State ---
  const [blockSql, setBlockSql] = useState(true)
  const [rateLimiting, setRateLimiting] = useState(true)
  const [inputSanitization, setInputSanitization] = useState(false)
  const [riskThreshold, setRiskThreshold] = useState(80)
  const [customRule, setCustomRule] = useState('')
  const [originalEvents, setOriginalEvents] = useState<SessionEvent[]>([])

  // --- Simulation State ---
  const [isSimulating, setIsSimulating] = useState(false)
  const [hasSimulated, setHasSimulated] = useState(false)
  const [simulatedEvents, setSimulatedEvents] = useState<SimulatedEvent[]>([])
  const [displayedEvents, setDisplayedEvents] = useState<SimulatedEvent[]>([])
  
  // Results State
  const [originalFinalRisk, setOriginalFinalRisk] = useState(0)
  const [simulatedFinalRisk, setSimulatedFinalRisk] = useState(0)

  // Refs for GSAP
  const timelineRef = useRef<HTMLDivElement>(null)

  // --- Fetch Actual Session Events ---
  useEffect(() => {
    if (!sessionId) return
    
    // Fetch the real events for this session
    apiService.events.getEventsBySession(sessionId)
      .then(res => {
        const data = res.data
        if (!Array.isArray(data)) return
        const mappedEvents: SessionEvent[] = data.map((evt, idx) => ({
          id: evt.eventId || `evt_${idx}`,
          step: idx + 1,
          eventType: evt.eventType || 'API_REQUEST',
          endpoint: evt.endpoint || '',
          method: evt.httpMethod || 'GET',
          originalDecision: (evt.decision as any) || 'ALLOW',
          originalRisk: evt.riskScore || 0,
          query: evt.requestContext?.query || '',
          ip: evt.sourceIp || ''
        }))
        setOriginalEvents(mappedEvents)
        const maxRisk = mappedEvents.length > 0 ? Math.max(...mappedEvents.map(e => e.originalRisk)) : 0
        setOriginalFinalRisk(Math.round(maxRisk * 100))
      })
      .catch(err => console.error("Error fetching simulation events:", err))
  }, [sessionId])

  // --- Simulation Logic ---
  const runSimulation = async () => {
    if (!sessionId) return
    setIsSimulating(true)
    setHasSimulated(false)
    setDisplayedEvents([])
    
    // Map toggles to rule IDs that our backend seeder created
    const activeRules: string[] = []
    if (blockSql) activeRules.push('RULE-SQL-01')
    if (riskThreshold < 100) activeRules.push('RULE-RISK-01')
    // In a real system, we might dynamically create a rule for the threshold
    // For the demo, we'll assume RULE-RISK-01 matches the threshold or just use the seeder defaults
    
    try {
      const response = await forensicService.runWhatIf(sessionId, { rules: activeRules })
      const report = response.data

      const newEvents: SimulatedEvent[] = report.steps.map((step, idx) => {
        const originalEvt = originalEvents[idx] || {}
        return {
          ...originalEvt,
          step: idx + 1,
          simulatedDecision: (step.simulatedDecision as any),
          simulatedRisk: step.riskScore,
          diverged: step.originalDecision !== step.simulatedDecision,
          divergenceReason: step.originalDecision !== step.simulatedDecision ? `Policy triggered: ${step.ruleId}` : ''
        }
      })

      setSimulatedEvents(newEvents)
      const maxRisk = newEvents.length > 0 ? Math.max(...newEvents.map(e => e.simulatedRisk)) : 0
      setSimulatedFinalRisk(Math.round(maxRisk * 100))
      setIsSimulating(false)
      setHasSimulated(true)
      playReplay(newEvents)
    } catch (err) {
      console.error("Simulation failed:", err)
      setIsSimulating(false)
    }
  }

  // --- GSAP Replay Animation ---
  const playReplay = (events: SimulatedEvent[]) => {
    setDisplayedEvents([])
    const tl = gsap.timeline()

    events.forEach((evt, index) => {
      tl.add(() => {
        setDisplayedEvents(prev => [...prev, evt])
      }, index * 0.8) // 800ms between steps
    })
  }

  // --- Helpers ---
  const getDecisionIcon = (decision: EventDecision) => {
    switch (decision) {
      case 'ALLOW': return <CheckCircle className="text-emerald-500" size={20} />
      case 'FLAG': return <AlertTriangle className="text-amber-500" size={20} />
      case 'DENY': return <XCircle className="text-rose-500" size={20} />
    }
  }
  
  const getDecisionColor = (decision: EventDecision) => {
    switch (decision) {
      case 'ALLOW': return 'text-emerald-400 bg-emerald-400/10 border-emerald-500/20'
      case 'FLAG': return 'text-amber-400 bg-amber-400/10 border-amber-500/20'
      case 'DENY': return 'text-rose-400 bg-rose-400/10 border-rose-500/20'
    }
  }

  return (
    <motion.div
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      exit={{ opacity: 0 }}
      className="min-h-screen bg-slate-950 flex flex-col text-slate-200"
    >
      {/* Header */}
      <div className="border-b border-slate-800 bg-slate-950/80 backdrop-blur-md sticky top-0 z-40 px-8 py-4 flex items-center justify-between">
        <div className="flex items-center gap-4">
          <button
            onClick={() => navigate(-1)}
            className="p-2 hover:bg-slate-800 rounded-lg transition-colors text-slate-400 hover:text-white"
          >
            <ChevronLeft size={20} />
          </button>
          <div>
            <h1 className="text-2xl font-bold text-white bg-clip-text text-transparent bg-gradient-to-r from-indigo-400 to-cyan-400">
              What-If Simulation
            </h1>
            <p className="text-sm text-slate-500">Session: {sessionId || 'sess_mock123'}</p>
          </div>
        </div>
      </div>

      <div className="flex-1 flex flex-col lg:flex-row overflow-hidden relative">
        {/* --- LEFT: CONTROLS PANEL --- */}
        <div className="w-full lg:w-1/3 lg:border-r border-slate-800 p-8 overflow-y-auto bg-slate-900/50">
          <h2 className="text-lg font-semibold text-white mb-6 flex items-center gap-2">
            <ShieldAlert size={20} className="text-indigo-400" />
            Simulation Controls
          </h2>

          <div className="space-y-8">
            {/* Rule Toggles */}
            <div className="space-y-4">
              <h3 className="text-sm font-medium text-slate-400 uppercase tracking-wider">Rule Toggles</h3>
              
              <label className="flex items-center justify-between p-4 rounded-xl bg-slate-800/50 border border-slate-700/50 cursor-pointer hover:bg-slate-800 transition-colors">
                <span className="font-medium">Block SQL keywords</span>
                <input 
                  type="checkbox" 
                  className="w-5 h-5 rounded border-slate-600 text-indigo-500 focus:ring-indigo-500 bg-slate-900"
                  checked={blockSql}
                  onChange={(e) => setBlockSql(e.target.checked)}
                />
              </label>

              <label className="flex items-center justify-between p-4 rounded-xl bg-slate-800/50 border border-slate-700/50 cursor-pointer hover:bg-slate-800 transition-colors">
                <span className="font-medium">Rate limiting</span>
                <input 
                  type="checkbox" 
                  className="w-5 h-5 rounded border-slate-600 text-indigo-500 focus:ring-indigo-500 bg-slate-900"
                  checked={rateLimiting}
                  onChange={(e) => setRateLimiting(e.target.checked)}
                />
              </label>
              
              <label className="flex items-center justify-between p-4 rounded-xl bg-slate-800/50 border border-slate-700/50 cursor-pointer hover:bg-slate-800 transition-colors">
                <span className="font-medium">Input sanitization</span>
                <input 
                  type="checkbox" 
                  className="w-5 h-5 rounded border-slate-600 text-indigo-500 focus:ring-indigo-500 bg-slate-900"
                  checked={inputSanitization}
                  onChange={(e) => setInputSanitization(e.target.checked)}
                />
              </label>
            </div>

            {/* Risk Slider */}
            <div className="space-y-4">
              <div className="flex justify-between items-center">
                <h3 className="text-sm font-medium text-slate-400 uppercase tracking-wider">Risk Sensitivity</h3>
                <span className="text-indigo-400 font-mono bg-indigo-500/10 px-2 py-1 rounded text-xs">{riskThreshold}%</span>
              </div>
              <input 
                type="range" 
                min="0" 
                max="100" 
                value={riskThreshold}
                onChange={(e) => setRiskThreshold(Number(e.target.value))}
                className="w-full h-2 bg-slate-800 rounded-lg appearance-none cursor-pointer accent-indigo-500"
              />
              <div className="flex justify-between text-xs text-slate-500">
                <span>Lenient (100%)</span>
                <span>Strict (0%)</span>
              </div>
            </div>

            {/* Custom Rule */}
            <div className="space-y-4">
              <h3 className="text-sm font-medium text-slate-400 uppercase tracking-wider">Custom Rule (Advanced)</h3>
              <textarea 
                placeholder="IF query contains 'DROP' -> block"
                value={customRule}
                onChange={(e) => setCustomRule(e.target.value)}
                className="w-full h-24 bg-slate-950 border border-slate-700 rounded-xl p-4 text-sm font-mono text-slate-300 focus:outline-none focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500 resize-none transition-all"
              />
            </div>

            {/* Run Button */}
            <button
              onClick={runSimulation}
              disabled={isSimulating}
              className="w-full py-4 px-6 bg-gradient-to-r from-indigo-600 to-cyan-600 hover:from-indigo-500 hover:to-cyan-500 text-white rounded-xl font-bold shadow-[0_0_20px_rgba(79,70,229,0.3)] transition-all flex items-center justify-center gap-2 hover:scale-[1.02] active:scale-[0.98] disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {isSimulating ? (
                <motion.div animate={{ rotate: 360 }} transition={{ repeat: Infinity, duration: 1, ease: "linear" }} className="w-5 h-5 border-2 border-white/30 border-t-white rounded-full" />
              ) : (
                <><Play fill="currentColor" size={18} /> Run Simulation</>
              )}
            </button>
          </div>
        </div>

        {/* --- RIGHT: REPLAY ENGINE --- */}
        <div className="w-full lg:w-2/3 flex flex-col relative bg-[#0a0f1c] bg-[radial-gradient(ellipse_80%_80%_at_50%_-20%,rgba(120,119,198,0.15),rgba(255,255,255,0))]">
          
          <div className="flex-1 overflow-y-auto p-8 pb-32" ref={timelineRef}>
            {displayedEvents.length === 0 && !isSimulating && !hasSimulated ? (
               <div className="h-full flex flex-col items-center justify-center text-slate-500">
                  <ShieldCheck size={64} className="mb-4 opacity-20" />
                  <p className="text-lg">Configure rules and run simulation to view changes.</p>
               </div>
            ) : null}

            <div className="max-w-3xl mx-auto space-y-6">
              <AnimatePresence>
                {displayedEvents.map((evt, idx) => (
                  <motion.div
                    key={evt.id}
                    initial={{ opacity: 0, y: 20, x: -20 }}
                    animate={{ opacity: 1, y: 0, x: 0 }}
                    className={`relative p-6 rounded-2xl border ${evt.diverged ? 'bg-indigo-950/20 border-indigo-500/30' : 'bg-slate-900/50 border-slate-800'}`}
                  >
                    {/* Connecting Line */}
                    {idx !== displayedEvents.length - 1 && (
                       <div className="absolute left-8 top-[100%] w-0.5 h-6 bg-slate-800" />
                    )}

                    <div className="flex justify-between items-start mb-4">
                      <div className="flex items-center gap-3">
                        <div className="w-8 h-8 rounded-full bg-slate-800 flex items-center justify-center text-sm font-bold font-mono">
                          {evt.step}
                        </div>
                        <div>
                           <h4 className="font-medium text-white">{evt.eventType}</h4>
                           <span className="text-xs font-mono text-slate-500">{evt.method} {evt.endpoint}</span>
                        </div>
                      </div>
                      
                      {/* Original vs Simulated Badge */}
                      <div className="flex items-center gap-4 text-sm font-mono">
                        <div className="flex flex-col items-end">
                           <span className="text-[10px] text-slate-500 uppercase">Original</span>
                           <span className="text-slate-400">{evt.originalDecision}</span>
                        </div>
                        {evt.diverged && (
                          <>
                            <motion.div 
                              initial={{ scale: 0 }} 
                              animate={{ scale: 1 }} 
                              className="text-indigo-400"
                            >
                              →
                            </motion.div>
                            <div className="flex flex-col items-end">
                              <span className="text-[10px] text-indigo-400 uppercase">Simulated</span>
                              <span className={`px-2 py-0.5 rounded border ${getDecisionColor(evt.simulatedDecision)}`}>
                                {evt.simulatedDecision}
                              </span>
                            </div>
                          </>
                        )}
                      </div>
                    </div>

                    {/* Query Info */}
                    {evt.query && (
                      <div className="mt-4 p-3 bg-slate-950 rounded-lg border border-slate-800/50 font-mono text-sm text-amber-200/70 break-all">
                        {evt.query}
                      </div>
                    )}

                    {/* Divergence Highlight */}
                    {evt.diverged && (
                      <motion.div 
                        initial={{ opacity: 0, height: 0 }}
                        animate={{ opacity: 1, height: 'auto' }}
                        className="mt-4 p-3 bg-indigo-500/10 border border-indigo-500/20 rounded-lg flex items-start gap-3"
                      >
                         <ShieldAlert size={16} className="text-indigo-400 mt-0.5 flex-shrink-0" />
                         <div>
                            <p className="text-sm text-indigo-200 font-medium">Behavior Diverged</p>
                            <p className="text-xs text-indigo-300/70">{evt.divergenceReason}</p>
                         </div>
                      </motion.div>
                    )}
                  </motion.div>
                ))}
              </AnimatePresence>
            </div>
          </div>

          {/* --- BOTTOM: RESULTS PANEL --- */}
          <AnimatePresence>
            {hasSimulated && displayedEvents.length === originalEvents.length && (
              <motion.div
                initial={{ y: "100%" }}
                animate={{ y: 0 }}
                exit={{ y: "100%" }}
                transition={{ type: "spring", damping: 25, stiffness: 200 }}
                className="absolute bottom-0 left-0 w-full bg-slate-900 border-t border-slate-700/50 p-6 z-10 backdrop-blur-xl"
              >
                <div className="max-w-4xl mx-auto flex flex-col sm:flex-row items-center justify-between gap-8">
                  
                  {/* Risk Comparison */}
                  <div className="flex-1 flex justify-around items-center w-full">
                    <div className="text-center">
                      <p className="text-xs text-slate-400 uppercase tracking-wider mb-1">Original Risk</p>
                      <p className="text-3xl font-light text-rose-400 font-mono">{originalFinalRisk}%</p>
                    </div>
                    
                    <div className="w-16 h-0.5 bg-slate-800 relative">
                       <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-6 h-6 rounded-full bg-slate-800 flex items-center justify-center">
                          <ChevronLeft size={14} className="rotate-180 text-slate-500" />
                       </div>
                    </div>

                    <div className="text-center">
                      <p className="text-xs text-indigo-400 uppercase tracking-wider mb-1">Simulated Risk</p>
                      <p className="text-3xl font-light text-indigo-400 font-mono">{simulatedFinalRisk}%</p>
                    </div>
                  </div>

                  {/* Outcome Comparison */}
                  <div className="flex-1 bg-slate-950/50 rounded-xl p-4 border border-slate-800 w-full">
                    <h4 className="text-xs text-slate-400 uppercase tracking-wider mb-3">Simulation Outcome</h4>
                    <div className="space-y-2 text-sm">
                      <div className="flex justify-between items-center">
                        <span className="text-slate-500">Original Result:</span>
                        <span className="text-rose-400 font-mono bg-rose-400/10 px-2 py-0.5 rounded">Flagged at Step 4</span>
                      </div>
                      <div className="flex justify-between items-center">
                        <span className="text-indigo-300">Simulated Result:</span>
                        <span className="text-indigo-400 font-mono bg-indigo-400/10 px-2 py-0.5 rounded font-medium flex items-center gap-1">
                          <ShieldAlert size={14}/> 
                          {simulatedFinalRisk >= 99 ? 'Blocked at Step 3' : 'Flagged at Step 4'}
                        </span>
                      </div>
                    </div>
                  </div>

                </div>
              </motion.div>
            )}
          </AnimatePresence>

        </div>
      </div>
    </motion.div>
  )
}
