import { motion } from 'framer-motion'
import { Database, ChevronRight, AlertTriangle } from 'lucide-react'

interface Session {
  sessionId: string
  eventCount: number
  status: 'SAFE' | 'REVIEW' | 'BLOCKED'
  timestamp: string
  riskScore: number
}

interface SessionsPreviewProps {
  sessions?: Session[]
  loading?: boolean
  onViewAll?: () => void
}

const defaultSessions: Session[] = [
  {
    sessionId: 'sess-001',
    eventCount: 12,
    status: 'SAFE',
    timestamp: '2 min ago',
    riskScore: 0.15,
  },
  {
    sessionId: 'sess-002',
    eventCount: 8,
    status: 'BLOCKED',
    timestamp: '15 min ago',
    riskScore: 0.92,
  },
  {
    sessionId: 'sess-003',
    eventCount: 15,
    status: 'SAFE',
    timestamp: '1 hour ago',
    riskScore: 0.22,
  },
  {
    sessionId: 'sess-004',
    eventCount: 6,
    status: 'REVIEW',
    timestamp: '2 hours ago',
    riskScore: 0.58,
  },
  {
    sessionId: 'sess-005',
    eventCount: 20,
    status: 'SAFE',
    timestamp: '3 hours ago',
    riskScore: 0.08,
  },
]

const statusConfig = {
  SAFE: {
    bg: 'bg-green-500/10',
    border: 'border-green-500/30',
    badge: 'bg-green-500/20 text-green-300',
    icon: '✓',
  },
  REVIEW: {
    bg: 'bg-orange-500/10',
    border: 'border-orange-500/30',
    badge: 'bg-orange-500/20 text-orange-300',
    icon: '!',
  },
  BLOCKED: {
    bg: 'bg-red-500/10',
    border: 'border-red-500/30',
    badge: 'bg-red-500/20 text-red-300',
    icon: '✕',
  },
}

const SessionsPreview = ({
  sessions = defaultSessions,
  loading = false,
  onViewAll,
}: SessionsPreviewProps) => {
  const containerVariants = {
    hidden: { opacity: 0, y: 20 },
    visible: {
      opacity: 1,
      y: 0,
      transition: {
        duration: 0.4,
        staggerChildren: 0.05,
        delayChildren: 0.45,
      },
    },
  }

  const itemVariants = {
    hidden: { opacity: 0, y: 10 },
    visible: { opacity: 1, y: 0 },
  }

  return (
    <motion.div
      className="bg-slate-800/50 border border-slate-700 rounded-xl p-6 lg:col-span-2"
      variants={containerVariants}
      initial="hidden"
      animate="visible"
    >
      {/* Header */}
      <div className="flex items-center justify-between mb-6">
        <div className="flex items-center gap-2">
          <Database size={20} className="text-slate-300" />
          <h2 className="text-lg font-semibold text-white">Recent Sessions</h2>
        </div>
        <motion.button
          onClick={onViewAll}
          whileHover={{ scale: 1.05 }}
          className="text-blue-400 hover:text-blue-300 text-sm font-medium flex items-center gap-1"
        >
          View All <ChevronRight size={16} />
        </motion.button>
      </div>

      {/* Sessions Table */}
      <motion.div className="overflow-x-auto" variants={containerVariants}>
        {loading ? (
          <motion.div
            animate={{ opacity: [0.5, 1, 0.5] }}
            transition={{ repeat: Infinity, duration: 1.5 }}
            className="text-center py-8 text-slate-400"
          >
            Loading sessions...
          </motion.div>
        ) : sessions.length === 0 ? (
          <div className="text-center py-8 text-slate-400">No sessions yet</div>
        ) : (
          <div className="space-y-2">
            {/* Table Header */}
            <div className="grid grid-cols-12 gap-4 px-4 py-2 text-xs font-semibold text-slate-400 uppercase tracking-wide">
              <div className="col-span-4">Session ID</div>
              <div className="col-span-2">Events</div>
              <div className="col-span-2">Risk Score</div>
              <div className="col-span-2">Status</div>
              <div className="col-span-2">Time</div>
            </div>

            {/* Table Rows */}
            <motion.div className="space-y-1" variants={containerVariants}>
              {sessions.map((session, idx) => {
                const config = statusConfig[session.status]
                const riskColor =
                  session.riskScore > 0.7
                    ? 'text-red-400'
                    : session.riskScore > 0.4
                      ? 'text-orange-400'
                      : 'text-green-400'

                return (
                  <motion.div
                    key={session.sessionId}
                    variants={itemVariants}
                    whileHover={{
                      backgroundColor: 'rgba(30, 41, 59, 0.6)',
                      x: 5,
                    }}
                    className={`${config.bg} ${config.border} border grid grid-cols-12 gap-4 px-4 py-3 rounded-lg cursor-pointer transition-all group`}
                  >
                    {/* Session ID */}
                    <div className="col-span-4">
                      <p className="text-sm text-white font-mono group-hover:text-blue-300 transition-colors truncate">
                        {session.sessionId}
                      </p>
                    </div>

                    {/* Events */}
                    <div className="col-span-2">
                      <motion.span
                        className="inline-block text-sm font-semibold text-slate-300 group-hover:text-white"
                        whileHover={{ scale: 1.1 }}
                      >
                        {session.eventCount}
                      </motion.span>
                    </div>

                    {/* Risk Score */}
                    <div className="col-span-2">
                      <div className="flex items-center gap-2">
                        <div className="w-16 h-1.5 bg-slate-700 rounded-full overflow-hidden">
                          <motion.div
                            className={`h-full bg-gradient-to-r from-green-500 to-red-500`}
                            initial={{ width: 0 }}
                            animate={{
                              width: `${session.riskScore * 100}%`,
                            }}
                            transition={{ duration: 0.8, delay: idx * 0.1 }}
                          />
                        </div>
                        <p className={`text-xs font-semibold ${riskColor}`}>
                          {(session.riskScore * 100).toFixed(0)}%
                        </p>
                      </div>
                    </div>

                    {/* Status */}
                    <div className="col-span-2">
                      <motion.span
                        className={`${config.badge} px-2 py-1 rounded text-xs font-semibold inline-flex items-center gap-1`}
                        whileHover={{ scale: 1.05 }}
                      >
                        <span>{config.icon}</span>
                        {session.status}
                      </motion.span>
                    </div>

                    {/* Time */}
                    <div className="col-span-2 flex items-center justify-between">
                      <p className="text-xs text-slate-400 group-hover:text-slate-300 transition-colors">
                        {session.timestamp}
                      </p>
                      <motion.div
                        initial={{ x: -5, opacity: 0 }}
                        whileHover={{ x: 5, opacity: 1 }}
                        className="text-slate-500 group-hover:text-slate-300"
                      >
                        <ChevronRight size={16} />
                      </motion.div>
                    </div>
                  </motion.div>
                )
              })}
            </motion.div>
          </div>
        )}
      </motion.div>
    </motion.div>
  )
}

export default SessionsPreview
