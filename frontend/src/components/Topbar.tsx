import { motion } from 'framer-motion'
import { Search, Bell, Settings, User } from 'lucide-react'

interface TopbarProps {
  title?: string
}

const Topbar = ({ title = 'Dashboard' }: TopbarProps) => {
  return (
    <motion.div
      className="fixed top-0 left-0 right-0 h-16 bg-slate-800/50 border-b border-slate-700 flex items-center justify-between px-8 shadow-md backdrop-blur-sm"
      initial={{ y: -64 }}
      animate={{ y: 0 }}
      transition={{ duration: 0.3 }}
    >
      {/* Left: Title */}
      <motion.h1
        className="text-2xl font-bold text-white"
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        transition={{ delay: 0.2 }}
      >
        {title}
      </motion.h1>

      {/* Right: Actions */}
      <div className="flex items-center gap-4">
        {/* Search Bar */}
        <motion.div
          className="flex items-center gap-2 bg-slate-700 px-4 py-2 rounded-lg border border-slate-600 hover:border-slate-500 transition-colors"
          whileHover={{ scale: 1.02 }}
        >
          <Search size={18} className="text-slate-400" />
          <input
            type="text"
            placeholder="Search sessions..."
            className="bg-transparent text-sm text-white placeholder-slate-400 outline-none w-48"
          />
        </motion.div>

        {/* Icons */}
        <motion.button
          whileHover={{ scale: 1.1, rotate: [0, 5, -5, 0] }}
          whileTap={{ scale: 0.95 }}
          className="p-2 rounded-lg hover:bg-slate-700 transition-colors relative"
        >
          <Bell size={20} className="text-slate-300" />
          <motion.span
            className="absolute top-0 right-0 w-2 h-2 bg-red-500 rounded-full"
            animate={{ scale: [1, 1.2, 1] }}
            transition={{ repeat: Infinity, duration: 2 }}
          />
        </motion.button>

        <motion.button
          whileHover={{ scale: 1.1 }}
          whileTap={{ scale: 0.95 }}
          className="p-2 rounded-lg hover:bg-slate-700 transition-colors"
        >
          <Settings size={20} className="text-slate-300" />
        </motion.button>

        {/* User Profile */}
        <motion.div
          whileHover={{ scale: 1.05 }}
          className="flex items-center gap-2 pl-4 border-l border-slate-700"
        >
          <div className="w-8 h-8 rounded-full bg-blue-500 flex items-center justify-center">
            <User size={16} className="text-white" />
          </div>
          <span className="text-sm text-slate-300">Admin</span>
        </motion.div>
      </div>
    </motion.div>
  )
}

export default Topbar
