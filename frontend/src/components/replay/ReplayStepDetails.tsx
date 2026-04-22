import React from 'react'
import { motion } from 'framer-motion'
import { CheckCircle, XCircle, AlertCircle, Clock, User, Zap } from 'lucide-react'
import { ReplayStep } from '../../types/api.types'

interface ReplayStepDetailsProps {
  step: ReplayStep
}

export const ReplayStepDetails: React.FC<ReplayStepDetailsProps> = ({ step }) => {
  const getDecisionColor = (decision: string) => {
    switch (decision) {
      case 'ALLOW':
        return 'text-green-400'
      case 'DENY':
        return 'text-red-400'
      case 'FLAG':
        return 'text-orange-400'
      default:
        return 'text-slate-400'
    }
  }

  const getDecisionBg = (decision: string) => {
    switch (decision) {
      case 'ALLOW':
        return 'bg-green-900/30 border border-green-500/50'
      case 'DENY':
        return 'bg-red-900/30 border border-red-500/50'
      case 'FLAG':
        return 'bg-orange-900/30 border border-orange-500/50'
      default:
        return 'bg-slate-800/30 border border-slate-500/50'
    }
  }

  return (
    <div className="space-y-6">
      {/* Decision Badge */}
      <motion.div
        initial={{ opacity: 0, y: 10 }}
        animate={{ opacity: 1, y: 0 }}
        className={`p-4 rounded-lg ${getDecisionBg(step.decision)}`}
      >
        <div className="flex items-center gap-3">
          {step.decision === 'ALLOW' && <CheckCircle size={24} className="text-green-400" />}
          {step.decision === 'DENY' && <XCircle size={24} className="text-red-400" />}
          {step.decision === 'FLAG' && <AlertCircle size={24} className="text-orange-400" />}
          <div>
            <div className={`text-lg font-bold ${getDecisionColor(step.decision)}`}>
              {step.event_type}
            </div>
            <div className="text-sm text-slate-300">
              Decision: <span className={`font-semibold ${getDecisionColor(step.decision)}`}>{step.decision}</span>
            </div>
          </div>
        </div>
      </motion.div>

      {/* Basic Info */}
      <motion.div
        initial={{ opacity: 0, y: 10 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.1 }}
        className="bg-slate-800/30 border border-slate-700/50 rounded-lg p-4 space-y-3"
      >
        <h3 className="text-sm font-semibold text-white uppercase tracking-wider">
          Request Details
        </h3>
        {step.endpoint && (
          <div className="flex items-start gap-3">
            <Zap size={16} className="text-cyan-400 flex-shrink-0 mt-1" />
            <div>
              <div className="text-xs text-slate-400">Endpoint</div>
              <div className="font-mono text-sm text-slate-200">{step.endpoint}</div>
            </div>
          </div>
        )}
        {step.method && (
          <div className="flex items-start gap-3">
            <Zap size={16} className="text-slate-400 flex-shrink-0 mt-1" />
            <div>
              <div className="text-xs text-slate-400">Method</div>
              <div className={`font-semibold text-sm ${
                step.method === 'GET' ? 'text-blue-400' :
                step.method === 'POST' ? 'text-green-400' :
                step.method === 'DELETE' ? 'text-red-400' :
                'text-slate-300'
              }`}>
                {step.method}
              </div>
            </div>
          </div>
        )}
        {step.user_id && (
          <div className="flex items-start gap-3">
            <User size={16} className="text-slate-400 flex-shrink-0 mt-1" />
            <div>
              <div className="text-xs text-slate-400">User ID</div>
              <div className="font-mono text-sm text-slate-200">{step.user_id}</div>
            </div>
          </div>
        )}
        {step.ip_address && (
          <div className="flex items-start gap-3">
            <Zap size={16} className="text-slate-400 flex-shrink-0 mt-1" />
            <div>
              <div className="text-xs text-slate-400">IP Address</div>
              <div className="font-mono text-sm text-slate-200">{step.ip_address}</div>
            </div>
          </div>
        )}
        {step.timestamp && (
          <div className="flex items-start gap-3">
            <Clock size={16} className="text-slate-400 flex-shrink-0 mt-1" />
            <div>
              <div className="text-xs text-slate-400">Timestamp</div>
              <div className="text-sm text-slate-200">
                {new Date(step.timestamp).toLocaleString()}
              </div>
            </div>
          </div>
        )}
      </motion.div>

      {/* Risk Score */}
      <motion.div
        initial={{ opacity: 0, y: 10 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.2 }}
        className="bg-slate-800/30 border border-slate-700/50 rounded-lg p-4 space-y-3"
      >
        <h3 className="text-sm font-semibold text-white uppercase tracking-wider">
          Risk Analysis
        </h3>
        <div className="space-y-2">
          <div className="flex justify-between items-center">
            <span className="text-slate-300">Risk Score</span>
            <span className={`font-bold text-lg ${
              step.risk_score > 0.7 ? 'text-red-400' :
              step.risk_score > 0.4 ? 'text-orange-400' :
              'text-green-400'
            }`}>
              {(step.risk_score * 100).toFixed(0)}%
            </span>
          </div>
          <div className="h-2 bg-slate-700/50 rounded-full overflow-hidden">
            <motion.div
              initial={{ width: 0 }}
              animate={{ width: `${step.risk_score * 100}%` }}
              transition={{ duration: 0.5 }}
              className={`h-full ${
                step.risk_score > 0.7 ? 'bg-gradient-to-r from-red-500 to-red-600' :
                step.risk_score > 0.4 ? 'bg-gradient-to-r from-orange-500 to-orange-600' :
                'bg-gradient-to-r from-green-500 to-green-600'
              }`}
            />
          </div>
        </div>
      </motion.div>

      {/* Rule Information */}
      {step.rule_id && (
        <motion.div
          initial={{ opacity: 0, y: 10 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.3 }}
          className="bg-slate-800/30 border border-slate-700/50 rounded-lg p-4 space-y-3"
        >
          <h3 className="text-sm font-semibold text-white uppercase tracking-wider">
            Applied Rule
          </h3>
          <div className="font-mono text-sm text-cyan-400 break-all">
            {step.rule_id}
          </div>
        </motion.div>
      )}

      {/* Conditions */}
      {step.conditions && (
        <motion.div
          initial={{ opacity: 0, y: 10 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.4 }}
          className="bg-slate-800/30 border border-slate-700/50 rounded-lg p-4 space-y-3"
        >
          <h3 className="text-sm font-semibold text-white uppercase tracking-wider">
            Policy Conditions
          </h3>
          
          {/* Passed Conditions */}
          {step.conditions.passed.length > 0 && (
            <div>
              <div className="text-xs text-green-400 font-semibold mb-2">Passed</div>
              <div className="space-y-1">
                {step.conditions.passed.map((condition) => (
                  <div key={condition} className="flex items-center gap-2 text-sm">
                    <CheckCircle size={14} className="text-green-400" />
                    <span className="text-slate-300 capitalize">
                      {condition.replace(/_/g, ' ')}
                    </span>
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* Failed Conditions */}
          {step.conditions.failed.length > 0 && (
            <div className="pt-2 border-t border-slate-700/50">
              <div className="text-xs text-red-400 font-semibold mb-2">Failed</div>
              <div className="space-y-1">
                {step.conditions.failed.map((condition) => (
                  <div key={condition} className="flex items-center gap-2 text-sm">
                    <XCircle size={14} className="text-red-400" />
                    <span className="text-slate-300 capitalize">
                      {condition.replace(/_/g, ' ')}
                    </span>
                  </div>
                ))}
              </div>
            </div>
          )}
        </motion.div>
      )}
    </div>
  )
}
