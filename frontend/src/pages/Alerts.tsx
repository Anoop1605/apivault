import React, { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { motion, AnimatePresence } from 'framer-motion'
import { ShieldAlert, AlertTriangle, Play, ChevronRight, Filter, ShieldCheck, X } from 'lucide-react'

// --- Mock API Layer ---

interface AlertData {
  id: string
  type: string
  severity: 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW'
  sessionId: string
  risk: number
  timestamp: string
  status: 'ACTIVE' | 'RESOLVED'
  description?: string
  rulesTriggered?: string[]
  riskTimeline?: number[]
}

const fetchAlerts = async (): Promise<AlertData[]> => {
  try {
    // Adjust URL if your Alerts are served from a different port/path
    const response = await fetch('http://localhost:8083/forensics/query/alerts')
    if (!response.ok) return []
    return await response.json()
  } catch (error) {
    console.error("Error fetching live alerts:", error)
    return []
  }
}

// --- Component ---

export const Alerts: React.FC = () => {
  const navigate = useNavigate()
  const { data: alerts, isLoading } = useQuery({
    queryKey: ['alerts'],
    queryFn: fetchAlerts,
  })

  const [selectedAlert, setSelectedAlert] = useState<AlertData | null>(null)
  
  // Filters
  const [filterSeverity, setFilterSeverity] = useState<string>('ALL')
  const [filterStatus, setFilterStatus] = useState<string>('ACTIVE')

  if (isLoading) {
    return (
      <div className="w-full h-screen flex items-center justify-center bg-slate-950">
        <motion.div animate={{ rotate: 360 }} transition={{ duration: 1, repeat: Infinity, ease: 'linear' }} className="w-12 h-12 border-4 border-slate-800 border-t-cyan-500 rounded-full" />
      </div>
    )
  }

  // Derived Data
  const activeAlerts = alerts?.filter(a => a.status === 'ACTIVE') || []
  const filteredAlerts = alerts?.filter(a => {
    if (filterStatus !== 'ALL' && a.status !== filterStatus) return false
    if (filterSeverity !== 'ALL' && a.severity !== filterSeverity) return false
    return true
  }) || []

  const criticalAlerts = activeAlerts.filter(a => a.severity === 'CRITICAL')
  const otherAlerts = filteredAlerts.filter(a => a.severity !== 'CRITICAL')

  // Helpers
  const getSeverityColor = (severity: string) => {
    switch(severity) {
      case 'CRITICAL': return 'text-rose-500 bg-rose-500/10 border-rose-500/20'
      case 'HIGH': return 'text-orange-500 bg-orange-500/10 border-orange-500/20'
      case 'MEDIUM': return 'text-amber-500 bg-amber-500/10 border-amber-500/20'
      default: return 'text-blue-500 bg-blue-500/10 border-blue-500/20'
    }
  }

  const formatTimeAgo = (dateStr: string) => {
    const diff = Math.floor((Date.now() - new Date(dateStr).getTime()) / 60000)
    if (diff < 60) return `${diff}m ago`
    return `${Math.floor(diff/60)}h ago`
  }

  return (
    <div className="min-h-screen bg-slate-950 relative overflow-hidden flex flex-col">
      {/* Background gradients */}
      <div className="absolute top-0 left-0 w-full h-full overflow-hidden -z-10 pointer-events-none">
         <div className="absolute top-[-10%] left-[-10%] w-[40%] h-[40%] rounded-full bg-cyan-900/10 blur-[120px]" />
         <div className="absolute bottom-[-10%] right-[-10%] w-[40%] h-[40%] rounded-full bg-rose-900/5 blur-[120px]" />
      </div>

      {/* Header & Filters */}
      <div className="border-b border-slate-800/50 bg-slate-950/50 backdrop-blur-xl sticky top-0 z-30">
        <div className="max-w-7xl mx-auto px-8 py-6">
          <div className="flex flex-col md:flex-row md:items-center justify-between gap-6">
            <div>
              <h1 className="text-3xl font-bold text-white flex items-center gap-3">
                <ShieldAlert className="text-cyan-400" size={32} />
                Alerts
              </h1>
              <p className="text-slate-400 mt-1">{activeAlerts.length} Active Alerts requiring attention</p>
            </div>
            
            <div className="flex items-center gap-4">
              <div className="flex items-center gap-2 bg-slate-900 rounded-lg p-1 border border-slate-800">
                <Filter size={16} className="text-slate-500 ml-2" />
                <select 
                  className="bg-transparent text-sm text-slate-300 focus:outline-none py-1 pr-4 cursor-pointer"
                  value={filterSeverity}
                  onChange={e => setFilterSeverity(e.target.value)}
                >
                  <option value="ALL">All Severities</option>
                  <option value="CRITICAL">Critical</option>
                  <option value="HIGH">High</option>
                  <option value="MEDIUM">Medium</option>
                </select>
                <div className="w-px h-4 bg-slate-700" />
                <select 
                  className="bg-transparent text-sm text-slate-300 focus:outline-none py-1 px-2 cursor-pointer"
                  value={filterStatus}
                  onChange={e => setFilterStatus(e.target.value)}
                >
                  <option value="ALL">All Status</option>
                  <option value="ACTIVE">Active</option>
                  <option value="RESOLVED">Resolved</option>
                </select>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div className="flex-1 overflow-y-auto">
        <div className="max-w-7xl mx-auto px-8 py-8 space-y-12">
          
          {/* Critical Alerts Section */}
          {criticalAlerts.length > 0 && (
            <section>
              <h2 className="text-sm font-bold text-rose-500 uppercase tracking-widest mb-4 flex items-center gap-2">
                <AlertTriangle size={16} /> Top Priority
              </h2>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                {criticalAlerts.map(alert => (
                  <motion.div 
                    key={alert.id}
                    whileHover={{ scale: 1.01 }}
                    className="relative rounded-2xl overflow-hidden group cursor-pointer"
                    onClick={() => setSelectedAlert(alert)}
                  >
                    {/* Glowing effect */}
                    <div className="absolute inset-0 bg-gradient-to-r from-rose-500/20 to-orange-500/5 group-hover:opacity-100 transition-opacity" />
                    <div className="absolute inset-0 border border-rose-500/30 rounded-2xl" />
                    
                    <div className="relative p-6 bg-slate-900/80 backdrop-blur-sm">
                      <div className="flex justify-between items-start mb-4">
                        <div className="flex items-center gap-3">
                           <span className={`px-2.5 py-1 text-xs font-bold rounded ${getSeverityColor(alert.severity)}`}>
                             {alert.severity}
                           </span>
                           <h3 className="text-xl font-semibold text-white">{alert.type}</h3>
                        </div>
                        <span className="text-slate-500 text-sm">{formatTimeAgo(alert.timestamp)}</span>
                      </div>
                      
                      <div className="grid grid-cols-2 gap-4 mb-6 text-sm">
                        <div className="bg-slate-950/50 rounded-lg p-3 border border-slate-800">
                           <span className="text-slate-500 block mb-1">Session</span>
                           <span className="text-cyan-400 font-mono">{alert.sessionId}</span>
                        </div>
                        <div className="bg-slate-950/50 rounded-lg p-3 border border-slate-800">
                           <span className="text-slate-500 block mb-1">Risk Level</span>
                           <span className="text-rose-400 font-mono text-lg">{alert.risk}%</span>
                        </div>
                      </div>

                      <div className="flex gap-3">
                         <button 
                           onClick={(e) => { e.stopPropagation(); navigate(`/replay/${alert.sessionId}`) }}
                           className="flex-1 py-2 bg-slate-800 hover:bg-slate-700 text-white rounded-lg flex items-center justify-center gap-2 text-sm font-medium transition-colors border border-slate-700"
                         >
                           <Play size={16} /> Replay
                         </button>
                         <button 
                           onClick={(e) => { e.stopPropagation(); setSelectedAlert(alert) }}
                           className="flex-1 py-2 bg-rose-500/10 hover:bg-rose-500/20 text-rose-300 rounded-lg flex items-center justify-center gap-2 text-sm font-medium transition-colors border border-rose-500/20"
                         >
                           Analyze <ChevronRight size={16} />
                         </button>
                      </div>
                    </div>
                  </motion.div>
                ))}
              </div>
            </section>
          )}

          {/* Alert List Section */}
          <section>
            <h2 className="text-sm font-bold text-slate-400 uppercase tracking-widest mb-4">Recent Alerts</h2>
            
            <div className="bg-slate-900/50 border border-slate-800 rounded-xl overflow-hidden">
              <table className="w-full text-left text-sm text-slate-300">
                <thead className="bg-slate-900 border-b border-slate-800 text-slate-500 uppercase tracking-wider text-xs">
                  <tr>
                    <th className="px-6 py-4 font-medium">Type</th>
                    <th className="px-6 py-4 font-medium">Severity</th>
                    <th className="px-6 py-4 font-medium">Session</th>
                    <th className="px-6 py-4 font-medium">Risk</th>
                    <th className="px-6 py-4 font-medium">Time</th>
                    <th className="px-6 py-4 font-medium text-right">Action</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-800/50">
                  {otherAlerts.map(alert => (
                    <motion.tr 
                      key={alert.id}
                      initial={{ opacity: 0 }} animate={{ opacity: 1 }}
                      className="hover:bg-slate-800/30 transition-colors group cursor-pointer"
                      onClick={() => setSelectedAlert(alert)}
                    >
                      <td className="px-6 py-4 font-medium text-slate-200">{alert.type}</td>
                      <td className="px-6 py-4">
                         <span className={`px-2 py-1 text-[10px] font-bold rounded ${getSeverityColor(alert.severity)}`}>
                           {alert.severity}
                         </span>
                      </td>
                      <td className="px-6 py-4 font-mono text-cyan-400/80 group-hover:text-cyan-400">{alert.sessionId}</td>
                      <td className="px-6 py-4 font-mono text-amber-400/80">{alert.risk}%</td>
                      <td className="px-6 py-4 text-slate-500">{formatTimeAgo(alert.timestamp)}</td>
                      <td className="px-6 py-4 text-right">
                         <button 
                           onClick={(e) => { e.stopPropagation(); navigate(`/replay/${alert.sessionId}`) }}
                           className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-md bg-slate-800 hover:bg-slate-700 text-slate-300 hover:text-white transition-colors"
                         >
                           <Play size={14} fill="currentColor" /> Replay
                         </button>
                      </td>
                    </motion.tr>
                  ))}
                  {otherAlerts.length === 0 && (
                     <tr>
                        <td colSpan={6} className="px-6 py-12 text-center text-slate-500">
                           <ShieldCheck size={48} className="mx-auto mb-3 opacity-20" />
                           No alerts match your current filters.
                        </td>
                     </tr>
                  )}
                </tbody>
              </table>
            </div>
          </section>

        </div>
      </div>

      {/* Slide-over Details Panel */}
      <AnimatePresence>
        {selectedAlert && (
          <>
            {/* Backdrop */}
            <motion.div 
              initial={{ opacity: 0 }} animate={{ opacity: 1 }} exit={{ opacity: 0 }}
              className="fixed inset-0 bg-slate-950/60 backdrop-blur-sm z-40"
              onClick={() => setSelectedAlert(null)}
            />
            
            {/* Panel */}
            <motion.div
              initial={{ x: '100%' }} animate={{ x: 0 }} exit={{ x: '100%' }}
              transition={{ type: "spring", damping: 25, stiffness: 200 }}
              className="fixed right-0 top-0 h-full w-full max-w-md bg-slate-900 border-l border-slate-800 shadow-2xl z-50 flex flex-col"
            >
              <div className="p-6 border-b border-slate-800 flex justify-between items-center bg-slate-950/50">
                <div className="flex items-center gap-3">
                   <span className={`px-2 py-1 text-xs font-bold rounded ${getSeverityColor(selectedAlert.severity)}`}>
                     {selectedAlert.severity}
                   </span>
                   <h3 className="font-semibold text-white">Alert Details</h3>
                </div>
                <button 
                  onClick={() => setSelectedAlert(null)}
                  className="p-2 text-slate-400 hover:text-white hover:bg-slate-800 rounded-lg transition-colors"
                >
                  <X size={20} />
                </button>
              </div>

              <div className="flex-1 overflow-y-auto p-6 space-y-8">
                <div>
                  <h2 className="text-2xl font-bold text-white mb-2">{selectedAlert.type}</h2>
                  <p className="text-slate-400">{selectedAlert.description}</p>
                </div>

                <div className="grid grid-cols-2 gap-4">
                   <div className="bg-slate-950 rounded-xl p-4 border border-slate-800">
                      <span className="text-slate-500 text-xs uppercase tracking-wider block mb-1">Session ID</span>
                      <span className="text-cyan-400 font-mono cursor-pointer hover:underline" onClick={() => navigate(`/sessions/${selectedAlert.sessionId}`)}>{selectedAlert.sessionId}</span>
                   </div>
                   <div className="bg-slate-950 rounded-xl p-4 border border-slate-800">
                      <span className="text-slate-500 text-xs uppercase tracking-wider block mb-1">Final Risk</span>
                      <span className="text-rose-400 font-mono text-xl">{selectedAlert.risk}%</span>
                   </div>
                </div>

                {selectedAlert.rulesTriggered && (
                  <div>
                    <h4 className="text-sm font-semibold text-white mb-3">Rules Triggered</h4>
                    <div className="flex flex-wrap gap-2">
                      {selectedAlert.rulesTriggered.map(rule => (
                        <span key={rule} className="px-3 py-1.5 bg-slate-800 border border-slate-700 rounded-md text-xs font-mono text-slate-300">
                          {rule}
                        </span>
                      ))}
                    </div>
                  </div>
                )}

                {selectedAlert.riskTimeline && (
                  <div>
                    <h4 className="text-sm font-semibold text-white mb-3">Risk Progression</h4>
                    <div className="flex items-end gap-2 h-24 bg-slate-950 p-4 rounded-xl border border-slate-800">
                       {selectedAlert.riskTimeline.map((val, idx) => (
                         <div key={idx} className="flex-1 flex flex-col justify-end group relative">
                           <motion.div 
                             initial={{ height: 0 }}
                             animate={{ height: `${val}%` }}
                             className={`w-full rounded-t-sm ${val > 50 ? 'bg-rose-500' : 'bg-indigo-500'}`}
                           />
                           {/* Tooltip */}
                           <div className="absolute bottom-full mb-2 left-1/2 -translate-x-1/2 opacity-0 group-hover:opacity-100 bg-slate-800 text-xs px-2 py-1 rounded transition-opacity pointer-events-none z-10 whitespace-nowrap">
                              Step {idx + 1}: {val}%
                           </div>
                         </div>
                       ))}
                    </div>
                  </div>
                )}
              </div>

              <div className="p-6 border-t border-slate-800 bg-slate-950/50 flex flex-col gap-3">
                <button 
                  onClick={() => navigate(`/replay/${selectedAlert.sessionId}`)}
                  className="w-full py-3 bg-slate-800 hover:bg-slate-700 text-white rounded-lg flex items-center justify-center gap-2 font-medium transition-colors"
                >
                  <Play size={18} fill="currentColor" /> Replay Session
                </button>
                <button 
                  onClick={() => navigate(`/simulation/${selectedAlert.sessionId}`)}
                  className="w-full py-3 bg-gradient-to-r from-cyan-600 to-blue-600 hover:from-cyan-500 hover:to-blue-500 text-white rounded-lg flex items-center justify-center gap-2 font-medium transition-all shadow-[0_0_15px_rgba(8,145,178,0.2)]"
                >
                  🧪 What-If Simulation
                </button>
              </div>
            </motion.div>
          </>
        )}
      </AnimatePresence>
    </div>
  )
}
