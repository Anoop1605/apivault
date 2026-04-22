import React from 'react'
import { motion } from 'framer-motion'
import { CheckCircle, XCircle, AlertCircle } from 'lucide-react'
import { ReplayStep } from '../../types/api.types'

interface ReplayTimelineProps {
  steps: ReplayStep[]
  currentStep: number
  onSelectStep: (index: number) => void
}

export const ReplayTimeline: React.FC<ReplayTimelineProps> = ({
  steps,
  currentStep,
  onSelectStep,
}) => {
  const getDecisionIcon = (decision: string) => {
    switch (decision) {
      case 'ALLOW':
        return <CheckCircle size={20} className="text-green-400" />
      case 'DENY':
        return <XCircle size={20} className="text-red-400" />
      case 'FLAG':
        return <AlertCircle size={20} className="text-orange-400" />
      default:
        return <CheckCircle size={20} className="text-slate-400" />
    }
  }

  const getDecisionColor = (decision: string) => {
    switch (decision) {
      case 'ALLOW':
        return 'border-green-500/50 bg-green-900/20'
      case 'DENY':
        return 'border-red-500/50 bg-red-900/20'
      case 'FLAG':
        return 'border-orange-500/50 bg-orange-900/20'
      default:
        return 'border-slate-500/50 bg-slate-900/20'
    }
  }

  return (
    <div className="space-y-4">
      <div className="flex items-center gap-2 mb-6">
        <div className="w-3 h-3 rounded-full bg-cyan-400 animate-pulse" />
        <h2 className="text-lg font-bold text-white">Event Timeline</h2>
      </div>

      <div className="relative space-y-3">
        {/* Vertical Connector Line */}
        {steps.length > 1 && (
          <div className="absolute left-5 top-12 bottom-0 w-0.5 bg-gradient-to-b from-cyan-500/50 via-slate-500/30 to-red-500/50" />
        )}

        {/* Timeline Steps */}
        {steps.map((step, index) => (
          <motion.div
            key={step.step}
            initial={{ opacity: 0, x: -20 }}
            animate={{ opacity: 1, x: 0 }}
            transition={{ delay: index * 0.1 }}
            onClick={() => onSelectStep(index)}
            className="relative cursor-pointer"
          >
            {/* Step Number Circle */}
            <div className="absolute left-0 top-2 w-11 h-11 rounded-full bg-slate-800 border-2 border-cyan-500/50 flex items-center justify-center z-10">
              <span className="font-bold text-sm text-white">{index + 1}</span>
            </div>

            {/* Step Card */}
            <motion.div
              whileHover={{ scale: 1.02, x: 8 }}
              className={`ml-16 p-4 rounded-lg border transition-all ${
                index === currentStep
                  ? `${getDecisionColor(step.decision)} ring-2 ring-cyan-500/50 shadow-lg shadow-cyan-500/20`
                  : `${getDecisionColor(step.decision)} hover:border-slate-400/50`
              }`}
            >
              <div className="flex items-start gap-3">
                {getDecisionIcon(step.decision)}
                <div className="flex-1 min-w-0">
                  <div className="flex items-center gap-2 mb-1">
                    <span className="font-semibold text-white truncate">
                      {step.event_type}
                    </span>
                    <span className={`text-xs font-bold px-2 py-0.5 rounded-full ${
                      step.decision === 'ALLOW' ? 'bg-green-500/30 text-green-300' :
                      step.decision === 'DENY' ? 'bg-red-500/30 text-red-300' :
                      'bg-orange-500/30 text-orange-300'
                    }`}>
                      {step.decision}
                    </span>
                  </div>
                  {step.endpoint && (
                    <div className="text-xs text-slate-300 font-mono truncate">
                      {step.endpoint}
                    </div>
                  )}
                  <div className="flex items-center gap-4 mt-2 text-xs text-slate-400">
                    <span>Risk: <span className="text-orange-300 font-semibold">{(step.risk_score * 100).toFixed(0)}%</span></span>
                    {step.rule_id && <span>Rule: <span className="text-cyan-300">{step.rule_id}</span></span>}
                  </div>
                </div>
              </div>
            </motion.div>
          </motion.div>
        ))}
      </div>
    </div>
  )
}
