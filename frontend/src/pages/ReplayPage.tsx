import React, { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { motion, AnimatePresence } from 'framer-motion'
import { ChevronLeft } from 'lucide-react'
import { forensicService } from '../services/api'
import { ReplayStep, ReplayResponse } from '../types/api.types'
import { ReplayHeader } from '../components/replay/ReplayHeader'
import { Controls } from '../components/replay/Controls'
import { ReplayTimeline } from '../components/replay/ReplayTimeline'
import { ReplayStepDetails } from '../components/replay/ReplayStepDetails'
import { RiskChart } from '../components/replay/RiskChart'
import { InsightsPanel } from '../components/replay/InsightsPanel'

export const ReplayPage: React.FC = () => {
  const { sessionId } = useParams<{ sessionId: string }>()
  const navigate = useNavigate()

  // State Management
  const [steps, setSteps] = useState<ReplayStep[]>([])
  const [currentStep, setCurrentStep] = useState(0)
  const [isPlaying, setIsPlaying] = useState(false)
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [summary, setSummary] = useState<any>(null)

  // Fetch replay data
  useEffect(() => {
    if (!sessionId) return

    const fetchReplay = async () => {
      try {
        setIsLoading(true)
        const response = await forensicService.getReplay(sessionId)
        const data: ReplayResponse = response.data
        setSteps(data.steps)
        setSummary(data.summary)
        setCurrentStep(0)
        setError(null)
      } catch (err) {
        console.error('Failed to load replay:', err)
        setError('Failed to load forensic replay data. Ensure the session exists in the Event Store.')
        setSteps([])
        setSummary(null)
      } finally {
        setIsLoading(false)
      }
    }

    fetchReplay()
  }, [sessionId])

  // Playback interval
  useEffect(() => {
    if (!isPlaying || currentStep >= steps.length - 1) {
      return
    }

    const interval = setInterval(() => {
      setCurrentStep((prev) => {
        if (prev >= steps.length - 1) {
          setIsPlaying(false)
          return prev
        }
        return prev + 1
      })
    }, 800)

    return () => clearInterval(interval)
  }, [isPlaying, currentStep, steps.length])

  // Control handlers
  const handlePlay = () => setIsPlaying(true)
  const handlePause = () => setIsPlaying(false)
  const handleNext = () => {
    setIsPlaying(false)
    if (currentStep < steps.length - 1) {
      setCurrentStep((prev) => prev + 1)
    }
  }
  const handlePrev = () => {
    setIsPlaying(false)
    if (currentStep > 0) {
      setCurrentStep((prev) => prev - 1)
    }
  }
  const handleRestart = () => {
    setCurrentStep(0)
    setIsPlaying(false)
  }
  const handleJumpTo = (step: number) => {
    setIsPlaying(false)
    setCurrentStep(step)
  }

  if (isLoading) {
    return (
      <div className="w-full h-screen flex items-center justify-center bg-slate-950">
        <motion.div
          animate={{ opacity: [0.5, 1, 0.5] }}
          transition={{ duration: 2, repeat: Infinity }}
          className="text-lg text-slate-300"
        >
          Loading replay...
        </motion.div>
      </div>
    )
  }

  const currentStepData = steps[currentStep]
  const visibleSteps = steps.slice(0, currentStep + 1)

  return (
    <motion.div
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      exit={{ opacity: 0 }}
      className="w-full min-h-screen bg-slate-950 flex flex-col"
    >
      {/* Aurora Background */}
      <div className="fixed inset-0 -z-10 bg-gradient-to-br from-slate-950 via-slate-900 to-slate-950" />

      {/* Sticky Top Section (Header + Controls) */}
      <div className="sticky top-0 z-40 bg-slate-950/80 backdrop-blur-md border-b border-slate-700/50 flex flex-col shadow-lg shadow-black/20">
        
        {/* Header */}
        <div className="max-w-7xl mx-auto w-full px-8 py-4">
          <div className="flex items-center gap-3 mb-4">
            <button
              onClick={() => navigate('/sessions')}
              className="p-2 hover:bg-slate-800 rounded-lg transition-colors"
            >
              <ChevronLeft size={20} className="text-slate-300" />
            </button>
            <h1 className="text-2xl font-bold text-white">
              🔁 Replay Session
            </h1>
          </div>
          <ReplayHeader sessionId={sessionId || ''} />
        </div>

        {/* Error Alert */}
        {error && (
          <motion.div
            initial={{ opacity: 0, y: -10 }}
            animate={{ opacity: 1, y: 0 }}
            className="mx-8 mb-4 p-3 bg-orange-500/20 border border-orange-500/50 rounded-lg text-orange-300 text-sm"
          >
            {error}
          </motion.div>
        )}

        {/* Controls */}
        <div className="border-t border-slate-700/50 bg-slate-950/40 px-8 py-4">
          <Controls
            isPlaying={isPlaying}
            currentStep={currentStep}
            totalSteps={steps.length}
            onPlay={handlePlay}
            onPause={handlePause}
            onNext={handleNext}
            onPrev={handlePrev}
            onRestart={handleRestart}
            onJumpTo={handleJumpTo}
          />
        </div>
      </div>

      {/* Main Content - Timeline + Details */}
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
              <ReplayTimeline
                steps={visibleSteps}
                currentStep={currentStep}
                onSelectStep={handleJumpTo}
              />
            </div>
          </div>
        </div>

        {/* Right: Step Details */}
        <div className="hidden lg:flex lg:w-2/5 flex-col overflow-hidden bg-slate-900/40 border-l border-slate-700/50 relative z-10">
          <div className="flex-1 overflow-y-auto">
            <div className="p-8">
              <AnimatePresence mode="wait">
                {currentStepData && (
                  <motion.div
                    key={currentStep}
                    initial={{ opacity: 0, y: 20 }}
                    animate={{ opacity: 1, y: 0 }}
                    exit={{ opacity: 0, y: -20 }}
                    transition={{ duration: 0.3 }}
                  >
                    <ReplayStepDetails step={currentStepData} />
                  </motion.div>
                )}
              </AnimatePresence>
            </div>
          </div>
        </div>
      </motion.div>

      {/* Risk Chart */}
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.3 }}
        className="border-t border-slate-700/50 bg-slate-950/50 px-8 py-6"
      >
        <RiskChart steps={steps} currentStep={currentStep} />
      </motion.div>

      {/* Insights Panel */}
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.4 }}
        className="border-t border-slate-700/50 bg-slate-950/50 px-8 py-6"
      >
        <InsightsPanel steps={steps} currentStep={currentStep} summary={summary} />
      </motion.div>
    </motion.div>
  )
}


export default ReplayPage
