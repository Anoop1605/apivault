import React from 'react'
import { Play, Pause, SkipForward, SkipBack, RotateCcw } from 'lucide-react'
import { motion } from 'framer-motion'

interface ControlsProps {
  isPlaying: boolean
  currentStep: number
  totalSteps: number
  onPlay: () => void
  onPause: () => void
  onNext: () => void
  onPrev: () => void
  onRestart: () => void
  onJumpTo: (step: number) => void
}

export const Controls: React.FC<ControlsProps> = ({
  isPlaying,
  currentStep,
  totalSteps,
  onPlay,
  onPause,
  onNext,
  onPrev,
  onRestart,
  onJumpTo,
}) => {
  return (
    <div className="flex items-center gap-4 justify-between">
      <div className="flex items-center gap-2">
        {/* Play/Pause */}
        <motion.button
          whileHover={{ scale: 1.05 }}
          whileTap={{ scale: 0.95 }}
          onClick={isPlaying ? onPause : onPlay}
          className={`p-2.5 rounded-lg transition-all ${
            isPlaying
              ? 'bg-blue-600 text-white hover:bg-blue-700'
              : 'bg-slate-700 text-slate-200 hover:bg-slate-600'
          }`}
        >
          {isPlaying ? <Pause size={18} /> : <Play size={18} />}
        </motion.button>

        {/* Previous */}
        <motion.button
          whileHover={{ scale: 1.05 }}
          whileTap={{ scale: 0.95 }}
          onClick={onPrev}
          disabled={currentStep === 0}
          className="p-2.5 rounded-lg bg-slate-700 text-slate-200 hover:bg-slate-600 disabled:opacity-50 disabled:cursor-not-allowed transition-all"
        >
          <SkipBack size={18} />
        </motion.button>

        {/* Next */}
        <motion.button
          whileHover={{ scale: 1.05 }}
          whileTap={{ scale: 0.95 }}
          onClick={onNext}
          disabled={currentStep >= totalSteps - 1}
          className="p-2.5 rounded-lg bg-slate-700 text-slate-200 hover:bg-slate-600 disabled:opacity-50 disabled:cursor-not-allowed transition-all"
        >
          <SkipForward size={18} />
        </motion.button>

        {/* Restart */}
        <motion.button
          whileHover={{ scale: 1.05 }}
          whileTap={{ scale: 0.95 }}
          onClick={onRestart}
          className="p-2.5 rounded-lg bg-slate-700 text-slate-200 hover:bg-slate-600 transition-all"
        >
          <RotateCcw size={18} />
        </motion.button>
      </div>

      {/* Step Display */}
      <div className="flex items-center gap-4">
        <div className="text-sm text-slate-300">
          Step <span className="font-bold text-white">{currentStep + 1}</span> / <span className="font-semibold">{totalSteps}</span>
        </div>

        {/* Progress Bar */}
        <div className="w-48 h-2 bg-slate-700/50 rounded-full overflow-hidden border border-slate-600/30">
          <motion.div
            initial={{ width: '0%' }}
            animate={{ width: `${((currentStep + 1) / totalSteps) * 100}%` }}
            transition={{ duration: 0.3 }}
            className="h-full bg-gradient-to-r from-cyan-500 to-blue-500"
          />
        </div>

        {/* Speed Control */}
        <div className="flex items-center gap-2 pl-4 border-l border-slate-600/30">
          <span className="text-xs text-slate-400">Speed:</span>
          <select className="bg-slate-700 text-slate-200 text-xs px-2 py-1 rounded border border-slate-600/50 focus:outline-none focus:border-cyan-500">
            <option>1x</option>
            <option>1.5x</option>
            <option>2x</option>
          </select>
        </div>
      </div>
    </div>
  )
}
