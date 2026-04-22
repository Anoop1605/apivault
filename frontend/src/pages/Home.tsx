import { useState, useEffect } from 'react'
import { motion } from 'framer-motion'
import Sidebar from '../components/Sidebar'
import Topbar from '../components/Topbar'
import MetricsCards from '../components/MetricsCards'
import AlertsPreview from '../components/AlertsPreview'
import ActivityFeed from '../components/ActivityFeed'
import SessionsPreview from '../components/SessionsPreview'
import { forensicService } from '../services/api'

const Home = () => {
  const [metrics, setMetrics] = useState({
    totalSessions: 0,
    totalEvents: 0,
    highRiskSessions: 0,
    avgEventsPerSession: 0,
  })

  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const loadData = async () => {
      try {
        setLoading(true)
        const { data: sessions } = await forensicService.listSessions()

        const totalSessions = sessions.length
        const totalEvents = sessions.reduce((sum, s) => sum + s.eventCount, 0)
        const highRiskSessions = 8 // This would come from alerts
        const avgEventsPerSession =
          totalSessions > 0 ? (totalEvents / totalSessions).toFixed(1) : 0

        setMetrics({
          totalSessions,
          totalEvents,
          highRiskSessions,
          avgEventsPerSession: parseFloat(avgEventsPerSession as string),
        })
      } catch (error) {
        console.error('Failed to load dashboard data:', error)
      } finally {
        setLoading(false)
      }
    }

    loadData()
  }, [])

  const pageVariants = {
    hidden: { opacity: 0 },
    visible: {
      opacity: 1,
      transition: {
        duration: 0.3,
        staggerChildren: 0.1,
      },
    },
  }

  const containerVariants = {
    hidden: { opacity: 0 },
    visible: {
      opacity: 1,
      transition: {
        staggerChildren: 0.05,
        delayChildren: 0.2,
      },
    },
  }

  return (
    <motion.div
      className="min-h-screen bg-slate-950"
      variants={pageVariants}
      initial="hidden"
      animate="visible"
    >
      {/* Navigation */}
      <Sidebar />
      <Topbar title="Dashboard" />

      {/* Main Content */}
      <motion.main
        className="pt-20 p-8"
        variants={containerVariants}
        initial="hidden"
        animate="visible"
      >
        {/* Metrics Row */}
        <section className="mb-8">
          <MetricsCards
            data={[
              {
                title: 'Total Sessions',
                value: metrics.totalSessions,
                icon: <div>📊</div>,
                trend: '+12 today',
                color: 'blue',
              },
              {
                title: 'Total Events',
                value: metrics.totalEvents,
                icon: <div>📈</div>,
                trend: '+45 today',
                color: 'green',
              },
              {
                title: 'High Risk Sessions',
                value: metrics.highRiskSessions,
                icon: <div>⚠️</div>,
                trend: '-2 from yesterday',
                color: 'orange',
              },
              {
                title: 'Avg Events/Session',
                value: metrics.avgEventsPerSession,
                icon: <div>📉</div>,
                trend: 'Stable',
                color: 'purple',
              },
            ]}
            loading={loading}
          />
        </section>

        {/* Two Column Layout */}
        <section className="grid grid-cols-1 lg:grid-cols-3 gap-8 mb-8">
          {/* Left Column - Alerts */}
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.3 }}
            className="lg:col-span-1"
          >
            <AlertsPreview
              onViewAll={() => window.location.href = '/alerts'}
            />
          </motion.div>

          {/* Right Column - Activity */}
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.35 }}
            className="lg:col-span-2"
          >
            <ActivityFeed loading={loading} />
          </motion.div>
        </section>

        {/* Sessions Overview */}
        <section>
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.4 }}
          >
            <SessionsPreview
              loading={loading}
              onViewAll={() => window.location.href = '/sessions'}
            />
          </motion.div>
        </section>
      </motion.main>

      {/* Footer */}
      <motion.footer
        className="px-8 py-4 text-center text-slate-500 text-xs border-t border-slate-800"
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        transition={{ delay: 0.5 }}
      >
        © 2024 Sentinel Forensics Dashboard. All rights reserved.
      </motion.footer>
    </motion.div>
  )
}

export default Home
