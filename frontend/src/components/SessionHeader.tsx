import React from 'react'
import { motion } from 'framer-motion'
import { ChevronLeft, AlertTriangle, CheckCircle, AlertCircle } from 'lucide-react'
import { useNavigate } from 'react-router-dom'

interface SessionHeaderProps {
  sessionId: string
  userId: string
  maxRisk: number
  status: string
  eventCount: number
  duration: number
}

const getStatusIcon = (status: string) => {
  switch (status) {
    case 'compromised':
      return <AlertTriangle className="w-5 h-5 text-red-400" />
    case 'suspicious':
      return <AlertCircle className="w-5 h-5 text-orange-400" />
    case 'active':
      return <CheckCircle className="w-5 h-5 text-green-400" />
    default:
      return null
  }
}

const getStatusColorClass = (status: string) => {
  switch (status) {
    case 'compromised':
      return 'bg-red-900/20 border-red-500/50 text-red-300'
    case 'suspicious':
      return 'bg-orange-900/20 border-orange-500/50 text-orange-300'
    case 'active':
      return 'bg-green-900/20 border-green-500/50 text-green-300'
    default:
      return 'bg-slate-800/20 border-slate-500/50 text-slate-300'
  }
}

const getRiskColorClass = (risk: number) => {
  if (risk >= 0.7) return 'text-red-400'
  if (risk >= 0.4) return 'text-orange-400'
  return 'text-green-400'
}

export const SessionHeader: React.FC<SessionHeaderProps> = ({
  sessionId,
  userId,
  maxRisk,
  status,
  eventCount,
  duration,
}) => {
  const navigate = useNavigate()

  return (
    <motion.div
      initial={{ opacity: 0, y: -20 }}
      animate={{ opacity: 1, y: 0 }}
      className="bg-gradient-to-b from-slate-900/95 to-slate-900/60 border-b-2 border-slate-700/70 sticky top-0 z-40 backdrop-blur shadow-2xl"
    >
      <div className="px-8 py-6 space-y-5">
        <div className="flex items-center gap-4 pb-3 border-b border-slate-700/30">
          <button onClick={() => navigate('/sessions')} className="p-2 hover:bg-slate-800/50 rounded-lg transition group flex-shrink-0">
            <ChevronLeft className="w-6 h-6 text-slate-400 group-hover:text-white" />
          </button>
          <div className="flex-1">
            <h1 className="text-3xl font-bold text-white">Session Details</h1>
            <p className="text-xs text-slate-500 mt-1 font-mono tracking-wide">{sessionId}</p>
          </div>
        </div>

        <div className="pt-2">
          <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-4">
            <SummaryCard label="Session" value={sessionId.slice(0, 8) + '...'} title={sessionId} />
            <SummaryCard label="User" value={userId.split('_').pop() || userId} />
            <SummaryCard label="Max Risk" value={`${(maxRisk * 100).toFixed(0)}%`} highlighted riskColor={getRiskColorClass(maxRisk)} />
            <StatusCard status={status} icon={getStatusIcon(status)} colorClass={getStatusColorClass(status)} />
            <SummaryCard label="Events" value={eventCount.toString()} />
            <SummaryCard label="Duration" value={`${(duration / 1000).toFixed(1)}s`} />
          </div>
        </div>

        <motion.div initial={{ scaleX: 0 }} animate={{ scaleX: 1 }} transition={{ delay: 0.2 }} className="h-2 bg-gradient-to-r from-green-500 via-yellow-500 to-red-500 rounded-full mt-4" style={{ width: `${maxRisk * 100}%` }} />
      </div>
    </motion.div>
  )
}

const SummaryCard: React.FC<{ label: string; value: string; title?: string; highlighted?: boolean; riskColor?: string }> = ({ label, value, title, highlighted, riskColor }) => {
  const baseClass = 'px-3 py-2 rounded-lg border transition-all'
  const highlightedClass = 'bg-slate-800/50 border-slate-600/50 ring-1 ring-cyan-500/30'
  const defaultClass = 'bg-slate-800/30 border-slate-700/50 hover:bg-slate-800/50'
  const className = highlighted ? highlightedClass : defaultClass
  return (
    <div className={`${baseClass} ${className}`} title={title || value}>
      <div className="text-xs font-semibold text-slate-400 mb-1">{label}</div>
      <div className={`text-sm font-bold text-white truncate ${riskColor || ''}`}>{value}</div>
    </div>
  )
}

const StatusCard: React.FC<{ status: string; icon: React.ReactNode; colorClass: string }> = ({ status, icon, colorClass }) => {
  return (
    <div className={`px-3 py-2 rounded-lg border ${colorClass} flex items-center gap-2`}>
      <div className="text-xs font-semibold text-slate-400">Status</div>
      <div className="flex items-center gap-2">
        {icon}
        <span className="text-sm font-semibold capitalize">{status}</span>
      </div>
    </div>
  )
}
