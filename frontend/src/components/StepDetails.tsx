import React from 'react'
import { motion, AnimatePresence } from 'framer-motion'
import { TimelineStep } from './StepCard'
import { AlertCircle, CheckCircle, AlertTriangle, Copy } from 'lucide-react'

interface StepDetailsProps {
  step: TimelineStep | null
}

export const StepDetails: React.FC<StepDetailsProps> = ({ step }) => {
  if (!step) {
    return (
      <motion.div
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        className="flex items-center justify-center h-full text-slate-400"
      >
        <div className="text-center space-y-3">
          <AlertCircle className="w-16 h-16 mx-auto opacity-40" />
          <p className="text-lg font-medium">Select a step to view details</p>
        </div>
      </motion.div>
    )
  }

  const getDecisionBadge = () => {
    switch (step.decision) {
      case 'ALLOW':
        return (
          <div className="flex items-center gap-2 px-3 py-1 rounded-full bg-green-900/30 border border-green-500/50 w-fit">
            <CheckCircle className="w-4 h-4 text-green-400" />
            <span className="text-sm font-bold text-green-300">ALLOWED</span>
          </div>
        )
      case 'DENY':
        return (
          <div className="flex items-center gap-2 px-3 py-1 rounded-full bg-red-900/30 border border-red-500/50 w-fit">
            <AlertCircle className="w-4 h-4 text-red-400" />
            <span className="text-sm font-bold text-red-300">DENIED</span>
          </div>
        )
      case 'FLAG':
        return (
          <div className="flex items-center gap-2 px-3 py-1 rounded-full bg-orange-900/30 border border-orange-500/50 w-fit">
            <AlertTriangle className="w-4 h-4 text-orange-400" />
            <span className="text-sm font-bold text-orange-300">FLAGGED</span>
          </div>
        )
      default:
        return null
    }
  }

  return (
    <AnimatePresence mode="wait">
      <motion.div
        key={step.id}
        initial={{ opacity: 0, x: 20 }}
        animate={{ opacity: 1, x: 0 }}
        exit={{ opacity: 0, x: 20 }}
        transition={{ duration: 0.3 }}
        className="flex flex-col gap-8 h-full overflow-y-auto pr-1"
      >
        {/* Event Type & Decision */}
        <div className="space-y-4">
          <div className="flex items-center justify-between gap-4">
            <h3 className="text-2xl font-bold text-white">
              {step.eventType.replace(/_/g, ' ')}
            </h3>
            {getDecisionBadge()}
          </div>
        </div>

        {/* Basic Info */}
        <div className="bg-slate-800/30 border border-slate-700/50 rounded-lg p-4 space-y-2">
          <div className="text-xs text-slate-400 uppercase tracking-wider font-semibold mb-3">
            Event Details
          </div>

          <InfoRow label="Timestamp" value={new Date(step.timestamp).toLocaleString()} />
          <InfoRow label="Endpoint" value={step.endpoint} mono />
          <InfoRow label="Method" value={step.method} />
          <InfoRow label="User" value={step.userName} />
          <InfoRow label="IP Address" value={step.ipAddress} mono />

          {step.policyRuleId && (
            <InfoRow label="Policy Rule" value={step.policyRuleId} mono code />
          )}
        </div>

        {/* Risk Analysis */}
        <div className="bg-slate-800/30 border border-slate-700/50 rounded-lg p-4 space-y-2">
          <div className="text-xs text-slate-400 uppercase tracking-wider font-semibold mb-3">
            Risk Analysis
          </div>

          <div className="space-y-3">
            {/* Risk Score Bar */}
            <div>
              <div className="flex justify-between mb-2">
                <span className="text-sm text-slate-300">Overall Risk Score</span>
                <span className="text-sm font-bold text-orange-300">
                  {(step.riskScore * 100).toFixed(1)}%
                </span>
              </div>
              <div className="w-full bg-slate-700/50 rounded-full h-2 overflow-hidden">
                <motion.div
                  initial={{ width: 0 }}
                  animate={{ width: `${step.riskScore * 100}%` }}
                  transition={{ delay: 0.2, duration: 0.5 }}
                  className={`h-full rounded-full ${
                    step.riskScore >= 0.7
                      ? 'bg-red-500'
                      : step.riskScore >= 0.4
                      ? 'bg-orange-500'
                      : 'bg-green-500'
                  }`}
                />
              </div>
            </div>

            {/* Risk Signals */}
            <div className="pt-2 space-y-2">
              <RiskSignal label="IP Reputation" value={0.3} />
              <RiskSignal label="Request Rate" value={0.8} />
              <RiskSignal label="JWT Anomaly" value={0.5} />
              <RiskSignal label="Endpoint Frequency" value={0.7} />
            </div>
          </div>
        </div>

        {/* Policy Conditions */}
        {step.policyConditions && step.policyConditions.length > 0 && (
          <div className="bg-slate-800/30 border border-slate-700/50 rounded-lg p-4 space-y-3">
            <div className="text-xs text-slate-400 uppercase tracking-wider font-semibold">
              Policy Conditions
            </div>

            <div className="space-y-2">
              {step.policyConditions.map((condition, idx) => (
                <motion.div
                  key={idx}
                  initial={{ opacity: 0, x: -10 }}
                  animate={{ opacity: 1, x: 0 }}
                  transition={{ delay: idx * 0.1 }}
                  className={`
                    flex items-center gap-2 p-2 rounded border text-sm
                    ${
                      condition.met
                        ? 'bg-green-900/20 border-green-500/30'
                        : 'bg-red-900/20 border-red-500/30'
                    }
                  `}
                >
                  {condition.met ? (
                    <CheckCircle className="w-4 h-4 text-green-400 flex-shrink-0" />
                  ) : (
                    <AlertCircle className="w-4 h-4 text-red-400 flex-shrink-0" />
                  )}
                  <span className={condition.met ? 'text-green-300' : 'text-red-300'}>
                    {condition.condition}
                  </span>
                </motion.div>
              ))}
            </div>
          </div>
        )}

        {/* Failed Conditions Highlight */}
        {step.failedConditions && step.failedConditions.length > 0 && (
          <motion.div
            initial={{ opacity: 0, y: 10 }}
            animate={{ opacity: 1, y: 0 }}
            className="bg-red-900/20 border-l-4 border-red-500 rounded-r-lg p-4"
          >
            <div className="flex items-start gap-3">
              <AlertTriangle className="w-5 h-5 text-red-400 flex-shrink-0 mt-0.5" />
              <div>
                <h4 className="font-bold text-red-300 mb-2">Failed Conditions</h4>
                <ul className="space-y-1">
                  {step.failedConditions.map((cond, idx) => (
                    <li key={idx} className="text-sm text-red-300/80">
                      • {cond}
                    </li>
                  ))}
                </ul>
              </div>
            </div>
          </motion.div>
        )}

        {/* Request Body (if available) */}
        {step.requestBody && (
          <div className="bg-slate-800/30 border border-slate-700/50 rounded-lg p-4 space-y-3">
            <div className="text-xs text-slate-400 uppercase tracking-wider font-semibold">
              Request Body
            </div>
            <div className="bg-slate-950/50 rounded p-3 text-xs text-slate-300 font-mono overflow-x-auto max-h-32 overflow-y-auto">
              {step.requestBody}
            </div>
          </div>
        )}

        {/* Action Buttons */}
        <div className="flex gap-2 pt-4 border-t border-slate-700/50">
          <button className="flex-1 px-3 py-2 rounded bg-cyan-600/20 border border-cyan-500/50 text-cyan-300 text-sm font-semibold hover:bg-cyan-600/30 transition">
            Copy Details
          </button>
          <button className="flex-1 px-3 py-2 rounded bg-slate-700/20 border border-slate-600/50 text-slate-300 text-sm font-semibold hover:bg-slate-700/30 transition">
            Export as PDF
          </button>
        </div>
      </motion.div>
    </AnimatePresence>
  )
}

const InfoRow: React.FC<{
  label: string
  value: string
  mono?: boolean
  code?: boolean
}> = ({ label, value, mono = false, code = false }) => (
  <div className="flex justify-between items-start gap-2">
    <span className="text-xs text-slate-400 font-medium">{label}</span>
    <span className={`text-xs text-slate-200 text-right ${mono ? 'font-mono' : ''}`}>
      {code ? (
        <code className="bg-slate-950/50 px-1.5 py-0.5 rounded">{value}</code>
      ) : (
        value
      )}
    </span>
  </div>
)

const RiskSignal: React.FC<{ label: string; value: number }> = ({
  label,
  value,
}) => (
  <div>
    <div className="flex justify-between mb-1">
      <span className="text-xs text-slate-400">{label}</span>
      <span className="text-xs font-semibold text-slate-300">
        {(value * 100).toFixed(0)}%
      </span>
    </div>
    <div className="w-full bg-slate-700/50 rounded-full h-1.5 overflow-hidden">
      <div
        className={`h-full rounded-full ${
          value >= 0.7
            ? 'bg-red-500'
            : value >= 0.4
            ? 'bg-orange-500'
            : 'bg-green-500'
        }`}
        style={{ width: `${value * 100}%` }}
      />
    </div>
  </div>
)
