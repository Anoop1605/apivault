import { motion } from 'framer-motion'
import { TrendingUp, AlertCircle, Activity, BarChart3 } from 'lucide-react'

interface MetricCard {
  title: string
  value: string | number
  icon: React.ReactNode
  trend?: string
  color: 'blue' | 'green' | 'orange' | 'purple'
}

interface MetricsCardsProps {
  data?: MetricCard[]
  loading?: boolean
}

const defaultMetrics: MetricCard[] = [
  {
    title: 'Total Sessions',
    value: 120,
    icon: <BarChart3 size={24} />,
    trend: '+12 today',
    color: 'blue',
  },
  {
    title: 'Total Events',
    value: 980,
    icon: <Activity size={24} />,
    trend: '+45 today',
    color: 'green',
  },
  {
    title: 'High Risk Sessions',
    value: 8,
    icon: <AlertCircle size={24} />,
    trend: '-2 from yesterday',
    color: 'orange',
  },
  {
    title: 'Avg Events/Session',
    value: '8.1',
    icon: <TrendingUp size={24} />,
    trend: 'Stable',
    color: 'purple',
  },
]

const colorClasses = {
  blue: {
    bg: 'bg-blue-500/10',
    border: 'border-blue-500/30',
    icon: 'text-blue-400',
    glow: 'shadow-blue-500/20',
  },
  green: {
    bg: 'bg-green-500/10',
    border: 'border-green-500/30',
    icon: 'text-green-400',
    glow: 'shadow-green-500/20',
  },
  orange: {
    bg: 'bg-orange-500/10',
    border: 'border-orange-500/30',
    icon: 'text-orange-400',
    glow: 'shadow-orange-500/20',
  },
  purple: {
    bg: 'bg-purple-500/10',
    border: 'border-purple-500/30',
    icon: 'text-purple-400',
    glow: 'shadow-purple-500/20',
  },
}

const MetricsCards = ({ data = defaultMetrics, loading = false }: MetricsCardsProps) => {
  const containerVariants = {
    hidden: { opacity: 0 },
    visible: {
      opacity: 1,
      transition: {
        staggerChildren: 0.1,
        delayChildren: 0.3,
      },
    },
  }

  const cardVariants = {
    hidden: { opacity: 0, y: 20 },
    visible: {
      opacity: 1,
      y: 0,
      transition: {
        duration: 0.4,
        ease: 'easeOut',
      },
    },
  }

  const counterVariants = {
    hidden: { opacity: 0 },
    visible: { opacity: 1 },
  }

  return (
    <motion.div
      className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6"
      variants={containerVariants}
      initial="hidden"
      animate="visible"
    >
      {data.map((metric, idx) => {
        const colors = colorClasses[metric.color]

        return (
          <motion.div
            key={idx}
            variants={cardVariants}
            whileHover={{
              y: -8,
              boxShadow: `0 0 20px rgba(0,0,0,0.3)`,
            }}
            className={`${colors.bg} ${colors.border} border rounded-xl p-6 cursor-pointer transition-all duration-200 group`}
          >
            {/* Header */}
            <div className="flex items-center justify-between mb-4">
              <h3 className="text-sm font-medium text-slate-300 group-hover:text-white transition-colors">
                {metric.title}
              </h3>
              <motion.div
                className={`${colors.icon} p-3 rounded-lg ${colors.bg} group-hover:scale-110 transition-transform`}
                whileHover={{
                  scale: 1.2,
                  rotate: [0, 5, -5, 0],
                }}
              >
                {metric.icon}
              </motion.div>
            </div>

            {/* Value */}
            <motion.div variants={counterVariants}>
              <p className="text-3xl font-bold text-white mb-2">
                {loading ? (
                  <motion.span
                    animate={{ opacity: [0.5, 1, 0.5] }}
                    transition={{ repeat: Infinity, duration: 1.5 }}
                  >
                    —
                  </motion.span>
                ) : (
                  metric.value
                )}
              </p>
            </motion.div>

            {/* Trend */}
            {metric.trend && (
              <motion.p
                className="text-xs text-slate-400 group-hover:text-slate-300 transition-colors"
                initial={{ opacity: 0 }}
                animate={{ opacity: 1 }}
                transition={{ delay: 0.2 }}
              >
                ✨ {metric.trend}
              </motion.p>
            )}
          </motion.div>
        )
      })}
    </motion.div>
  )
}

export default MetricsCards
