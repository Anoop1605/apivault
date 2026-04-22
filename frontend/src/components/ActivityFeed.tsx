import { motion } from 'framer-motion'
import { Activity, LogIn, LogOut, Lock, AlertTriangle } from 'lucide-react'

interface ActivityEvent {
  id: string
  type: 'LOGIN' | 'LOGOUT' | 'ACCESS' | 'ATTACK'
  decision: 'ALLOW' | 'BLOCK' | 'REVIEW'
  message: string
  timestamp: string
  sessionId: string
}

interface ActivityFeedProps {
  events?: ActivityEvent[]
  loading?: boolean
}

const defaultEvents: ActivityEvent[] = [
  {
    id: '1',
    type: 'LOGIN',
    decision: 'ALLOW',
    message: 'User login successful',
    timestamp: '5 min ago',
    sessionId: 'sess-001',
  },
  {
    id: '2',
    type: 'ACCESS',
    decision: 'BLOCK',
    message: 'DELETE /api/payments blocked',
    timestamp: '8 min ago',
    sessionId: 'sess-002',
  },
  {
    id: '3',
    type: 'ACCESS',
    decision: 'ALLOW',
    message: 'GET /api/users allowed',
    timestamp: '12 min ago',
    sessionId: 'sess-001',
  },
  {
    id: '4',
    type: 'ATTACK',
    decision: 'BLOCK',
    message: 'SQL injection attempt blocked',
    timestamp: '15 min ago',
    sessionId: 'sess-003',
  },
  {
    id: '5',
    type: 'ACCESS',
    decision: 'REVIEW',
    message: 'Suspicious pattern detected',
    timestamp: '18 min ago',
    sessionId: 'sess-004',
  },
]

const eventConfig = {
  LOGIN: { icon: LogIn, color: 'text-green-400', bg: 'bg-green-500/10' },
  LOGOUT: { icon: LogOut, color: 'text-slate-400', bg: 'bg-slate-500/10' },
  ACCESS: { icon: Lock, color: 'text-blue-400', bg: 'bg-blue-500/10' },
  ATTACK: { icon: AlertTriangle, color: 'text-red-400', bg: 'bg-red-500/10' },
}

const decisionConfig = {
  ALLOW: {
    badge: 'bg-green-500/20 text-green-300 border-green-500/30',
    dot: 'bg-green-500',
  },
  BLOCK: {
    badge: 'bg-red-500/20 text-red-300 border-red-500/30',
    dot: 'bg-red-500',
  },
  REVIEW: {
    badge: 'bg-orange-500/20 text-orange-300 border-orange-500/30',
    dot: 'bg-orange-500',
  },
}

const ActivityFeed = ({ events = defaultEvents, loading = false }: ActivityFeedProps) => {
  const containerVariants = {
    hidden: { opacity: 0, y: 20 },
    visible: {
      opacity: 1,
      y: 0,
      transition: {
        duration: 0.4,
        staggerChildren: 0.06,
        delayChildren: 0.35,
      },
    },
  }

  const itemVariants = {
    hidden: { opacity: 0, y: 10 },
    visible: { opacity: 1, y: 0 },
  }

  return (
    <motion.div
      className="bg-slate-800/50 border border-slate-700 rounded-xl p-6"
      variants={containerVariants}
      initial="hidden"
      animate="visible"
    >
      {/* Header */}
      <div className="flex items-center gap-2 mb-6">
        <Activity size={20} className="text-blue-400" />
        <h2 className="text-lg font-semibold text-white">Recent Activity</h2>
      </div>

      {/* Timeline */}
      <motion.div className="space-y-4" variants={containerVariants}>
        {loading ? (
          <motion.div
            animate={{ opacity: [0.5, 1, 0.5] }}
            transition={{ repeat: Infinity, duration: 1.5 }}
            className="text-center py-8 text-slate-400"
          >
            Loading activity...
          </motion.div>
        ) : events.length === 0 ? (
          <div className="text-center py-8 text-slate-400">No activity yet</div>
        ) : (
          events.map((event, idx) => {
            const eventCfg = eventConfig[event.type]
            const decisionCfg = decisionConfig[event.decision]
            const Icon = eventCfg.icon

            return (
              <motion.div
                key={event.id}
                variants={itemVariants}
                className="flex gap-4 group cursor-pointer"
              >
                {/* Timeline Line */}
                <div className="flex flex-col items-center">
                  {/* Icon */}
                  <motion.div
                    className={`${eventCfg.bg} p-2 rounded-lg group-hover:scale-110 transition-transform`}
                    whileHover={{
                      scale: 1.15,
                      rotate: [0, 5, -5, 0],
                    }}
                  >
                    <Icon size={18} className={eventCfg.color} />
                  </motion.div>

                  {/* Connector Line */}
                  {idx < events.length - 1 && (
                    <div className="w-0.5 h-8 bg-slate-700 my-2" />
                  )}
                </div>

                {/* Content */}
                <motion.div
                  className="flex-1 pt-1 group-hover:bg-slate-900/50 p-3 rounded-lg transition-colors"
                  whileHover={{ x: 5 }}
                >
                  <div className="flex items-start justify-between gap-2">
                    <div className="flex-1 min-w-0">
                      <p className="text-sm text-white group-hover:text-slate-100 transition-colors font-medium">
                        {event.message}
                      </p>
                      <div className="flex items-center gap-2 mt-1">
                        <p className="text-xs text-slate-400">
                          {event.sessionId}
                        </p>
                        <span className="text-xs text-slate-500">•</span>
                        <p className="text-xs text-slate-400">{event.timestamp}</p>
                      </div>
                    </div>

                    {/* Decision Badge */}
                    <motion.span
                      className={`${decisionCfg.badge} px-2 py-1 rounded text-xs font-semibold border flex-shrink-0 flex items-center gap-1`}
                      whileHover={{ scale: 1.05 }}
                    >
                      <motion.div
                        className={`${decisionCfg.dot} w-1.5 h-1.5 rounded-full`}
                        animate={{ scale: [1, 1.3, 1] }}
                        transition={{
                          repeat: Infinity,
                          duration: 2,
                          delay: idx * 0.1,
                        }}
                      />
                      {event.decision}
                    </motion.span>
                  </div>
                </motion.div>
              </motion.div>
            )
          })
        )}
      </motion.div>
    </motion.div>
  )
}

export default ActivityFeed
