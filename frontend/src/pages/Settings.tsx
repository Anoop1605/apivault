import React, { useState, useEffect } from 'react'
import { motion, AnimatePresence } from 'framer-motion'
import { 
  Settings as SettingsIcon, Shield, Sliders, Bell, 
  Unplug, User, Save, RefreshCw, CheckCircle2, ChevronRight
} from 'lucide-react'

// --- Types ---
type Tab = 'general' | 'security' | 'simulation' | 'alerts' | 'integrations' | 'account'

interface AppSettings {
  // General
  theme: 'dark' | 'light'
  timeFormat: '12h' | '24h'
  defaultPage: 'dashboard' | 'sessions'
  
  // Security Rules
  blockSql: boolean
  detectXss: boolean
  rateLimiting: boolean
  inputSanitization: boolean
  customRule: string
  
  // Simulation Config
  riskThreshold: number
  simMode: 'strict' | 'balanced' | 'lenient'
  autoStop: boolean
  highlightDivergence: boolean
  
  // Alerts
  enableCritical: boolean
  emailNotifications: boolean
  slackIntegration: boolean
  alertThreshold: number
}

const DEFAULT_SETTINGS: AppSettings = {
  theme: 'dark',
  timeFormat: '24h',
  defaultPage: 'dashboard',
  blockSql: true,
  detectXss: true,
  rateLimiting: false,
  inputSanitization: true,
  customRule: 'IF query contains "DROP" -> BLOCK',
  riskThreshold: 50,
  simMode: 'balanced',
  autoStop: true,
  highlightDivergence: true,
  enableCritical: true,
  emailNotifications: true,
  slackIntegration: false,
  alertThreshold: 70
}

export const Settings: React.FC = () => {
  const [activeTab, setActiveTab] = useState<Tab>('security')
  
  // Settings State
  const [settings, setSettings] = useState<AppSettings>(DEFAULT_SETTINGS)
  const [isDirty, setIsDirty] = useState(false)
  const [saveStatus, setSaveStatus] = useState<'idle' | 'saving' | 'saved'>('idle')

  // Handle setting change
  const updateSetting = <K extends keyof AppSettings>(key: K, value: AppSettings[K]) => {
    setSettings(prev => ({ ...prev, [key]: value }))
    setIsDirty(true)
    setSaveStatus('idle')
  }

  // Handle save
  const handleSave = () => {
    setSaveStatus('saving')
    // Simulate API call
    setTimeout(() => {
      setSaveStatus('saved')
      setIsDirty(false)
      setTimeout(() => setSaveStatus('idle'), 3000)
    }, 800)
  }

  // Handle reset
  const handleReset = () => {
    setSettings(DEFAULT_SETTINGS)
    setIsDirty(true)
  }

  // --- Reusable UI Components ---

  const Toggle = ({ label, description, checked, onChange }: any) => (
    <div className="flex items-center justify-between p-4 bg-slate-900/50 border border-slate-800 rounded-xl hover:bg-slate-800/50 transition-colors">
      <div>
        <h4 className="text-white font-medium">{label}</h4>
        {description && <p className="text-sm text-slate-400 mt-1">{description}</p>}
      </div>
      <button 
        onClick={() => onChange(!checked)}
        className={`relative inline-flex h-6 w-11 items-center rounded-full transition-colors focus:outline-none focus:ring-2 focus:ring-cyan-500 focus:ring-offset-2 focus:ring-offset-slate-900 ${checked ? 'bg-cyan-500' : 'bg-slate-700'}`}
      >
        <span className={`inline-block h-4 w-4 transform rounded-full bg-white transition-transform ${checked ? 'translate-x-6' : 'translate-x-1'}`} />
        {checked && (
          <motion.div 
            initial={{ scale: 0, opacity: 0 }} 
            animate={{ scale: 1.5, opacity: 0 }} 
            transition={{ duration: 0.5 }}
            className="absolute inset-0 bg-cyan-400 rounded-full" 
          />
        )}
      </button>
    </div>
  )

  // --- Tab Contents ---

  const renderGeneral = () => (
    <motion.div initial={{ opacity: 0, y: 10 }} animate={{ opacity: 1, y: 0 }} className="space-y-6">
      <h3 className="text-xl font-bold text-white mb-6">General Settings</h3>
      
      <div className="space-y-4">
        <div className="p-4 bg-slate-900/50 border border-slate-800 rounded-xl">
          <label className="block text-sm font-medium text-slate-400 mb-2">Theme Preference</label>
          <select 
            value={settings.theme} onChange={e => updateSetting('theme', e.target.value as any)}
            className="w-full bg-slate-950 border border-slate-700 rounded-lg p-3 text-white focus:outline-none focus:border-cyan-500"
          >
            <option value="dark">Dark Mode</option>
            <option value="light">Light Mode</option>
          </select>
        </div>

        <div className="p-4 bg-slate-900/50 border border-slate-800 rounded-xl">
          <label className="block text-sm font-medium text-slate-400 mb-2">Default Landing Page</label>
          <select 
            value={settings.defaultPage} onChange={e => updateSetting('defaultPage', e.target.value as any)}
            className="w-full bg-slate-950 border border-slate-700 rounded-lg p-3 text-white focus:outline-none focus:border-cyan-500"
          >
            <option value="dashboard">Dashboard Overview</option>
            <option value="sessions">Session Explorer</option>
          </select>
        </div>
      </div>
    </motion.div>
  )

  const renderSecurity = () => (
    <motion.div initial={{ opacity: 0, y: 10 }} animate={{ opacity: 1, y: 0 }} className="space-y-6">
      <div className="flex items-center gap-3 mb-6">
        <div className="p-2 bg-rose-500/10 rounded-lg">
          <Shield className="text-rose-400" size={24} />
        </div>
        <div>
          <h3 className="text-xl font-bold text-white">Security Rules</h3>
          <p className="text-sm text-slate-400">Core threat detection engine parameters</p>
        </div>
      </div>

      <div className="space-y-4">
        <Toggle 
          label="Block SQL Keywords" 
          description="Automatically drop requests containing common SQL injection signatures."
          checked={settings.blockSql} onChange={(v: boolean) => updateSetting('blockSql', v)} 
        />
        <Toggle 
          label="Detect XSS Patterns" 
          description="Scan and flag cross-site scripting tags in payloads."
          checked={settings.detectXss} onChange={(v: boolean) => updateSetting('detectXss', v)} 
        />
        <Toggle 
          label="Rate Limiting" 
          description="Throttle excessive requests from individual IPs."
          checked={settings.rateLimiting} onChange={(v: boolean) => updateSetting('rateLimiting', v)} 
        />
        <Toggle 
          label="Input Sanitization" 
          description="Strip malicious characters before downstream processing."
          checked={settings.inputSanitization} onChange={(v: boolean) => updateSetting('inputSanitization', v)} 
        />

        <div className="p-5 bg-slate-900/50 border border-slate-800 rounded-xl mt-8">
          <h4 className="text-white font-medium mb-1">Advanced Custom Rule</h4>
          <p className="text-sm text-slate-400 mb-4">Define a regex or behavioral rule.</p>
          <textarea 
            value={settings.customRule}
            onChange={e => updateSetting('customRule', e.target.value)}
            className="w-full h-24 bg-slate-950 border border-slate-700 rounded-lg p-4 text-cyan-400 font-mono text-sm focus:outline-none focus:border-cyan-500 transition-colors resize-none"
          />
        </div>
      </div>
    </motion.div>
  )

  const renderSimulation = () => (
    <motion.div initial={{ opacity: 0, y: 10 }} animate={{ opacity: 1, y: 0 }} className="space-y-6">
      <div className="flex items-center gap-3 mb-6">
        <div className="p-2 bg-indigo-500/10 rounded-lg">
          <Sliders className="text-indigo-400" size={24} />
        </div>
        <div>
          <h3 className="text-xl font-bold text-white">Simulation Config</h3>
          <p className="text-sm text-slate-400">Controls the "What-If" forensic engine</p>
        </div>
      </div>

      <div className="space-y-6">
        <div className="p-6 bg-slate-900/50 border border-slate-800 rounded-xl">
          <div className="flex justify-between items-center mb-4">
            <h4 className="text-white font-medium">Global Risk Threshold</h4>
            <span className="px-3 py-1 bg-indigo-500/20 text-indigo-300 rounded text-sm font-mono">{settings.riskThreshold}%</span>
          </div>
          <input 
            type="range" min="0" max="100" 
            value={settings.riskThreshold}
            onChange={e => updateSetting('riskThreshold', parseInt(e.target.value))}
            className="w-full h-2 bg-slate-800 rounded-lg appearance-none cursor-pointer accent-indigo-500"
          />
        </div>

        <div className="p-6 bg-slate-900/50 border border-slate-800 rounded-xl">
          <h4 className="text-white font-medium mb-4">Simulation Mode</h4>
          <div className="grid grid-cols-3 gap-4">
            {(['strict', 'balanced', 'lenient'] as const).map(mode => (
              <button
                key={mode}
                onClick={() => updateSetting('simMode', mode)}
                className={`py-3 px-4 rounded-lg border capitalize font-medium transition-all ${settings.simMode === mode ? 'bg-indigo-500/20 border-indigo-500 text-indigo-300' : 'bg-slate-950 border-slate-800 text-slate-400 hover:border-slate-600'}`}
              >
                {mode}
              </button>
            ))}
          </div>
        </div>

        <Toggle 
          label="Auto-stop simulation when blocked" 
          description="Halt the replay instantly upon reaching a DENY decision."
          checked={settings.autoStop} onChange={(v: boolean) => updateSetting('autoStop', v)} 
        />
        <Toggle 
          label="Highlight divergence points" 
          description="Visually flash the timeline when simulated behavior differs from original."
          checked={settings.highlightDivergence} onChange={(v: boolean) => updateSetting('highlightDivergence', v)} 
        />
      </div>
    </motion.div>
  )

  const renderAlerts = () => (
    <motion.div initial={{ opacity: 0, y: 10 }} animate={{ opacity: 1, y: 0 }} className="space-y-6">
      <div className="flex items-center gap-3 mb-6">
        <div className="p-2 bg-amber-500/10 rounded-lg">
          <Bell className="text-amber-400" size={24} />
        </div>
        <div>
          <h3 className="text-xl font-bold text-white">Alerts & Notifications</h3>
          <p className="text-sm text-slate-400">Manage communication and thresholds</p>
        </div>
      </div>

      <div className="space-y-4">
        <Toggle 
          label="Enable Critical Alerts" 
          checked={settings.enableCritical} onChange={(v: boolean) => updateSetting('enableCritical', v)} 
        />
        <Toggle 
          label="Email Notifications" 
          checked={settings.emailNotifications} onChange={(v: boolean) => updateSetting('emailNotifications', v)} 
        />
        <Toggle 
          label="Slack Integration" 
          checked={settings.slackIntegration} onChange={(v: boolean) => updateSetting('slackIntegration', v)} 
        />

        <div className="p-6 bg-slate-900/50 border border-slate-800 rounded-xl mt-6">
          <div className="flex justify-between items-center mb-4">
            <h4 className="text-white font-medium">Alert Generation Threshold</h4>
            <span className="text-amber-400 font-mono">&gt;{settings.alertThreshold}%</span>
          </div>
          <p className="text-sm text-slate-400 mb-4">Only notify if calculated risk exceeds this level.</p>
          <input 
            type="range" min="0" max="100" 
            value={settings.alertThreshold}
            onChange={e => updateSetting('alertThreshold', parseInt(e.target.value))}
            className="w-full h-2 bg-slate-800 rounded-lg appearance-none cursor-pointer accent-amber-500"
          />
        </div>
      </div>
    </motion.div>
  )

  const renderIntegrations = () => (
    <motion.div initial={{ opacity: 0, y: 10 }} animate={{ opacity: 1, y: 0 }} className="space-y-6">
      <h3 className="text-xl font-bold text-white mb-6">System Integrations</h3>
      
      <div className="p-6 bg-slate-900/50 border border-slate-800 rounded-xl space-y-4">
        <div>
          <label className="block text-sm font-medium text-slate-400 mb-2">Primary API Endpoint</label>
          <div className="flex gap-3">
            <input 
              type="text" 
              defaultValue="http://localhost:5000/api"
              disabled
              className="flex-1 bg-slate-950 border border-slate-700 rounded-lg p-3 text-slate-500 font-mono focus:outline-none"
            />
            <button className="px-4 bg-slate-800 hover:bg-slate-700 text-white rounded-lg transition-colors whitespace-nowrap">
              Test Connection
            </button>
          </div>
        </div>

        <div>
          <label className="block text-sm font-medium text-slate-400 mb-2">Webhook URL</label>
          <input 
            type="text" 
            placeholder="https://your-domain.com/webhook"
            className="w-full bg-slate-950 border border-slate-700 rounded-lg p-3 text-white focus:outline-none focus:border-cyan-500"
          />
        </div>
      </div>
    </motion.div>
  )

  const tabs: { id: Tab, label: string, icon: React.ReactNode }[] = [
    { id: 'general', label: 'General', icon: <SettingsIcon size={18} /> },
    { id: 'security', label: 'Security Rules', icon: <Shield size={18} /> },
    { id: 'simulation', label: 'Simulation Config', icon: <Sliders size={18} /> },
    { id: 'alerts', label: 'Alerts & Notifications', icon: <Bell size={18} /> },
    { id: 'integrations', label: 'Integrations', icon: <Unplug size={18} /> },
    { id: 'account', label: 'Account', icon: <User size={18} /> },
  ]

  return (
    <div className="min-h-screen bg-slate-950 relative">
      {/* Background Ambience */}
      <div className="absolute top-0 right-0 w-[50%] h-[50%] bg-blue-900/10 blur-[150px] pointer-events-none rounded-full" />
      
      {/* Header */}
      <div className="border-b border-slate-800 bg-slate-950/80 backdrop-blur-xl sticky top-0 z-30">
        <div className="max-w-6xl mx-auto px-8 py-6 flex items-center justify-between">
          <div>
            <h1 className="text-2xl font-bold text-white flex items-center gap-3">
              <SettingsIcon className="text-cyan-400" /> System Control Panel
            </h1>
            <p className="text-sm text-slate-400 mt-1">Configure global behavior, security, and simulations.</p>
          </div>
          
          <div className="flex items-center gap-4">
            <AnimatePresence mode="wait">
              {isDirty && (
                <motion.span 
                  initial={{ opacity: 0, x: 20 }} animate={{ opacity: 1, x: 0 }} exit={{ opacity: 0, x: -20 }}
                  className="text-amber-400 text-sm font-medium flex items-center gap-2"
                >
                  <div className="w-2 h-2 rounded-full bg-amber-400 animate-pulse" />
                  Unsaved changes
                </motion.span>
              )}
              {saveStatus === 'saved' && (
                <motion.span 
                  initial={{ opacity: 0, scale: 0.8 }} animate={{ opacity: 1, scale: 1 }} exit={{ opacity: 0 }}
                  className="text-emerald-400 text-sm font-medium flex items-center gap-1"
                >
                  <CheckCircle2 size={16} /> Saved
                </motion.span>
              )}
            </AnimatePresence>

            <button 
              onClick={handleReset}
              className="px-4 py-2 text-sm text-slate-300 hover:text-white hover:bg-slate-800 rounded-lg transition-colors flex items-center gap-2"
            >
              <RefreshCw size={14} /> Reset
            </button>
            
            <button 
              onClick={handleSave}
              disabled={!isDirty || saveStatus === 'saving'}
              className="px-6 py-2 bg-cyan-600 hover:bg-cyan-500 disabled:opacity-50 disabled:cursor-not-allowed text-white font-medium rounded-lg transition-all flex items-center gap-2 shadow-[0_0_15px_rgba(8,145,178,0.2)]"
            >
              {saveStatus === 'saving' ? (
                <motion.div animate={{ rotate: 360 }} transition={{ repeat: Infinity, duration: 1, ease: 'linear' }} className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full" />
              ) : (
                <Save size={16} /> 
              )}
              Save Configuration
            </button>
          </div>
        </div>
      </div>

      <div className="max-w-6xl mx-auto px-8 py-10 flex flex-col md:flex-row gap-10">
        
        {/* Sidebar Navigation */}
        <div className="w-full md:w-64 flex-shrink-0">
          <nav className="space-y-1">
            {tabs.map(tab => (
              <button
                key={tab.id}
                onClick={() => setActiveTab(tab.id)}
                className={`w-full flex items-center justify-between p-3 rounded-xl transition-all ${
                  activeTab === tab.id 
                  ? 'bg-slate-800 border border-slate-700 text-white shadow-[0_0_15px_rgba(30,41,59,0.5)]' 
                  : 'text-slate-400 hover:bg-slate-900 hover:text-slate-200'
                }`}
              >
                <div className="flex items-center gap-3">
                  <span className={activeTab === tab.id ? 'text-cyan-400' : ''}>{tab.icon}</span>
                  <span className="font-medium text-sm">{tab.label}</span>
                </div>
                {activeTab === tab.id && <ChevronRight size={16} className="text-slate-500" />}
              </button>
            ))}
          </nav>
        </div>

        {/* Content Panel */}
        <div className="flex-1 min-w-0">
          <AnimatePresence mode="wait">
            {activeTab === 'general' && <motion.div key="gen" exit={{ opacity: 0 }}>{renderGeneral()}</motion.div>}
            {activeTab === 'security' && <motion.div key="sec" exit={{ opacity: 0 }}>{renderSecurity()}</motion.div>}
            {activeTab === 'simulation' && <motion.div key="sim" exit={{ opacity: 0 }}>{renderSimulation()}</motion.div>}
            {activeTab === 'alerts' && <motion.div key="ale" exit={{ opacity: 0 }}>{renderAlerts()}</motion.div>}
            {activeTab === 'integrations' && <motion.div key="int" exit={{ opacity: 0 }}>{renderIntegrations()}</motion.div>}
            {activeTab === 'account' && (
              <motion.div key="acc" exit={{ opacity: 0 }} initial={{ opacity: 0, y: 10 }} animate={{ opacity: 1, y: 0 }} className="space-y-6">
                <h3 className="text-xl font-bold text-white mb-6">Account Details</h3>
                <div className="p-6 bg-slate-900/50 border border-slate-800 rounded-xl text-slate-400">
                  <p>Basic account settings placeholder (Name, Email, Password).</p>
                </div>
              </motion.div>
            )}
          </AnimatePresence>
        </div>

      </div>
    </div>
  )
}
