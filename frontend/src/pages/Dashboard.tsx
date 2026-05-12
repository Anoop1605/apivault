import { motion } from 'framer-motion'
import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  AlertTriangle,
  Zap,
  Shield,
  TrendingUp,
  Clock,
  Users,
  Activity,
  Target,
  ChevronRight,
  Flame,
} from 'lucide-react'
import { apiService } from '../services/api'

const Dashboard = () => {
  const navigate = useNavigate()
  const [activeSession, setActiveSession] = useState(0)
  const [sessions, setSessions] = useState<any[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const loadSessions = async () => {
      try {
        // Fetch all session IDs directly from Event Store (8081)
        const { data: sessionIds } = await apiService.events.getAllSessionIds()
        // For each session, fetch its events to build a rich summary
        const summaries = await Promise.all(
          sessionIds.slice(0, 20).map(async (sessionId: string) => {
            try {
              const { data: events } = await apiService.events.getEventsBySession(sessionId)
              const denyCount = events.filter(e => e.decision === 'DENY').length
              const maxRisk = Math.max(0, ...events.map(e => e.riskScore || 0))
              const userId = events.find(e => e.userId)?.userId || 'anonymous'
              const eventCount = events.length
              // Use the first event's policy rule as summary rule
              const topRule = events.find(e => e.policyRuleId)?.policyRuleId || 'N/A'
              return { sessionId, userId, denyCount, maxRisk, eventCount, topRule }
            } catch {
              return { sessionId, userId: 'unknown', denyCount: 0, maxRisk: 0, eventCount: 0, topRule: 'N/A' }
            }
          })
        )
        setSessions(summaries)
      } catch (error) {
        console.error('Failed to load sessions:', error)
      } finally {
        setLoading(false)
      }
    }
    loadSessions()
  }, [])

  const containerVariants = {
    hidden: { opacity: 0 },
    visible: {
      opacity: 1,
      transition: {
        staggerChildren: 0.1,
        delayChildren: 0.2,
      },
    },
  }

  const itemVariants = {
    hidden: { opacity: 0, y: 20 },
    visible: {
      opacity: 1,
      y: 0,
      transition: { duration: 0.5, ease: 'easeOut' },
    },
  }

  const getCriticalSession = () => {
    if (sessions.length === 0) return null
    // Score = deny events (weighted x3) + total event count
    return sessions.reduce((prev: any, current: any) => {
      const scoreA = (prev.denyCount || 0) * 3 + (prev.eventCount || 0)
      const scoreB = (current.denyCount || 0) * 3 + (current.eventCount || 0)
      return scoreB > scoreA ? current : prev
    })
  }

  const getThreatLevel = () => {
    const totalDenies = sessions.reduce((sum: number, s: any) => sum + (s.denyCount || 0), 0)
    if (totalDenies > 5) return { label: 'CRITICAL', color: 'from-red-400 to-red-600' }
    if (totalDenies > 2) return { label: 'HIGH', color: 'from-orange-400 to-red-500' }
    if (totalDenies > 0) return { label: 'MEDIUM', color: 'from-yellow-400 to-orange-500' }
    return { label: 'LOW', color: 'from-green-400 to-cyan-400' }
  }

  const criticalSession = getCriticalSession()
  const threat = getThreatLevel()

  return (
    <motion.div
      className="min-h-screen bg-gradient-to-br from-slate-950 via-slate-900 to-slate-950 overflow-hidden"
      variants={containerVariants}
      initial="hidden"
      animate="visible"
    >
      {/* Top bar */}
      <motion.div
        className="fixed top-0 left-0 right-0 h-16 bg-slate-900/50 border-b border-slate-800 flex items-center justify-between px-8 backdrop-blur-md z-40"
        initial={{ y: -64 }}
        animate={{ y: 0 }}
      >
        <motion.h1 className="text-2xl font-bold text-white">
          Forensic <span className="text-red-400">Dashboard</span>
        </motion.h1>
        <div className="flex items-center gap-4">
          <motion.button
            whileHover={{ scale: 1.05 }}
            className="px-4 py-2 rounded-lg bg-slate-800 text-slate-300 hover:text-white transition-colors"
          >
            Filter
          </motion.button>
        </div>
      </motion.div>

      {/* Main Content - Normal spacing */}
      <div className="pt-8 lg:pt-12 px-4 md:px-8 pb-40 w-full">
        {/* Dynamic Alert Banner - Replaces the hardcoded hero */}
        {criticalSession && (
          <motion.div
            variants={itemVariants}
            className="mb-12 rounded-3xl overflow-hidden w-full relative"
            style={{
              background: 'linear-gradient(135deg, rgba(239, 68, 68, 0.2), rgba(245, 158, 11, 0.15))',
              border: '2px solid rgba(239, 68, 68, 0.4)',
              backdropFilter: 'blur(15px)',
            }}
          >
            <div className="p-8 md:p-10 flex flex-col md:flex-row items-center justify-between gap-8">
              <div className="flex-1">
                <div className="flex items-center gap-3 mb-4">
                  <span className="px-4 py-1 rounded-full bg-red-600 text-white font-bold text-sm animate-pulse">
                    CRITICAL ALERT
                  </span>
                  <span className="text-slate-400 text-sm font-semibold">Multiple Violations Detected</span>
                </div>
                <h2 className="text-3xl md:text-4xl font-black text-white mb-4">
                  Suspicious Activity on <span className="text-red-400 font-mono">{criticalSession.sessionId.slice(0, 16)}...</span>
                </h2>
                <div className="flex gap-4">
                  <div className="bg-slate-900/50 p-4 rounded-xl border border-slate-800">
                    <p className="text-slate-500 text-xs font-bold uppercase mb-1">Event Count</p>
                    <p className="text-2xl font-black text-red-400">
                      {criticalSession.eventCount} Interceptions
                    </p>
                  </div>
                  <div className="bg-slate-900/50 p-4 rounded-xl border border-slate-800">
                    <p className="text-slate-500 text-xs font-bold uppercase mb-1">Security Status</p>
                    <p className="text-2xl font-black text-orange-400">ISOLATED</p>
                  </div>
                </div>
              </div>
              <motion.button
                whileHover={{ scale: 1.05, boxShadow: '0 0 30px rgba(239, 68, 68, 0.4)' }}
                onClick={() => navigate(`/sessions/${criticalSession.sessionId}`)}
                className="px-8 py-4 bg-red-600 hover:bg-red-500 text-white font-black rounded-2xl shadow-xl transition-all"
              >
                INVESTIGATE NOW
              </motion.button>
            </div>
          </motion.div>
        )}
        {/* Grid Layout - Responsive - ONLY REAL STATS OR NECESSARY ONES */}
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-2 gap-4 md:gap-6 mb-12 mt-20">
          {/* New Sessions Card - Now reflecting real count if available, or just keeping it simple */}
          <motion.div
            variants={itemVariants}
            className="rounded-2xl p-6 md:p-8"
            style={{
              background: 'linear-gradient(135deg, rgba(59, 130, 246, 0.15), rgba(6, 182, 212, 0.15))',
              border: '2px solid rgba(59, 130, 246, 0.3)',
              backdropFilter: 'blur(10px)',
            }}
            whileHover={{ y: -8, borderColor: 'rgba(59, 130, 246, 0.6)' }}
          >
            <div className="flex items-center justify-between mb-4">
              <h3 className="text-slate-300 font-bold text-sm md:text-base">Active Forensic Sessions</h3>
              <motion.div
                className="p-3 rounded-xl bg-blue-500/20 border border-blue-500/30"
                whileHover={{ scale: 1.1, rotate: 10 }}
              >
                <Users size={20} className="text-blue-400" />
              </motion.div>
            </div>
            <motion.div
              className="text-4xl md:text-5xl font-black text-transparent bg-gradient-to-r from-blue-400 to-cyan-400 bg-clip-text mb-2"
              animate={{ y: [0, -3, 0] }}
              transition={{ duration: 3, repeat: Infinity }}
            >
              {sessions.length}
            </motion.div>
            <p className="text-xs md:text-sm text-slate-400 font-semibold">Real-time sessions discovered</p>
          </motion.div>

          {/* Threat Level Card */}
          <motion.div
            variants={itemVariants}
            className="rounded-2xl p-6 md:p-8"
            style={{
              background: 'linear-gradient(135deg, rgba(239, 68, 68, 0.15), rgba(245, 158, 11, 0.15))',
              border: '2px solid rgba(239, 68, 68, 0.3)',
              backdropFilter: 'blur(10px)',
            }}
            whileHover={{ y: -8, borderColor: 'rgba(239, 68, 68, 0.6)' }}
          >
            <div className="flex items-center justify-between mb-4">
              <h3 className="text-slate-300 font-bold text-sm md:text-base">System Threat Level</h3>
              <motion.div
                className="p-3 rounded-xl bg-red-500/20 border border-red-500/30"
                whileHover={{ scale: 1.1, rotate: 10 }}
              >
                <Flame size={20} className="text-red-400" />
              </motion.div>
            </div>
            <motion.div
              className={`text-4xl md:text-5xl font-black text-transparent bg-gradient-to-r ${threat.color} bg-clip-text mb-2`}
              animate={{ y: [0, -3, 0] }}
              transition={{ duration: 3, repeat: Infinity, delay: 0.1 }}
            >
              {threat.label}
            </motion.div>
            <p className="text-xs md:text-sm text-slate-400 font-semibold">
              {sessions.reduce((s: number, x: any) => s + (x.denyCount || 0), 0)} deny events detected
            </p>
          </motion.div>
        </div>

        {/* Sessions Grid */}
        <motion.div variants={itemVariants} className="mb-8">
          <h3 className="text-2xl md:text-3xl font-bold text-white mb-6">Discovered Security Sessions</h3>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4 md:gap-6">
            {sessions.map((session, idx) => (
              <motion.div
                key={idx}
                className="rounded-2xl p-6 md:p-8 cursor-pointer group"
                style={{
                  background: 'linear-gradient(135deg, rgba(51, 65, 85, 0.4), rgba(71, 85, 105, 0.3))',
                  border: '2px solid rgba(148, 163, 184, 0.2)',
                  backdropFilter: 'blur(10px)',
                }}
                whileHover={{
                  y: -6,
                  borderColor: 'rgba(148, 163, 184, 0.4)',
                }}
                animate={{
                  borderColor: activeSession === idx ? 'rgba(59, 130, 246, 0.6)' : 'rgba(148, 163, 184, 0.2)',
                }}
                onClick={() => setActiveSession(idx)}
              >
                <div className="flex items-start justify-between mb-6">
                  <div>
                    <p className="text-slate-400 text-xs md:text-sm uppercase tracking-wider font-bold">Session ID</p>
                    <p className="text-white font-mono font-bold text-sm md:text-base">{session.sessionId}</p>
                  </div>
                  <motion.div
                    className="px-3 py-1 rounded-lg bg-gradient-to-r from-blue-500/20 to-blue-500/10 border border-blue-500/30 text-xs font-bold text-blue-300"
                    whileHover={{ scale: 1.05 }}
                  >
                    TAMPER-PROOF
                  </motion.div>
                </div>

                <div className="grid grid-cols-2 gap-4 md:gap-6 mb-6">
                  <div>
                    <p className="text-slate-500 text-xs font-semibold mb-2">Audit Events</p>
                    <p className="text-2xl md:text-3xl font-black text-blue-400">{session.eventCount}</p>
                  </div>
                  <div>
                    <p className="text-slate-500 text-xs font-semibold mb-2">Deny Count</p>
                    <p className="text-2xl md:text-3xl font-black text-red-400">{session.denyCount ?? 0}</p>
                  </div>
                  <div>
                    <p className="text-slate-500 text-xs font-semibold mb-2">Max Risk</p>
                    <p className="text-lg font-black text-orange-400">{((session.maxRisk ?? 0) * 100).toFixed(0)}%</p>
                  </div>
                  <div>
                    <p className="text-slate-500 text-xs font-semibold mb-2">Top Rule</p>
                    <p className="text-xs font-mono text-cyan-400 truncate">{session.topRule}</p>
                  </div>
                </div>

                <motion.button
                  onClick={() => navigate(`/sessions/${session.sessionId}`)}
                  className="w-full py-3 rounded-lg text-sm md:text-base font-bold text-slate-300 group-hover:text-white transition-colors flex items-center justify-center gap-2 border border-slate-700/50 hover:border-blue-500/50 hover:bg-blue-500/10"
                  whileHover={{ gap: 12 }}
                >
                  Audit Timeline
                  <ChevronRight size={18} />
                </motion.button>
              </motion.div>
            ))}
          </div>
        </motion.div>

        {/* Simplified Analysis Section */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mt-12">
          <motion.div
            variants={itemVariants}
            className="rounded-2xl p-6 md:p-8"
            style={{
              background: 'linear-gradient(135deg, rgba(51, 65, 85, 0.4), rgba(71, 85, 105, 0.3))',
              border: '2px solid rgba(148, 163, 184, 0.2)',
              backdropFilter: 'blur(10px)',
            }}
          >
            <h4 className="text-xl font-bold text-white mb-6 flex items-center gap-3">
              <Shield className="text-blue-400" size={24} />
              Protected Infrastructure
            </h4>
            <div className="space-y-4">
              {['Payment API', 'User Service', 'Admin Portal', 'Event Store'].map((service, i) => (
                <div key={i} className="flex items-center gap-3 p-4 rounded-xl bg-slate-800/40 border border-slate-700/50">
                  <div className="w-2 h-2 rounded-full bg-blue-500 animate-pulse" />
                  <span className="text-slate-300 font-semibold">{service}</span>
                  <span className="ml-auto text-xs font-bold text-blue-400 bg-blue-500/10 px-2 py-1 rounded">SECURED</span>
                </div>
              ))}
            </div>
          </motion.div>

          <motion.div
            variants={itemVariants}
            className="rounded-2xl p-6 md:p-8"
            style={{
              background: 'linear-gradient(135deg, rgba(51, 65, 85, 0.4), rgba(71, 85, 105, 0.3))',
              border: '2px solid rgba(148, 163, 184, 0.2)',
              backdropFilter: 'blur(10px)',
            }}
          >
            <h4 className="text-xl font-bold text-white mb-6 flex items-center gap-3">
              <Activity className="text-red-400" size={24} />
              Intercepted Violations
            </h4>
            <div className="space-y-4">
              {['SQL Injection Attempts', 'Unauthorized Access', 'Credential Brute Force', 'Protocol Deviations'].map((threat, i) => (
                <div key={i} className="flex items-center gap-3 p-4 rounded-xl bg-slate-800/40 border border-slate-700/50">
                  <div className="w-2 h-2 rounded-full bg-red-500" />
                  <span className="text-slate-300 font-semibold">{threat}</span>
                  <span className="ml-auto text-xs font-bold text-red-400 bg-red-500/10 px-2 py-1 rounded">DETECTED</span>
                </div>
              ))}
            </div>
          </motion.div>
        </div>

      </div>
    </motion.div>
  )
}

export default Dashboard
