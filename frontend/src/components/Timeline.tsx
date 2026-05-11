import React from 'react'
import { motion } from 'framer-motion'
import { StepCard, TimelineStep } from './StepCard'

interface TimelineProps {
  steps: TimelineStep[]
  selectedStepId: string | null
  onSelectStep: (step: TimelineStep) => void
}

export const Timeline: React.FC<TimelineProps> = ({
  steps,
  selectedStepId,
  onSelectStep,
}) => {
  // Find the first DENY (attack/block point)
  const attackPointIndex = steps.findIndex(s => s.decision === 'DENY')

  return (
    <div className="flex flex-col gap-6 h-full overflow-y-auto pr-2">
      {/* Timeline Header */}
      <div className="sticky top-0 bg-gradient-to-b from-slate-950 via-slate-950 to-transparent pb-6 z-10 border-b border-slate-700/30">
        <h2 className="text-xl font-bold text-white mb-3 flex items-center gap-2">
          <div className="w-3 h-3 rounded-full bg-cyan-400 animate-pulse"></div>
          Event Timeline
        </h2>
        <div className="text-sm text-slate-400 space-y-1">
          <div>{steps.length} events total</div>
          <div className="text-cyan-400/70 font-semibold">{steps.filter(s => s.decision === 'DENY').length} blocked</div>
        </div>
      </div>

      {/* Timeline Steps with Vertical Connector */}
      <div className="relative pb-8">
        {/* Vertical line connecting all steps */}
        <div className="absolute left-[18px] top-2 bottom-0 w-0.5 bg-gradient-to-b from-cyan-500/50 via-slate-500/30 to-red-500/50 rounded-full" />

        {/* Steps */}
        <div className="space-y-4">
          {steps.map((step, index) => (
            <div key={step.id} className="relative pl-12 overflow-hidden">
              {/* Timeline dot */}
              <div
                className={`
                  absolute left-0 top-3 w-10 h-10 rounded-full flex items-center justify-center
                  border-2 transition-all duration-300 z-20
                  ${selectedStepId === step.id ? 'scale-125' : ''}
                  ${step.decision === 'ALLOW' ? 'bg-green-900/50 border-green-400' : ''}
                  ${step.decision === 'DENY' ? 'bg-red-900/50 border-red-400' : ''}
                  ${step.decision === 'FLAG' ? 'bg-orange-900/50 border-orange-400' : ''}
                  ${!['ALLOW', 'DENY', 'FLAG'].includes(step.decision) ? 'bg-slate-700/50 border-slate-400' : ''}
                `}
              >
                <span className="font-bold text-xs text-white">{index + 1}</span>
              </div>

              {/* Step Card Container with Overflow Clipping */}
              <div className="overflow-hidden rounded-lg">
                <StepCard
                  step={step}
                  isSelected={selectedStepId === step.id}
                  isAttackPoint={attackPointIndex === index && step.decision === 'DENY'}
                  onSelect={onSelectStep}
                  index={index}
                />
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Timeline Footer */}
      <div className="sticky bottom-0 bg-gradient-to-t from-slate-950 via-slate-950 to-transparent pt-6 mt-6 border-t border-slate-700/30 text-xs text-slate-400 space-y-2">
        {steps.length > 0 && (
          <>
            <div className="flex justify-between">
              <span>Duration:</span>
              <span className="text-cyan-400 font-semibold">{((steps[steps.length - 1].timestamp - steps[0].timestamp) / 1000).toFixed(2)}s</span>
            </div>
            <div className="flex justify-between">
              <span>Max Risk:</span>
              <span className="text-orange-400 font-semibold">{(Math.max(...steps.map(s => s.riskScore)) * 100).toFixed(0)}%</span>
            </div>
          </>
        )}
      </div>
    </div>
  )
}
