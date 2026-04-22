import React from 'react'
import { AlertCircle, CheckCircle, AlertTriangle, Clock } from 'lucide-react'
import { motion } from 'framer-motion'
import { BorderGlow } from './BorderGlow'

export interface TimelineStep {
  id: string
  eventType: 'REQUEST_RECEIVED' | 'POLICY_EVALUATED' | 'POLICY_ALLOWED' | 'RISK_FLAGGED' | 'POLICY_DENIED'
  endpoint: string
  method: string
  decision: 'ALLOW' | 'DENY' | 'FLAG'
  riskScore: number
  timestamp: number
  policyRuleId?: string
  userName: string
  ipAddress: string
  requestBody?: string
  headers?: Record<string, string>
  policyConditions?: {
    condition: string
    required: boolean
    met: boolean
  }[]
  failedConditions?: string[]
}

interface StepCardProps {
  step: TimelineStep
  isSelected: boolean
  isAttackPoint: boolean
  onSelect: (step: TimelineStep) => void
  index: number
}

export const StepCard: React.FC<StepCardProps> = ({
  step,
  isSelected,
  isAttackPoint,
  onSelect,
  index,
}) => {
  const getDecisionColor = (decision: string) => {
    switch (decision) {
      case 'ALLOW':
        return { bg: 'bg-green-900/20', border: 'border-green-500/50', glow: '34 197 94' }
      case 'DENY':
        return { bg: 'bg-red-900/20', border: 'border-red-500/50', glow: '239 68 68' }
      case 'FLAG':
        return { bg: 'bg-orange-900/20', border: 'border-orange-500/50', glow: '249 115 22' }
      default:
        return { bg: 'bg-slate-800/20', border: 'border-slate-500/50', glow: '100 116 139' }
    }
  }

  const getDecisionIcon = (decision: string) => {
    switch (decision) {
      case 'ALLOW':
        return <CheckCircle className="w-5 h-5 text-green-400" />
      case 'DENY':
        return <AlertCircle className="w-5 h-5 text-red-400" />
      case 'FLAG':
        return <AlertTriangle className="w-5 h-5 text-orange-400" />
      default:
        return <Clock className="w-5 h-5 text-slate-400" />
    }
  }

  const colors = getDecisionColor(step.decision)

  const cardContent = (
    <motion.div
      initial={{ opacity: 0, x: -20 }}
      animate={{ opacity: 1, x: 0 }}
      transition={{ delay: index * 0.1 }}
      onClick={() => onSelect(step)}
      className={`
        relative p-4 rounded-lg border-2 cursor-pointer transition-all duration-300
        ${colors.bg} ${colors.border}
        ${isSelected ? 'ring-2 ring-offset-2 ring-offset-slate-950 ring-cyan-500' : ''}
        ${isAttackPoint ? 'ring-4 ring-offset-2 ring-offset-slate-950 ring-red-500 scale-105' : ''}
        hover:scale-105 hover:shadow-lg
      `}
      style={{
        boxShadow: isAttackPoint 
          ? `0 0 30px rgba(239, 68, 68, 0.6), inset 0 0 20px rgba(239, 68, 68, 0.2)`
          : undefined
      }}
    >
      {/* Attack Point Badge */}
      {isAttackPoint && (
        <div className="absolute -top-3 -right-3 bg-red-600 text-white text-xs font-bold px-2 py-1 rounded-full">
          🚨 Block Point
        </div>
      )}

      <div className="flex items-start gap-3">
        <div className="flex-shrink-0 mt-1">
          {getDecisionIcon(step.decision)}
        </div>

        <div className="flex-grow min-w-0">
          <div className="font-semibold text-white text-sm mb-2">
            {step.eventType.replace(/_/g, ' ')}
          </div>

          <div className="space-y-1 text-xs text-slate-300">
            <div className="flex justify-between">
              <span className="font-mono">{step.endpoint}</span>
              <span className="text-slate-300 font-medium">{step.method}</span>
            </div>
            <div className="flex justify-between items-center">
              <span>Risk: <span className="font-semibold text-orange-300">{(step.riskScore * 100).toFixed(0)}%</span></span>
              <span className="px-2 py-0.5 rounded-full bg-slate-700/40 border border-slate-600/50 text-slate-200 font-mono text-xs whitespace-nowrap hover:bg-slate-700/60 transition-colors">
                <Clock className="w-3 h-3 inline mr-1" />
                {new Date(step.timestamp).toLocaleTimeString()}
              </span>
            </div>
          </div>
        </div>
      </div>

      {/* Decision Badge */}
      <div className="absolute bottom-2 right-2">
        <span className={`
          px-2 py-1 rounded text-xs font-bold
          ${step.decision === 'ALLOW' ? 'bg-green-600/30 text-green-300' : ''}
          ${step.decision === 'DENY' ? 'bg-red-600/30 text-red-300' : ''}
          ${step.decision === 'FLAG' ? 'bg-orange-600/30 text-orange-300' : ''}
        `}>
          {step.decision}
        </span>
      </div>
    </motion.div>
  )

  // Wrap with BorderGlow for premium effect
  if (step.decision === 'DENY') {
    return (
      <BorderGlow glowColor="239 68 68" intensity={isAttackPoint ? 80 : 40} className="rounded-lg">
        {cardContent}
      </BorderGlow>
    )
  }

  if (step.decision === 'ALLOW') {
    return (
      <BorderGlow glowColor="34 197 94" intensity={30} className="rounded-lg">
        {cardContent}
      </BorderGlow>
    )
  }

  if (step.decision === 'FLAG') {
    return (
      <BorderGlow glowColor="249 115 22" intensity={60} className="rounded-lg">
        {cardContent}
      </BorderGlow>
    )
  }

  return cardContent
}
