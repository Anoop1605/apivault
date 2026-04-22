import React from 'react'
import { Clock, AlertTriangle } from 'lucide-react'

interface ReplayHeaderProps {
  sessionId: string
}

export const ReplayHeader: React.FC<ReplayHeaderProps> = ({ sessionId }) => {
  return (
    <div className="space-y-3">
      <div className="flex items-center gap-2">
        <span className="text-slate-400 text-sm">Session ID:</span>
        <span className="font-mono text-sm text-cyan-400">{sessionId}</span>
      </div>
      <div className="grid grid-cols-3 gap-4">
        <div className="bg-slate-800/50 border border-slate-700/30 rounded-lg p-3">
          <div className="text-xs text-slate-400 flex items-center gap-2">
            <Clock size={14} />
            Time
          </div>
          <div className="text-sm font-semibold text-white mt-1">
            2:34
          </div>
        </div>
        <div className="bg-slate-800/50 border border-slate-700/30 rounded-lg p-3">
          <div className="text-xs text-slate-400">Risk Level</div>
          <div className="text-sm font-semibold text-orange-400 mt-1">
            82%
          </div>
        </div>
        <div className="bg-slate-800/50 border border-slate-700/30 rounded-lg p-3">
          <div className="text-xs text-slate-400 flex items-center gap-2">
            <AlertTriangle size={14} />
            Status
          </div>
          <div className="text-sm font-semibold text-red-400 mt-1">
            DENIED
          </div>
        </div>
      </div>
    </div>
  )
}
