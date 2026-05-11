import React from 'react'
import { motion } from 'framer-motion'
import { useNavigate, useParams } from 'react-router-dom'
import { TrendingUp, AlertTriangle, CheckCircle, Zap } from 'lucide-react'
import { ReplayStep } from '../../types/api.types'

interface InsightsPanelProps {
  steps: ReplayStep[]
  currentStep: number
  summary?: any
}

export const InsightsPanel: React.FC<InsightsPanelProps> = ({
  steps,
  currentStep,
  summary,
}) => {
  const navigate = useNavigate()
  const { sessionId } = useParams<{ sessionId: string }>()

  const minRisk = Math.min(...steps.map((s) => s.risk_score))
  const maxRisk = Math.max(...steps.map((s) => s.risk_score))
  const avgRisk =
    steps.reduce((sum, s) => sum + s.risk_score, 0) / steps.length
  const riskIncrease = maxRisk - minRisk
  const criticalThreshold = 0.5
  const criticalCrossedAt = steps.findIndex((s) => s.risk_score > criticalThreshold)
  const denyCount = steps.filter((s) => s.decision === 'DENY').length
  const lastDenyStep = steps.findLast((s) => s.decision === 'DENY')

  const container = {
    hidden: { opacity: 0 },
    show: {
      opacity: 1,
      transition: {
        staggerChildren: 0.1,
      },
    },
  }

  const item = {
    hidden: { opacity: 0, x: -10 },
    show: { opacity: 1, x: 0 },
  }

  return (
    <div className="space-y-6">
      <h3 className="text-lg font-bold text-white flex items-center gap-2">
        <Zap size={20} className="text-yellow-400" />
        Analysis & Insights
      </h3>

      <motion.div
        variants={container}
        initial="hidden"
        animate="show"
        className="space-y-4"
      >
        {/* Risk Progression */}
        <motion.div
          variants={item}
          className="bg-slate-800/30 border border-slate-700/50 rounded-lg p-4 space-y-3"
        >
          <div className="flex items-center gap-2">
            <TrendingUp size={18} className="text-blue-400" />
            <h4 className="font-semibold text-white">Risk Progression</h4>
          </div>
          <div className="grid grid-cols-3 gap-3">
            <div>
              <div className="text-xs text-slate-400">Started At</div>
              <div className="text-lg font-bold text-green-400">
                {(steps[0].risk_score * 100).toFixed(0)}%
              </div>
            </div>
            <div>
              <div className="text-xs text-slate-400">Increased By</div>
              <div className="text-lg font-bold text-orange-400">
                +{(riskIncrease * 100).toFixed(0)}%
              </div>
            </div>
            <div>
              <div className="text-xs text-slate-400">Ended At</div>
              <div className="text-lg font-bold text-red-400">
                {(steps[steps.length - 1].risk_score * 100).toFixed(0)}%
              </div>
            </div>
          </div>
          <p className="text-sm text-slate-300">
            Risk progressed steadily from {(steps[0].risk_score * 100).toFixed(0)}%
            {' '} to {(steps[steps.length - 1].risk_score * 100).toFixed(0)}%,
            {' '} indicating increasing security concerns throughout the session.
          </p>
        </motion.div>

        {/* Critical Threshold */}
        {criticalCrossedAt !== -1 && (
          <motion.div
            variants={item}
            className="bg-red-900/20 border border-red-500/50 rounded-lg p-4 space-y-3"
          >
            <div className="flex items-center gap-2">
              <AlertTriangle size={18} className="text-red-400" />
              <h4 className="font-semibold text-white">Critical Threshold Crossed</h4>
            </div>
            <p className="text-sm text-slate-300">
              Risk exceeded <span className="font-bold text-red-400">50%</span> at{' '}
              <span className="font-bold text-cyan-400">
                Step {criticalCrossedAt + 1}
              </span>
              . This marks a significant shift in threat level.
            </p>
          </motion.div>
        )}

        {/* Denial Summary */}
        {denyCount > 0 && (
          <motion.div
            variants={item}
            className="bg-red-900/20 border border-red-500/50 rounded-lg p-4 space-y-3"
          >
            <div className="flex items-center gap-2">
              <AlertTriangle size={18} className="text-red-400" />
              <h4 className="font-semibold text-white">Access Denied</h4>
            </div>
            <div className="space-y-2 text-sm text-slate-300">
              <p>
                Found <span className="font-bold text-red-400">{denyCount}</span> denial
                {denyCount > 1 ? 's' : ''} in the session.
              </p>
              {lastDenyStep && (
                <>
                  <p>
                    Final denial at{' '}
                    <span className="font-bold text-cyan-400">
                      Step {lastDenyStep.step}
                    </span>
                    {' '} with risk score{' '}
                    <span className="font-bold text-red-400">
                      {(lastDenyStep.risk_score * 100).toFixed(0)}%
                    </span>
                  </p>
                  {lastDenyStep.rule_id && (
                    <p>
                      Triggered by rule:{' '}
                      <span className="font-mono text-cyan-400 text-xs">
                        {lastDenyStep.rule_id}
                      </span>
                    </p>
                  )}
                </>
              )}
            </div>
          </motion.div>
        )}

        {/* Current Session State */}
        <motion.div
          variants={item}
          className="bg-slate-800/30 border border-slate-700/50 rounded-lg p-4 space-y-3"
        >
          <div className="flex items-center gap-2">
            <CheckCircle size={18} className="text-green-400" />
            <h4 className="font-semibold text-white">Current State</h4>
          </div>
          <div className="grid grid-cols-2 gap-3 text-sm">
            <div>
              <div className="text-slate-400">Current Step</div>
              <div className="text-lg font-bold text-white">
                {currentStep + 1} / {steps.length}
              </div>
            </div>
            <div>
              <div className="text-slate-400">Current Risk</div>
              <div className={`text-lg font-bold ${
                steps[currentStep].risk_score > 0.7 ? 'text-red-400' :
                steps[currentStep].risk_score > 0.4 ? 'text-orange-400' :
                'text-green-400'
              }`}>
                {(steps[currentStep].risk_score * 100).toFixed(0)}%
              </div>
            </div>
          </div>
        </motion.div>

        {/* Recommendations */}
        <motion.div
          variants={item}
          className="bg-slate-800/30 border border-slate-700/50 rounded-lg p-4 space-y-3"
        >
          <div className="flex items-center gap-2">
            <Zap size={18} className="text-yellow-400" />
            <h4 className="font-semibold text-white">Next Steps</h4>
          </div>
          <ul className="space-y-2 text-sm text-slate-300">
            <li className="flex items-start gap-2">
              <span className="text-cyan-400 font-bold mt-0.5">→</span>
              <span>Review the policy rules applied at critical decision points</span>
            </li>
            <li className="flex items-start gap-2">
              <span className="text-cyan-400 font-bold mt-0.5">→</span>
              <span>Run a what-if simulation to test alternative policy configurations</span>
            </li>
            <li className="flex items-start gap-2">
              <span className="text-cyan-400 font-bold mt-0.5">→</span>
              <span>Export this analysis for audit and compliance documentation</span>
            </li>
          </ul>
        </motion.div>

        {/* Simulation CTA */}
        <motion.button
          variants={item}
          whileHover={{ scale: 1.02 }}
          whileTap={{ scale: 0.98 }}
          onClick={() => navigate(`/simulation/${sessionId || 'default'}`)}
          className="w-full py-3 px-4 bg-gradient-to-r from-cyan-600 to-blue-600 hover:from-cyan-700 hover:to-blue-700 rounded-lg text-white font-semibold transition-all shadow-lg shadow-cyan-500/20"
        >
          🧪 Run What-If Simulation
        </motion.button>
      </motion.div>
    </div>
  )
}
