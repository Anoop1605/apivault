import React, { useState, useEffect } from 'react'
import { useParams } from 'react-router-dom'
import Topbar from '../components/Topbar'
import { SessionHeader } from '../components/SessionHeader'
import { Timeline } from '../components/Timeline'
import { StepDetails } from '../components/StepDetails'
import { ActionBar } from '../components/ActionBar'
import { TimelineStep } from '../components/StepCard'
import { forensicService } from '../services/api'

const Session = () => {
  // Grab the session ID from the URL (e.g., /sessions/sess-123)
  const { sessionId } = useParams<{ sessionId: string }>()
  
  const [steps, setSteps] = useState<TimelineStep[]>([])
  const [selectedStepId, setSelectedStepId] = useState<string | null>(null)
  const [loading, setLoading] = useState(true)

  // Fetch the detailed session timeline
  useEffect(() => {
    const fetchSessionData = async () => {
      if (!sessionId) return
      try {
        setLoading(true)
        // Fetch timeline events. Depending on your API, this might be getTimeline or a specific detail endpoint
        const { data } = await forensicService.getTimeline(sessionId)
        
        // Assuming data maps directly to TimelineStep interface.
        const timelineData = data as TimelineStep[]
        setSteps(timelineData || [])
        
        // Auto-select the first event or the first DENY event
        if (timelineData && timelineData.length > 0) {
          const firstBlock = timelineData.find(s => s.decision === 'DENY')
          setSelectedStepId(firstBlock ? firstBlock.id : timelineData[0].id)
        }
      } catch (error) {
        console.error('Failed to fetch session timeline:', error)
      } finally {
        setLoading(false)
      }
    }
    
    fetchSessionData()
  }, [sessionId])

  const selectedStep = steps.find(s => s.id === selectedStepId) || null

  // Derive summary metrics dynamically from the loaded steps
  const maxRisk = steps.length > 0 ? Math.max(...steps.map(s => s.riskScore || 0)) : 0
  const duration = steps.length > 1 
    ? steps[steps.length - 1].timestamp - steps[0].timestamp 
    : 0
  const isCompromised = steps.some(s => s.decision === 'DENY')
  const status = isCompromised ? 'compromised' : maxRisk > 0.4 ? 'suspicious' : 'active'
  const userId = steps.length > 0 ? steps[0].userName || 'Unknown' : 'Unknown'

  return (
    <div className="min-h-screen bg-slate-950 flex flex-col">
      <Topbar title="Session Explorer" />
      
      {/* Page Content: Account for the fixed Topbar */}
      <div className="flex-1 pt-16 flex flex-col h-screen overflow-hidden">
        
        {/* Header Summary */}
        <SessionHeader 
          sessionId={sessionId || 'Unknown'} 
          userId={userId}
          maxRisk={maxRisk}
          status={status}
          eventCount={steps.length}
          duration={duration}
        />
        
        {/* Main Split View */}
        <div className="flex-1 flex overflow-hidden p-6 gap-6 max-w-[1600px] mx-auto w-full">
          {loading ? (
             <div className="flex items-center justify-center w-full h-full text-slate-400">
               <div className="w-6 h-6 border-2 border-cyan-500 border-t-transparent rounded-full animate-spin mr-3"></div>
               Loading forensic data...
             </div>
          ) : steps.length === 0 ? (
             <div className="flex items-center justify-center w-full h-full text-slate-400">
               No timeline events found for this session.
             </div>
          ) : (
            <>
              {/* Left Column: Interactive Timeline List */}
              <div className="w-1/2 lg:w-5/12 bg-slate-900/40 rounded-xl border border-slate-700/50 p-6 overflow-hidden flex flex-col shadow-lg">
                <Timeline 
                  steps={steps} 
                  selectedStepId={selectedStepId} 
                  onSelectStep={(step) => setSelectedStepId(step.id)} 
                />
              </div>

              {/* Right Column: Deep Dive Details for selected event */}
              <div className="flex-1 bg-slate-900/40 rounded-xl border border-slate-700/50 p-6 overflow-hidden flex flex-col shadow-lg">
                <StepDetails step={selectedStep} />
              </div>
            </>
          )}
        </div>

        {/* Sticky Action Footer */}
        <ActionBar 
          sessionId={sessionId || ''}
          onReplay={() => console.log('Replay trigger clicked')}
          onSimulate={() => console.log('Policy simulation triggered')}
        />
      </div>
    </div>
  )
}

export default Session