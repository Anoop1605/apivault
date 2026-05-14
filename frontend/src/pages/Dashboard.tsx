import { motion } from 'framer-motion'
import { useState, useEffect } from 'react'
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
import { forensicService, eventStoreService } from '../services/api'

const Dashboard = () => {
  const [activeSession, setActiveSession] = useState(0)
  const [sessions, setSessions] = useState<any[]>([])
  const [alerts, setAlerts] = useState<any[]>([])
  const [metrics, setMetrics] = useState<any>({
    totalEvents: 0,
    blockedThreats: 0,
    activeSessions: 0,
    avgResponseTimeMs: 0.15,
    systemThreatLevel: 'LOW'
  })
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const fetchData = async () => {
      try {
        const [sessionsRes, metricsRes, alertsRes] = await Promise.all([
          forensicService.listSessions(),
          eventStoreService.getSystemMetrics(),
          eventStoreService.getAlerts()
        ])
        setSessions(sessionsRes.data.slice(0, 6))
        setMetrics(metricsRes.data)
        setAlerts(alertsRes.data.slice(0, 5))
      } catch (error) {
        console.error('Failed to load dashboard data:', error)
      } finally {
        setLoading(false)
      }
    }
    
    fetchData()
    const interval = setInterval(fetchData, 10000) // Poll every 10s
    return () => clearInterval(interval)
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
        {/* Hero Section - Featured Threat - LARGE */}
        <motion.div
          variants={itemVariants}
          className="mb-16 rounded-3xl overflow-hidden w-full"
          style={{
            background: 'linear-gradient(135deg, rgba(239, 68, 68, 0.15), rgba(245, 158, 11, 0.15), rgba(239, 68, 68, 0.1))',
            border: '2px solid rgba(239, 68, 68, 0.4)',
            backdropFilter: 'blur(15px)',
            minHeight: '400px',
          }}
        >
          <div className="relative p-8 md:p-12 lg:p-16 overflow-hidden h-full">
            {/* Multiple Background glows for intensity */}
            <motion.div
              className="absolute -top-60 -right-60 w-96 h-96 bg-red-600/30 rounded-full blur-3xl"
              animate={{
                x: [0, 50, 0],
                y: [0, 60, 0],
              }}
              transition={{
                duration: 8,
                repeat: Infinity,
                ease: 'easeInOut',
              }}
            />
            
            <motion.div
              className="absolute top-1/2 -left-40 w-80 h-80 bg-orange-500/20 rounded-full blur-3xl"
              animate={{
                x: [0, -30, 0],
                y: [0, 40, 0],
              }}
              transition={{
                duration: 6,
                repeat: Infinity,
                ease: 'easeInOut',
                delay: 1,
              }}
            />

            <div className="relative z-10 grid grid-cols-1 md:grid-cols-3 gap-8 md:gap-12 h-full">
              {/* Left - Threat Info */}
              <div className="col-span-1 md:col-span-2 flex flex-col justify-center">
                <motion.div
                  initial={{ opacity: 0, x: -30 }}
                  animate={{ opacity: 1, x: 0 }}
                  transition={{ delay: 0.2 }}
                >
                  {/* Critical Badge */}
                  <div className="flex items-center gap-3 mb-6">
                    <motion.div
                      className="px-4 py-2 rounded-full bg-gradient-to-r from-red-600 to-red-500 border-2 border-red-300"
                      animate={{ 
                        boxShadow: ['0 0 20px rgba(239, 68, 68, 0.6)', '0 0 40px rgba(239, 68, 68, 0.8)', '0 0 20px rgba(239, 68, 68, 0.6)'],
                        scale: [1, 1.05, 1]
                      }}
                      transition={{ duration: 2, repeat: Infinity }}
                    >
                      <p className="text-sm md:text-base font-bold text-white flex items-center gap-2">
                        🚨 CRITICAL ALERT
                      </p>
                    </motion.div>
                    <span className="text-slate-300 text-sm md:text-base bg-slate-800/50 px-3 py-1 rounded-full">Active Threat</span>
                  </div>

                  {/* Main Title */}
                  <h2 className="text-3xl md:text-4xl lg:text-5xl font-black text-white mb-4 leading-tight">
                    SQL Injection <span className="bg-gradient-to-r from-red-400 to-orange-400 bg-clip-text text-transparent">Attack Detected</span>
                  </h2>
                  
                  {/* Description */}
                  <p className="text-slate-300 text-base md:text-lg mb-6 leading-relaxed max-w-xl">
                    Multiple failed queries attempting to bypass database authentication. Immediate action required.
                  </p>

                  {/* Details Grid */}
                  <div className="grid grid-cols-2 md:grid-cols-3 gap-4 md:gap-6 text-sm md:text-base">
                    <motion.div 
                      className="p-3 md:p-4 rounded-lg bg-slate-800/40 border border-slate-700/50 hover:border-blue-500/50 transition-colors"
                      whileHover={{ scale: 1.05 }}
                    >
                      <p className="text-slate-400 text-xs md:text-sm">Session ID</p>
                      <p className="text-white font-mono font-bold text-sm md:text-base">sess-042</p>
                    </motion.div>
                    <motion.div 
                      className="p-3 md:p-4 rounded-lg bg-slate-800/40 border border-slate-700/50 hover:border-green-500/50 transition-colors"
                      whileHover={{ scale: 1.05 }}
                    >
                      <p className="text-slate-400 text-xs md:text-sm">Detected</p>
                      <p className="text-white font-mono font-bold text-sm md:text-base">2 min ago</p>
                    </motion.div>
                    <motion.div 
                      className="p-3 md:p-4 rounded-lg bg-slate-800/40 border border-slate-700/50 hover:border-red-500/50 transition-colors"
                      whileHover={{ scale: 1.05 }}
                    >
                      <p className="text-slate-400 text-xs md:text-sm">Risk Level</p>
                      <p className="text-red-400 font-bold text-sm md:text-base">CRITICAL</p>
                    </motion.div>
                  </div>
                </motion.div>
              </div>

              {/* Right - Big Statistics */}
              <motion.div
                className="col-span-1 flex flex-col justify-center items-center"
                initial={{ opacity: 0, x: 30 }}
                animate={{ opacity: 1, x: 0 }}
                transition={{ delay: 0.3 }}
              >
                <div className="text-center w-full">
                  {/* Big Number */}
                  <motion.div
                    className="text-6xl md:text-7xl lg:text-8xl font-black text-transparent bg-gradient-to-r from-red-500 via-orange-400 to-red-500 bg-clip-text mb-3"
                    animate={{ scale: [1, 1.08, 1] }}
                    transition={{ duration: 2.5, repeat: Infinity }}
                  >
                    {metrics.totalEvents}
                  </motion.div>
                  
                  <p className="text-slate-300 text-sm md:text-base mb-8 font-semibold">Malicious Events Detected</p>

                  {/* CTA Button */}
                  <motion.button
                    whileHover={{ 
                      scale: 1.08,
                      boxShadow: '0 0 40px rgba(239, 68, 68, 0.6)',
                      background: 'linear-gradient(135deg, rgb(220, 38, 38), rgb(239, 68, 68))'
                    }}
                    whileTap={{ scale: 0.95 }}
                    className="w-full px-6 md:px-8 py-3 md:py-4 rounded-xl bg-gradient-to-r from-red-600 to-red-500 text-white font-bold text-base md:text-lg hover:from-red-500 hover:to-red-400 transition-all shadow-lg shadow-red-500/50 mb-4"
                  >
                    🔍 Analyze Now
                  </motion.button>

                  {/* Secondary Button */}
                  <motion.button
                    whileHover={{ scale: 1.05 }}
                    className="w-full px-6 md:px-8 py-2 md:py-3 rounded-xl border-2 border-orange-500 text-orange-400 font-semibold text-sm md:text-base hover:bg-orange-500/10 transition-all"
                  >
                    View Timeline
                  </motion.button>
                </div>
              </motion.div>
            </div>
          </div>
        </motion.div>

        {/* Grid Layout - Responsive */}
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 md:gap-6 mb-8">
          {/* New Sessions Card */}
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
              <h3 className="text-slate-300 font-bold text-sm md:text-base">New Sessions</h3>
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
              {metrics.activeSessions}
            </motion.div>
            <p className="text-xs md:text-sm text-slate-400 font-semibold">+12% this week</p>
          </motion.div>

          {/* Active Threats Card */}
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
              <h3 className="text-slate-300 font-bold text-sm md:text-base">Blocked Threats</h3>
              <motion.div
                className="p-3 rounded-xl bg-red-500/20 border border-red-500/30"
                whileHover={{ scale: 1.1, rotate: 10 }}
              >
                <Shield size={20} className="text-red-400" />
              </motion.div>
            </div>
            <motion.div
              className="text-4xl md:text-5xl font-black text-transparent bg-gradient-to-r from-red-400 to-orange-400 bg-clip-text mb-2"
              animate={{ y: [0, -3, 0] }}
              transition={{ duration: 3, repeat: Infinity, delay: 0.1 }}
            >
              {metrics.blockedThreats}
            </motion.div>
            <p className="text-xs md:text-sm text-slate-400 font-semibold">Last 24 hours</p>
          </motion.div>

          {/* Avg Response Time */}
          <motion.div
            variants={itemVariants}
            className="rounded-2xl p-6 md:p-8"
            style={{
              background: 'linear-gradient(135deg, rgba(34, 197, 94, 0.15), rgba(168, 85, 247, 0.15))',
              border: '2px solid rgba(34, 197, 94, 0.3)',
              backdropFilter: 'blur(10px)',
            }}
            whileHover={{ y: -8, borderColor: 'rgba(34, 197, 94, 0.6)' }}
          >
            <div className="flex items-center justify-between mb-4">
              <h3 className="text-slate-300 font-bold text-sm md:text-base">Response Time</h3>
              <motion.div
                className="p-3 rounded-xl bg-green-500/20 border border-green-500/30"
                whileHover={{ scale: 1.1, rotate: 10 }}
              >
                <Zap size={20} className="text-green-400" />
              </motion.div>
            </div>
            <motion.div
              className="text-4xl md:text-5xl font-black text-transparent bg-gradient-to-r from-green-400 to-emerald-400 bg-clip-text mb-2"
              animate={{ y: [0, -3, 0] }}
              transition={{ duration: 3, repeat: Infinity, delay: 0.2 }}
            >
              {metrics.avgResponseTimeMs}s
            </motion.div>
            <p className="text-xs md:text-sm text-slate-400 font-semibold">Avg per event</p>
          </motion.div>

          {/* Threat Level */}
          <motion.div
            variants={itemVariants}
            className="rounded-2xl p-6 md:p-8"
            style={{
              background: 'linear-gradient(135deg, rgba(139, 92, 246, 0.15), rgba(168, 85, 247, 0.15))',
              border: '2px solid rgba(139, 92, 246, 0.3)',
              backdropFilter: 'blur(10px)',
            }}
            whileHover={{ y: -8, borderColor: 'rgba(139, 92, 246, 0.6)' }}
          >
            <div className="flex items-center justify-between mb-4">
              <h3 className="text-slate-300 font-bold text-sm md:text-base">Threat Level</h3>
              <motion.div
                className="p-3 rounded-xl bg-purple-500/20 border border-purple-500/30"
                whileHover={{ scale: 1.1, rotate: 10 }}
              >
                <Flame size={20} className="text-purple-400" />
              </motion.div>
            </div>
            <motion.div
              className="text-4xl md:text-5xl font-black text-transparent bg-gradient-to-r from-purple-400 to-pink-400 bg-clip-text mb-2"
              animate={{ y: [0, -3, 0] }}
              transition={{ duration: 3, repeat: Infinity, delay: 0.3 }}
            >
              {metrics.systemThreatLevel}
            </motion.div>
            <p className="text-xs md:text-sm text-slate-400 font-semibold">Current status</p>
          </motion.div>
        </div>

        {/* Sessions Grid */}
        <motion.div variants={itemVariants} className="mb-8">
          <h3 className="text-2xl md:text-3xl font-bold text-white mb-6">Recent Sessions</h3>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4 md:gap-6">
            {sessions.slice(0, 4).map((session, idx) => (
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
                    <p className="text-slate-400 text-xs md:text-sm">Session ID</p>
                    <p className="text-white font-mono font-bold text-sm md:text-base">{session.sessionId?.slice(0, 12)}...</p>
                  </div>
                  <motion.div
                    className="px-3 py-1 rounded-lg bg-gradient-to-r from-green-500/20 to-green-500/10 border border-green-500/30 text-xs font-bold text-green-300"
                    whileHover={{ scale: 1.05 }}
                  >
                    {Math.random() > 0.5 ? '✓ SAFE' : '⚠ REVIEW'}
                  </motion.div>
                </div>

                <div className="grid grid-cols-2 gap-4 md:gap-6 mb-6">
                  <div>
                    <p className="text-slate-500 text-xs font-semibold mb-2">Events</p>
                    <p className="text-2xl md:text-3xl font-black text-blue-400">{session.eventCount}</p>
                  </div>
                  <div>
                    <p className="text-slate-500 text-xs font-semibold mb-2">Risk Score</p>
                    <motion.div
                      className="w-full h-3 rounded-full bg-slate-700 overflow-hidden"
                    >
                      <motion.div
                        className="h-full bg-gradient-to-r from-green-400 via-yellow-400 to-red-400"
                        initial={{ width: 0 }}
                        animate={{ width: `${Math.random() * 100}%` }}
                        transition={{ delay: 0.5 + idx * 0.1 }}
                      />
                    </motion.div>
                  </div>
                </div>

                <motion.button
                  className="w-full py-3 rounded-lg text-sm md:text-base font-bold text-slate-300 group-hover:text-white transition-colors flex items-center justify-center gap-2 border border-slate-700/50 hover:border-blue-500/50 hover:bg-blue-500/10"
                  whileHover={{ gap: 12 }}
                >
                  View Details
                  <ChevronRight size={18} />
                </motion.button>
              </motion.div>
            ))}
          </div>
        </motion.div>

        {/* Top Threats Section */}
        <motion.div
          variants={itemVariants}
          className="mt-8"
        >
          <h3 className="text-2xl md:text-3xl font-bold text-white mb-6">Top Threats</h3>
          <div
            className="rounded-2xl p-6 md:p-8"
            style={{
              background: 'linear-gradient(135deg, rgba(51, 65, 85, 0.4), rgba(71, 85, 105, 0.3))',
              border: '2px solid rgba(148, 163, 184, 0.2)',
              backdropFilter: 'blur(10px)',
            }}
          >
            <div className="space-y-3 md:space-y-4">
              {alerts.length > 0 ? (
                alerts.map((alert, idx) => (
                  <motion.div
                    key={alert.id || idx}
                    className="flex items-center justify-between p-4 md:p-5 rounded-xl hover:bg-slate-800/30 transition-colors border border-slate-700/30 hover:border-red-500/30"
                    whileHover={{ x: 6 }}
                  >
                    <div className="flex items-center gap-3 md:gap-4 flex-1 min-w-0">
                      <motion.div
                        animate={{ rotate: [0, 360] }}
                        transition={{ duration: 3, repeat: Infinity, delay: idx * 0.2 }}
                        className="p-3 rounded-lg bg-red-500/20 border border-red-500/30 flex-shrink-0"
                      >
                        <AlertTriangle size={20} className="text-red-400" />
                      </motion.div>
                      <div className="flex flex-col">
                        <span className="text-slate-300 font-semibold text-sm md:text-base truncate">{alert.reason}</span>
                        <span className="text-slate-500 text-xs font-mono">{alert.endpoint}</span>
                      </div>
                    </div>
                    <span className="text-red-400 text-sm md:text-base font-bold ml-4 flex-shrink-0 bg-red-500/10 px-3 py-1 rounded-lg">
                      {(alert.riskScore * 100).toFixed(0)}% Risk
                    </span>
                  </motion.div>
                ))
              ) : (
                <div className="py-8 text-center">
                  <p className="text-slate-500 italic">No critical threats detected. System secure.</p>
                </div>
              )}
            </div>
          </div>
        </motion.div>

        {/* 🔥 SCROLL CONTENT SECTION 1 - Detailed Analysis */}
        <motion.div
          variants={itemVariants}
          className="mt-16"
        >
          <h3 className="text-2xl md:text-3xl font-bold text-white mb-6">Detailed Analysis</h3>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            {[
              { title: 'Attack Vectors', items: ['SQL Injection', 'XSS', 'CSRF', 'Path Traversal'] },
              { title: 'Affected Services', items: ['Payment API', 'User Service', 'Admin Panel', 'Event Store'] },
              { title: 'Detection Methods', items: ['WAF Rules', 'Behavior Analysis', 'Signature Match', 'Anomaly Detection'] },
              { title: 'Response Actions', items: ['Block IP', 'Alert Team', 'Log Event', 'Escalate'] },
            ].map((section, idx) => (
              <motion.div
                key={idx}
                variants={itemVariants}
                className="rounded-2xl p-6 md:p-8"
                style={{
                  background: 'linear-gradient(135deg, rgba(51, 65, 85, 0.4), rgba(71, 85, 105, 0.3))',
                  border: '2px solid rgba(148, 163, 184, 0.2)',
                  backdropFilter: 'blur(10px)',
                }}
                whileHover={{
                  borderColor: 'rgba(59, 130, 246, 0.4)',
                  backgroundColor: 'rgba(59, 130, 246, 0.1)',
                }}
              >
                <h4 className="text-lg font-bold text-white mb-4">{section.title}</h4>
                <div className="space-y-3">
                  {section.items.map((item, i) => (
                    <motion.div
                      key={i}
                      className="flex items-center gap-3 p-3 rounded-lg bg-slate-800/30 border border-slate-700/30"
                      initial={{ opacity: 0, x: -20 }}
                      whileInView={{ opacity: 1, x: 0 }}
                      transition={{ delay: i * 0.1 }}
                    >
                      <div className="w-2 h-2 rounded-full bg-blue-400" />
                      <span className="text-slate-300 text-sm md:text-base">{item}</span>
                    </motion.div>
                  ))}
                </div>
              </motion.div>
            ))}
          </div>
        </motion.div>

        {/* 🔥 SCROLL CONTENT SECTION 2 - Real-time Timeline */}
        <motion.div
          variants={itemVariants}
          className="mt-16"
        >
          <h3 className="text-2xl md:text-3xl font-bold text-white mb-6">Real-time Event Timeline</h3>
          <div className="space-y-4">
            {alerts.length > 0 ? (
              alerts.map((entry, idx) => (
                <motion.div
                  key={idx}
                  className="flex items-center gap-6 p-5 rounded-xl border border-slate-700/30 hover:border-slate-600/50 transition-all"
                  style={{
                    background: 'linear-gradient(135deg, rgba(51, 65, 85, 0.3), rgba(71, 85, 105, 0.2))',
                  }}
                  whileHover={{ x: 8 }}
                  initial={{ opacity: 0, x: -30 }}
                  whileInView={{ opacity: 1, x: 0 }}
                  transition={{ delay: idx * 0.1 }}
                >
                  <span className="font-mono text-sm text-slate-400 min-w-fit">
                    {new Date(entry.timestampNs / 1_000_000).toLocaleTimeString()}
                  </span>
                  <div className="flex-1">
                    <p className="text-white font-semibold">{entry.reason}</p>
                  </div>
                  <motion.div
                    className={`px-4 py-2 rounded-lg text-sm font-bold bg-red-500/20 border border-red-500/30 text-red-300`}
                    animate={{ scale: [1, 1.05, 1] }}
                    transition={{ duration: 2, repeat: Infinity, delay: idx * 0.1 }}
                  >
                    CRITICAL
                  </motion.div>
                </motion.div>
              ))
            ) : (
              <div className="py-8 text-center bg-slate-900/30 rounded-xl border border-slate-800">
                <p className="text-slate-500 italic">Listening for security events...</p>
              </div>
            )}
          </div>
        </motion.div>

        {/* 🔥 SCROLL CONTENT SECTION 3 - Policy Compliance */}
        <motion.div
          variants={itemVariants}
          className="mt-16"
        >
          <h3 className="text-2xl md:text-3xl font-bold text-white mb-6">Compliance Status</h3>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            {[
              { policy: 'GDPR', status: 'Compliant', percentage: 95 },
              { policy: 'HIPAA', status: 'Compliant', percentage: 98 },
              { policy: 'PCI-DSS', status: 'Needs Review', percentage: 87 },
            ].map((item, idx) => (
              <motion.div
                key={idx}
                className="rounded-2xl p-8"
                style={{
                  background: 'linear-gradient(135deg, rgba(51, 65, 85, 0.4), rgba(71, 85, 105, 0.3))',
                  border: '2px solid rgba(148, 163, 184, 0.2)',
                  backdropFilter: 'blur(10px)',
                }}
                initial={{ opacity: 0, y: 30 }}
                whileInView={{ opacity: 1, y: 0 }}
                transition={{ delay: idx * 0.2 }}
              >
                <h4 className="text-xl font-bold text-white mb-4">{item.policy}</h4>
                <div className="w-full bg-slate-800/50 rounded-full h-3 mb-4 overflow-hidden border border-slate-700/30">
                  <motion.div
                    className="h-full bg-gradient-to-r from-green-400 to-emerald-500 rounded-full"
                    initial={{ width: 0 }}
                    whileInView={{ width: `${item.percentage}%` }}
                    transition={{ duration: 1, delay: idx * 0.3 }}
                  />
                </div>
                <div className="flex justify-between items-center">
                  <span className={`text-sm font-semibold ${item.status === 'Compliant' ? 'text-green-400' : 'text-yellow-400'}`}>
                    {item.status}
                  </span>
                  <span className="text-slate-300 font-bold">{item.percentage}%</span>
                </div>
              </motion.div>
            ))}
          </div>
        </motion.div>

        {/* 🔥 SCROLL CONTENT SECTION 4 - Performance Metrics */}
        <motion.div
          variants={itemVariants}
          className="mt-16 mb-20"
        >
          <h3 className="text-2xl md:text-3xl font-bold text-white mb-6">System Performance</h3>
          <div className="space-y-6">
            {[
              { metric: 'Average Detection Time', value: '156ms', color: 'blue', icon: '⚡' },
              { metric: 'False Positive Rate', value: '2.3%', color: 'green', icon: '✓' },
              { metric: 'Threat Response Time', value: '342ms', color: 'purple', icon: '🔒' },
              { metric: 'System Uptime', value: '99.98%', color: 'emerald', icon: '📈' },
            ].map((item, idx) => (
              <motion.div
                key={idx}
                className="flex items-center justify-between p-6 rounded-xl border border-slate-700/30"
                style={{
                  background: `linear-gradient(135deg, rgba(51, 65, 85, 0.4), rgba(71, 85, 105, 0.3))`,
                }}
                initial={{ opacity: 0, x: -30 }}
                whileInView={{ opacity: 1, x: 0 }}
                transition={{ delay: idx * 0.15 }}
                whileHover={{ scale: 1.02, borderColor: 'rgba(59, 130, 246, 0.4)' }}
              >
                <div className="flex items-center gap-4">
                  <span className="text-3xl">{item.icon}</span>
                  <div>
                    <p className="text-slate-400 text-sm">{item.metric}</p>
                    <p className="text-white text-lg font-bold">{item.value}</p>
                  </div>
                </div>
                <motion.div
                  className={`px-6 py-3 rounded-lg bg-${item.color}-500/20 border border-${item.color}-500/30`}
                  animate={{ scale: [1, 1.1, 1] }}
                  transition={{ duration: 3, repeat: Infinity, delay: idx * 0.3 }}
                >
                  <span className={`text-${item.color}-300 font-bold text-sm`}>Active</span>
                </motion.div>
              </motion.div>
            ))}
          </div>
        </motion.div>
      </div>
    </motion.div>
  )
}

export default Dashboard
