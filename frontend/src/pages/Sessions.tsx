import { motion } from 'framer-motion'
import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  Search,
  Filter,
  Clock,
  AlertTriangle,
  CheckCircle,
  AlertCircle,
  TrendingUp,
  Eye,
  Download,
  Trash2,
} from 'lucide-react'

const Sessions = () => {
  const navigate = useNavigate()
  const [sessions, setSessions] = useState<any[]>([])
  const [filteredSessions, setFilteredSessions] = useState<any[]>([])
  const [loading, setLoading] = useState(true)
  const [searchQuery, setSearchQuery] = useState('')
  const [statusFilter, setStatusFilter] = useState<string>('all')
  const [selectedSession, setSelectedSession] = useState<any | null>(null)

  useEffect(() => {
    // Fetch real data from the Forensics Service
    fetch('http://localhost:8083/forensics/query/sessions')
      .then(response => response.json())
      .then(data => {
        // Transform API response to match component expectations
        const transformed = data.map((session: any) => ({
          ...session,
          riskLevel: session.maxRiskScore || 0,
          status: session.flagged ? 'compromised' : (session.maxRiskScore > 50 ? 'suspicious' : 'active')
        }))
        setSessions(transformed)
        setFilteredSessions(transformed)
        setLoading(false)
      })
      .catch(error => {
        console.error("Error fetching live sessions:", error)
        setLoading(false)
      })
  }, [])

  // Filter sessions based on search and status
  useEffect(() => {
    let filtered = sessions

    // Filter by status
    if (statusFilter !== 'all') {
      filtered = filtered.filter((s) => s.status?.toLowerCase() === statusFilter.toLowerCase())
    }

    // Filter by search query
    if (searchQuery) {
      filtered = filtered.filter((s) =>
        s.sessionId?.toLowerCase().includes(searchQuery.toLowerCase()) ||
        s.userId?.toLowerCase().includes(searchQuery.toLowerCase())
      )
    }

    setFilteredSessions(filtered)
  }, [searchQuery, statusFilter, sessions])

  const getRiskColor = (risk: number) => {
    if (risk >= 80) return 'from-red-600 to-red-500'
    if (risk >= 50) return 'from-orange-600 to-orange-500'
    if (risk >= 20) return 'from-yellow-600 to-yellow-500'
    return 'from-green-600 to-green-500'
  }

  const getRiskBadgeColor = (risk: number) => {
    if (risk >= 80) return 'bg-red-500/20 border-red-500/30 text-red-300'
    if (risk >= 50) return 'bg-orange-500/20 border-orange-500/30 text-orange-300'
    if (risk >= 20) return 'bg-yellow-500/20 border-yellow-500/30 text-yellow-300'
    return 'bg-green-500/20 border-green-500/30 text-green-300'
  }

  const getStatusIcon = (status: string) => {
    switch (status?.toLowerCase()) {
      case 'active':
        return <CheckCircle className="w-5 h-5 text-green-400" />
      case 'compromised':
        return <AlertTriangle className="w-5 h-5 text-red-400" />
      case 'suspicious':
        return <AlertCircle className="w-5 h-5 text-yellow-400" />
      default:
        return <Clock className="w-5 h-5 text-slate-400" />
    }
  }

  const containerVariants = {
    hidden: { opacity: 0 },
    visible: {
      opacity: 1,
      transition: {
        staggerChildren: 0.05,
        delayChildren: 0.1,
      },
    },
  }

  const itemVariants = {
    hidden: { opacity: 0, y: 20 },
    visible: {
      opacity: 1,
      y: 0,
      transition: { duration: 0.4 },
    },
  }

  return (
    <motion.div
      className="min-h-screen w-full bg-transparent relative"
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
          Active <span className="text-cyan-400">Sessions</span>
        </motion.h1>
        <div className="flex items-center gap-4">
          <motion.button
            whileHover={{ scale: 1.05 }}
            className="px-4 py-2 rounded-lg bg-slate-800 text-slate-300 hover:text-white transition-colors"
          >
            <Download className="w-4 h-4" />
          </motion.button>
        </div>
      </motion.div>

      {/* Main Content */}
      <div className="pt-24 px-4 md:px-8 pb-40 w-full">
        {/* Search and Filter Section */}
        <motion.div
          variants={itemVariants}
          className="mb-8 flex flex-col gap-4 md:flex-row md:items-center md:justify-between"
        >
          {/* Search */}
          <div className="flex-1 relative">
            <Search className="absolute left-4 top-3 w-5 h-5 text-slate-400" />
            <input
              type="text"
              placeholder="Search by Session ID or User ID..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="w-full pl-12 pr-4 py-2 rounded-lg bg-slate-800/50 border border-slate-700/50 text-white placeholder-slate-400 focus:outline-none focus:border-cyan-500/50 transition-colors"
            />
          </div>

          {/* Status Filter */}
          <div className="flex gap-2">
            {['all', 'active', 'suspicious', 'compromised'].map((status) => (
              <motion.button
                key={status}
                whileHover={{ scale: 1.05 }}
                whileTap={{ scale: 0.95 }}
                onClick={() => setStatusFilter(status)}
                className={`px-4 py-2 rounded-lg capitalize transition-all font-medium text-sm ${
                  statusFilter === status
                    ? 'bg-cyan-600 text-white shadow-lg shadow-cyan-500/30'
                    : 'bg-slate-800 text-slate-300 hover:text-white'
                }`}
              >
                {status}
              </motion.button>
            ))}
          </div>
        </motion.div>

        {/* Session Count */}
        <motion.div
          variants={itemVariants}
          className="mb-6 flex items-center justify-between"
        >
          <p className="text-slate-300">
            Showing <span className="text-cyan-400 font-bold">{filteredSessions.length}</span> of{' '}
            <span className="text-slate-400">{sessions.length}</span> sessions
          </p>
        </motion.div>

        {/* Sessions Grid */}
        {loading ? (
          <motion.div
            variants={itemVariants}
            className="text-center py-16"
          >
            <div className="inline-block">
              <motion.div
                className="w-12 h-12 border-2 border-cyan-500/30 border-t-cyan-500 rounded-full"
                animate={{ rotate: 360 }}
                transition={{ duration: 2, repeat: Infinity, ease: 'linear' }}
              />
            </div>
            <p className="text-slate-400 mt-4">Loading sessions...</p>
          </motion.div>
        ) : filteredSessions.length === 0 ? (
          <motion.div
            variants={itemVariants}
            className="text-center py-16"
          >
            <AlertTriangle className="w-12 h-12 text-slate-600 mx-auto mb-4" />
            <p className="text-slate-400">No sessions found</p>
          </motion.div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {filteredSessions.map((session, idx) => (
              <motion.div
                key={session.sessionId}
                variants={itemVariants}
                whileHover={{ scale: 1.02, translateY: -5 }}
                onClick={() => setSelectedSession(session)}
                className="cursor-pointer group"
              >
                <div
                  className="rounded-2xl p-6 border border-slate-700/50 transition-all duration-300 hover:border-cyan-500/50"
                  style={{
                    background: 'linear-gradient(135deg, rgba(51, 65, 85, 0.4), rgba(71, 85, 105, 0.3))',
                    backdropFilter: 'blur(10px)',
                  }}
                >
                  {/* Session Header */}
                  <div className="flex items-start justify-between mb-4">
                    <div className="flex-1">
                      <p className="text-xs text-slate-400 mb-1">Session ID</p>
                      <p className="text-sm md:text-base font-mono text-cyan-300 truncate">
                        {session.sessionId?.substring(0, 12)}...
                      </p>
                    </div>
                    <div className="flex items-center gap-1">
                      {getStatusIcon(session.status)}
                    </div>
                  </div>

                  {/* User ID */}
                  <div className="mb-4">
                    <p className="text-xs text-slate-400 mb-1">User ID</p>
                    <p className="text-sm text-slate-200">{session.userId || 'Unknown'}</p>
                  </div>

                  {/* Risk Level Bar */}
                  <div className="mb-4">
                    <div className="flex justify-between items-center mb-2">
                      <p className="text-xs text-slate-400">Risk Level</p>
                      <span
                        className={`text-sm font-bold bg-gradient-to-r ${getRiskColor(
                          session.riskLevel || 0
                        )} bg-clip-text text-transparent`}
                      >
                        {session.riskLevel || 0}%
                      </span>
                    </div>
                    <div className="w-full bg-slate-800/50 rounded-full h-2 border border-slate-700/30 overflow-hidden">
                      <motion.div
                        className={`h-full bg-gradient-to-r ${getRiskColor(session.riskLevel || 0)}`}
                        initial={{ width: 0 }}
                        whileInView={{ width: `${session.riskLevel || 0}%` }}
                        transition={{ duration: 1, delay: idx * 0.1 }}
                      />
                    </div>
                  </div>

                  {/* Stats Grid */}
                  <div className="grid grid-cols-2 gap-3 mb-4">
                    {/* Events */}
                    <div
                      className="rounded-lg p-3 border border-slate-700/30"
                      style={{
                        background: 'linear-gradient(135deg, rgba(30, 41, 59, 0.6), rgba(15, 23, 42, 0.4))',
                      }}
                    >
                      <p className="text-xs text-slate-400 mb-1">Events</p>
                      <p className="text-lg font-bold text-blue-300">
                        {session.eventCount || 0}
                      </p>
                    </div>

                    {/* Duration */}
                    <div
                      className="rounded-lg p-3 border border-slate-700/30"
                      style={{
                        background: 'linear-gradient(135deg, rgba(30, 41, 59, 0.6), rgba(15, 23, 42, 0.4))',
                      }}
                    >
                      <p className="text-xs text-slate-400 mb-1">Duration</p>
                      <p className="text-lg font-bold text-purple-300">
                        {session.duration ? `${Math.round(session.duration / 1000)}s` : '—'}
                      </p>
                    </div>
                  </div>

                  {/* Status Badge */}
                  <div className="flex items-center justify-between">
                    <span
                      className={`text-xs font-semibold px-3 py-1 rounded-full border capitalize ${getRiskBadgeColor(
                        session.riskLevel || 0
                      )}`}
                    >
                      {session.status || 'Unknown'}
                    </span>
                    <p className="text-xs text-slate-400">
                      {session.timestamp
                        ? new Date(session.timestamp).toLocaleDateString()
                        : '—'}
                    </p>
                  </div>

                  {/* Action Buttons */}
                  <div className="mt-4 flex gap-2 opacity-0 group-hover:opacity-100 transition-opacity">
                    <motion.button
                      whileHover={{ scale: 1.05 }}
                      onClick={(e) => {
                        e.stopPropagation()
                        navigate(`/sessions/${session.sessionId}`)
                      }}
                      className="flex-1 px-3 py-2 rounded-lg bg-cyan-600/20 border border-cyan-500/30 text-cyan-300 hover:bg-cyan-600/30 transition-colors text-sm font-medium flex items-center justify-center gap-2"
                    >
                      <Eye className="w-4 h-4" />
                      View
                    </motion.button>
                    <motion.button
                      whileHover={{ scale: 1.05 }}
                      className="flex-1 px-3 py-2 rounded-lg bg-red-600/20 border border-red-500/30 text-red-300 hover:bg-red-600/30 transition-colors text-sm font-medium flex items-center justify-center gap-2"
                    >
                      <Trash2 className="w-4 h-4" />
                    </motion.button>
                  </div>
                </div>
              </motion.div>
            ))}
          </div>
        )}

        {/* Session Details Modal */}
        {selectedSession && (
          <motion.div
            className="fixed inset-0 bg-black/50 flex items-center justify-center p-4 z-50 backdrop-blur-sm"
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            onClick={() => setSelectedSession(null)}
          >
            <motion.div
              className="bg-slate-900 rounded-2xl p-8 max-w-2xl w-full border border-slate-800"
              initial={{ scale: 0.9, opacity: 0 }}
              animate={{ scale: 1, opacity: 1 }}
              onClick={(e) => e.stopPropagation()}
            >
              <div className="flex items-center justify-between mb-6">
                <h2 className="text-2xl font-bold text-white">Session Details</h2>
                <motion.button
                  whileHover={{ scale: 1.1 }}
                  onClick={() => setSelectedSession(null)}
                  className="text-slate-400 hover:text-white"
                >
                  ✕
                </motion.button>
              </div>

              <div className="space-y-4 mb-6">
                <div>
                  <p className="text-slate-400 text-sm mb-1">Session ID</p>
                  <p className="text-white font-mono">{selectedSession.sessionId}</p>
                </div>
                <div>
                  <p className="text-slate-400 text-sm mb-1">User ID</p>
                  <p className="text-white">{selectedSession.userId}</p>
                </div>
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <p className="text-slate-400 text-sm mb-1">Status</p>
                    <p className="text-white capitalize">{selectedSession.status}</p>
                  </div>
                  <div>
                    <p className="text-slate-400 text-sm mb-1">Risk Level</p>
                    <p className="text-white">{selectedSession.riskLevel}%</p>
                  </div>
                </div>
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <p className="text-slate-400 text-sm mb-1">Events</p>
                    <p className="text-white">{selectedSession.eventCount}</p>
                  </div>
                  <div>
                    <p className="text-slate-400 text-sm mb-1">Timestamp</p>
                    <p className="text-white text-sm">
                      {new Date(selectedSession.timestamp).toLocaleString()}
                    </p>
                  </div>
                </div>
              </div>

              <div className="flex gap-3">
                <motion.button
                  whileHover={{ scale: 1.05 }}
                  className="flex-1 px-4 py-2 rounded-lg bg-cyan-600 text-white hover:bg-cyan-700 transition-colors font-medium"
                >
                  View Replay
                </motion.button>
                <motion.button
                  whileHover={{ scale: 1.05 }}
                  onClick={() => setSelectedSession(null)}
                  className="flex-1 px-4 py-2 rounded-lg bg-slate-800 text-slate-300 hover:bg-slate-700 transition-colors font-medium"
                >
                  Close
                </motion.button>
              </div>
            </motion.div>
          </motion.div>
        )}
      </div>
    </motion.div>
  )
}

export default Sessions
