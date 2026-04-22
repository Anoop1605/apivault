import React from 'react'
import { motion } from 'framer-motion'
import { Play, Zap, Download } from 'lucide-react'

interface ActionBarProps {
  sessionId: string
  onReplay: () => void
  onSimulate: () => void
}

export const ActionBar: React.FC<ActionBarProps> = ({
  sessionId,
  onReplay,
  onSimulate,
}) => {
  return (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ delay: 0.5 }}
      className="bg-gradient-to-t from-slate-950/95 via-slate-950/80 to-slate-950/60 border-t-2 border-slate-700/50 backdrop-blur sticky bottom-0 z-40 shadow-2xl"
    >
      <div className="px-8 py-6 flex flex-wrap items-center gap-4">
        {/* Primary Action: Replay */}
        <motion.button
          whileHover={{ scale: 1.05 }}
          whileTap={{ scale: 0.98 }}
          onClick={onReplay}
          className="flex items-center gap-2 px-4 py-2.5 rounded-lg bg-gradient-to-r from-cyan-600 to-cyan-500 text-white font-semibold hover:from-cyan-500 hover:to-cyan-400 transition-all shadow-lg hover:shadow-cyan-500/50"
        >
          <Play className="w-5 h-5" />
          Replay Session
        </motion.button>

        {/* Secondary Action: Simulate */}
        <motion.button
          whileHover={{ scale: 1.05 }}
          whileTap={{ scale: 0.98 }}
          onClick={onSimulate}
          className="flex items-center gap-2 px-4 py-2.5 rounded-lg bg-slate-800/50 border border-slate-600/50 text-slate-200 font-semibold hover:bg-slate-700/50 hover:border-slate-500/50 transition-all"
        >
          <Zap className="w-5 h-5" />
          Simulate Policy
        </motion.button>

        {/* Secondary Action: Download */}
        <motion.button
          whileHover={{ scale: 1.05 }}
          whileTap={{ scale: 0.98 }}
          className="flex items-center gap-2 px-4 py-2.5 rounded-lg bg-slate-800/30 border border-slate-700/50 text-slate-300 font-semibold hover:bg-slate-800/50 transition-all"
        >
          <Download className="w-5 h-5" />
          Export Report
        </motion.button>

        {/* Info Text */}
        <div className="ml-auto text-xs text-slate-400 hidden md:block">
          Session: <code className="bg-slate-800/50 px-2 py-1 rounded">{sessionId}</code>
        </div>
      </div>
    </motion.div>
  )
}
