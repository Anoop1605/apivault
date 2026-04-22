import { motion } from 'framer-motion'
import { AlertTriangle, ArrowRight, AlertCircle } from 'lucide-react'

interface Alert {
  id: string
  sessionId: string
  message: string
  severity: 'HIGH' | 'CRITICAL'
  timestamp: string
}

interface AlertsPreviewProps {
  alerts?: Alert[]
  loading?: boolean
  onViewAll?: () => void
}

const defaultAlerts: Alert[] = [
  {
    id: '1',
    sessionId: 'sess-001',
    message: 'Unauthorized DELETE attempt detected',
    severity: 'HIGH',
    timestamp: '2 min ago',
  },
  {
    id: '2',
    sessionId: 'sess-002',
    message: 'Multiple failed login attempts',
    severity: 'CRITICAL',
    timestamp: '15 min ago',
  },
  {
    id: '3',
    sessionId: 'sess-003',
    message: 'Suspicious API call pattern',
    severity: 'HIGH',
    timestamp: '1 hour ago',
  },
  {
    id: '4',
    sessionId: 'sess-004',
    message: 'Policy violation: Access denied',
    severity: 'HIGH',
    timestamp: '2 hours ago',
  },
  {
    id: '5',
    sessionId: 'sess-005',
    message: 'Rate limit exceeded',
    severity: 'CRITICAL',
    timestamp: '3 hours ago',
  },
]

const severityConfig = {
  HIGH: {
    bg: 'bg-orange-500/10',
    border: 'border-orange-500/30',
    badge: 'bg-orange-500/20 text-orange-300',
    dot: 'bg-orange-500',
  },
  CRITICAL: {
    bg: 'bg-red-500/10',
    border: 'border-red-500/30',
    badge: 'bg-red-500/20 text-red-300',
    dot: 'bg-red-500',
  },
}

const AlertsPreview = ({
  alerts = defaultAlerts,
  loading = false,
  onViewAll,
}: AlertsPreviewProps) => {
  const containerVariants = {
    hidden: { opacity: 0, y: 20 },
    visible: {
      opacity: 1,
      y: 0,
      transition: {
        duration: 0.4,
        staggerChildren: 0.05,
        delayChildren: 0.4,
      },
    },
  }

  const itemVariants = {
    hidden: { opacity: 0, x: -10 },
    visible: { opacity: 1, x: 0 },
  }

  return (
    <motion.div
      className="bg-slate-800/50 border border-slate-700 rounded-xl p-6"
      variants={containerVariants}
      initial="hidden"
      animate="visible"
    >
      {/* Header */}
      <div className="flex items-center justify-between mb-6">
        <div className="flex items-center gap-2">
          <AlertTriangle size={20} className="text-red-400" />
          <h2 className="text-lg font-semibold text-white">Recent Alerts</h2>
          <motion.span
            className="ml-2 px-2 py-1 bg-red-500/20 text-red-300 text-xs rounded-full"
            animate={{ scale: [1, 1.05, 1] }}
            transition={{ repeat: Infinity, duration: 2 }}
          >
            {alerts.length} active
          </motion.span>
        </div>
        <motion.button
          onClick={onViewAll}
          whileHover={{ x: 5 }}
          className="flex items-center gap-2 text-blue-400 hover:text-blue-300 text-sm"
        >
          View All <ArrowRight size={16} />
        </motion.button>
      </div>

      {/* Alerts List */}
      <motion.div className="space-y-3" variants={containerVariants}>
        {loading ? (
          <motion.div
            animate={{ opacity: [0.5, 1, 0.5] }}
            transition={{ repeat: Infinity, duration: 1.5 }}
            className="text-center py-8 text-slate-400"
          >
            Loading alerts...
          </motion.div>
        ) : alerts.length === 0 ? (
          <div className="text-center py-8 text-slate-400">No alerts</div>
        ) : (
          alerts.map((alert, idx) => {
            const config = severityConfig[alert.severity]

            return (
              <motion.div
                key={alert.id}
                variants={itemVariants}
                whileHover={{ x: 5, backgroundColor: 'rgba(30, 41, 59, 0.8)' }}
                className={`${config.bg} ${config.border} border rounded-lg p-4 cursor-pointer transition-all group`}
              >
                <div className="flex items-start gap-3">
                  {/* Indicator */}
                  <motion.div
                    className={`${config.dot} w-2 h-2 rounded-full mt-1.5 flex-shrink-0`}
                    animate={{ scale: [1, 1.2, 1] }}
                    transition={{
                      repeat: Infinity,
                      duration: 2,
                      delay: idx * 0.1,
                    }}
                  />

                  {/* Content */}
                  <div className="flex-1 min-w-0">
                    <div className="flex items-start justify-between gap-2">
                      <p className="text-sm text-white group-hover:text-slate-100 transition-colors flex-1">
                        {alert.message}
                      </p>
                      <motion.span
                        className={`${config.badge} px-2 py-1 rounded text-xs font-semibold flex-shrink-0`}
                        whileHover={{ scale: 1.05 }}
                      >
                        {alert.severity}
                      </motion.span>
                    </div>
                    <p className="text-xs text-slate-400 mt-1 group-hover:text-slate-300 transition-colors">
                      Session: {alert.sessionId} • {alert.timestamp}
                    </p>
                  </div>

                  {/* Hover Action */}
                  <motion.div
                    initial={{ opacity: 0 }}
                    whileHover={{ opacity: 1 }}
                    className="flex-shrink-0"
                  >
                    <AlertCircle size={18} className="text-slate-400" />
                  </motion.div>
                </div>
              </motion.div>
            )
          })
        )}
      </motion.div>
    </motion.div>
  )
}

export default AlertsPreview
