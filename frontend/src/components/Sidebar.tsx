import { motion } from 'framer-motion'
import { useLocation, useNavigate } from 'react-router-dom'
import { useState, useEffect } from 'react'
import {
  Home,
  Search,
  AlertTriangle,
  Settings,
  LogOut,
  Menu,
  X,
} from 'lucide-react'

// ✨ Staggered float for individual icons
const getFloatDelay = (index: number) => index * 0.15

const Sidebar = () => {
  const navigate = useNavigate()
  const location = useLocation()
  const [hoveredItem, setHoveredItem] = useState<string | null>(null)
  const [isMobile, setIsMobile] = useState(window.innerWidth < 768)
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false)
  const [selectedItem, setSelectedItem] = useState<string>('dashboard')

  // Handle responsive behavior
  useEffect(() => {
    const handleResize = () => {
      setIsMobile(window.innerWidth < 768)
    }

    window.addEventListener('resize', handleResize)
    return () => window.removeEventListener('resize', handleResize)
  }, [])

  // Keyboard navigation
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.ctrlKey && e.shiftKey) {
        const menuIds = menuItems.map(item => item.id)
        const currentIndex = menuIds.indexOf(selectedItem)
        
        if (e.key === 'ArrowRight') {
          const nextIndex = (currentIndex + 1) % menuIds.length
          navigate(menuItems[nextIndex].path)
          setSelectedItem(menuIds[nextIndex])
        } else if (e.key === 'ArrowLeft') {
          const prevIndex = (currentIndex - 1 + menuIds.length) % menuIds.length
          navigate(menuItems[prevIndex].path)
          setSelectedItem(menuIds[prevIndex])
        }
      }
    }

    window.addEventListener('keydown', handleKeyDown)
    return () => window.removeEventListener('keydown', handleKeyDown)
  }, [selectedItem, navigate])


  const menuItems = [
    { icon: Home, label: 'Dashboard', path: '/', id: 'dashboard', color: 'from-blue-400 to-blue-600', description: 'Main dashboard view' },
    {
      icon: Search,
      label: 'Session Explorer',
      path: '/sessions',
      id: 'sessions',
      color: 'from-cyan-400 to-cyan-600',
      description: 'Browse sessions'
    },
    {
      icon: AlertTriangle,
      label: 'Alerts',
      path: '/alerts',
      id: 'alerts',
      color: 'from-orange-400 to-orange-600',
      description: 'View alerts'
    },
    { 
      icon: Settings, 
      label: 'Settings', 
      path: '/settings', 
      id: 'settings',
      color: 'from-purple-400 to-purple-600',
      description: 'Customize settings'
    },
  ]

  const isActive = (path: string) => location.pathname === path

  const containerVariants = {
    hidden: { opacity: 0, y: -20 },
    visible: {
      opacity: 1,
      y: 0,
      transition: {
        staggerChildren: 0.08,
        delayChildren: 0.2,
      },
    },
  }

  const itemVariants = {
    hidden: { opacity: 0, scale: 0.5 },
    visible: { 
      opacity: 1, 
      scale: 1,
      transition: {
        type: 'spring',
        stiffness: 400,
        damping: 10,
      }
    },
  }

  return (
    <>
      {/* Desktop Floating Dock - UPPER MIDDLE - TRUE FLOATING LAYER */}
      {!isMobile && (
        <motion.div
          data-floating-nav
          className="fixed left-1/2 top-20 lg:top-24 transform -translate-x-1/2 w-full max-w-6xl px-4"
          style={{ zIndex: 9999 }}
          initial={{ opacity: 0, y: -80 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.6, ease: 'easeOut' }}
        >
          {/* Floating Icon Dock - FLOATS ABOVE EVERYTHING */}
          <motion.nav
            className="flex items-center justify-center gap-4 px-8 py-5 rounded-2xl shadow-2xl mx-auto w-fit backdrop-blur-xl"
            style={{
              background: 'linear-gradient(135deg, rgba(15, 23, 42, 0.9), rgba(30, 41, 59, 0.85))',
              border: '2px solid rgba(148, 163, 184, 0.4)',
              boxShadow: '0 30px 100px rgba(0, 0, 0, 0.7), inset 0 1px 3px rgba(255, 255, 255, 0.2), 0 0 40px rgba(59, 130, 246, 0.2)',
            }}
            animate={{
              y: [0, -8, 0],
            }}
            transition={{
              duration: 3,
              repeat: Infinity,
              repeatType: 'loop' as const,
              ease: 'easeInOut',
            }}
            variants={containerVariants}
            initial="hidden"
          >
            {menuItems.map((item, idx) => {
              const Icon = item.icon
              const active = isActive(item.path)

              return (
                <motion.div
                  key={item.id}
                  className="relative flex items-center justify-center group"
                  variants={itemVariants}
                  onMouseEnter={() => setHoveredItem(item.id)}
                  onMouseLeave={() => setHoveredItem(null)}
                  animate={{
                    y: [0, -12 + (getFloatDelay(idx) * 20), 0],
                  }}
                  transition={{
                    duration: 4,
                    repeat: Infinity,
                    repeatType: 'loop' as const,
                    ease: 'easeInOut',
                    delay: getFloatDelay(idx),
                  }}
                >
                  {/* Shiny Glow Background - ENHANCED */}
                  <motion.div
                    className={`absolute inset-0 rounded-full bg-gradient-to-br ${item.color} opacity-0 blur-xl`}
                    animate={{
                      opacity: hoveredItem === item.id || active ? 0.7 : 0,
                      scale: hoveredItem === item.id || active ? 1.5 : 1,
                    }}
                    transition={{ duration: 0.3 }}
                  />

                  {/* Shine Effect - ENHANCED */}
                  {(hoveredItem === item.id || active) && (
                    <motion.div
                      className={`absolute inset-0 rounded-full bg-gradient-to-r ${item.color} opacity-50`}
                      animate={{
                        backgroundPosition: ['0% 0%', '100% 100%'],
                      }}
                      transition={{
                        duration: 3,
                        repeat: Infinity,
                        repeatType: 'reverse',
                      }}
                      style={{
                        backgroundSize: '200% 200%',
                      }}
                    />
                  )}

                  {/* Button - FLOATING ORB STYLE */}
                  <motion.button
                    onClick={() => {
                      navigate(item.path)
                      setSelectedItem(item.id)
                    }}
                    className="relative w-16 h-16 lg:w-18 lg:h-18 rounded-full flex items-center justify-center transition-all duration-300 z-10"
                    style={{
                      background: active 
                        ? `linear-gradient(135deg, rgb(59, 130, 246), rgb(37, 99, 235))`
                        : 'rgba(51, 65, 85, 0.8)',
                      color: active ? '#fff' : '#cbd5e1',
                      boxShadow: active 
                        ? '0 12px 48px rgba(59, 130, 246, 0.6), inset 0 2px 4px rgba(255, 255, 255, 0.4)'
                        : '0 8px 24px rgba(0, 0, 0, 0.5), inset 0 1px 2px rgba(255, 255, 255, 0.2)',
                    }}
                    animate={{
                      scale: hoveredItem === item.id || active ? 1.3 : 1,
                      boxShadow: hoveredItem === item.id || active
                        ? `0 20px 60px ${active ? 'rgba(59, 130, 246, 0.8)' : 'rgba(148, 163, 184, 0.6)'}, inset 0 2px 4px rgba(255, 255, 255, 0.4)`
                        : '0 8px 24px rgba(0, 0, 0, 0.5), inset 0 1px 2px rgba(255, 255, 255, 0.2)',
                    }}
                    transition={{
                      type: 'spring',
                      stiffness: 400,
                      damping: 10,
                    }}
                    whileHover={{
                      y: -8,
                      rotate: 8,
                    }}
                    aria-label={item.label}
                    aria-current={active ? 'page' : undefined}
                  >
                    <motion.div
                      animate={{
                        rotate: hoveredItem === item.id ? [0, 15, -15, 0] : 0,
                      }}
                      transition={{ duration: 0.6 }}
                    >
                      <Icon 
                        size={32} 
                        className={active ? 'text-white drop-shadow-lg' : 'drop-shadow-md'}
                      />
                    </motion.div>
                  </motion.button>

                  {/* Enhanced Tooltip Label */}
                  <motion.div
                    className="absolute top-full mt-6 whitespace-nowrap pointer-events-none"
                    animate={{
                      opacity: hoveredItem === item.id ? 1 : 0,
                      y: hoveredItem === item.id ? 0 : -10,
                    }}
                    transition={{ duration: 0.25 }}
                  >
                    <div className="px-5 py-3 rounded-xl text-sm font-semibold text-white"
                      style={{
                        background: 'linear-gradient(135deg, rgba(59, 130, 246, 0.98), rgba(37, 99, 235, 0.98))',
                        boxShadow: '0 20px 50px rgba(59, 130, 246, 0.6), 0 0 3px rgba(255, 255, 255, 0.5)',
                        backdropFilter: 'blur(20px)',
                      }}
                    >
                      <div>{item.label}</div>
                      <div className="text-xs text-blue-100 font-normal mt-1 opacity-90">{item.description}</div>
                      {/* Arrow */}
                      <motion.div 
                        className="absolute bottom-full left-1/2 transform -translate-x-1/2 w-3 h-3 bg-blue-600 rotate-45 mb-0.5"
                        animate={{ y: [0, -5, 0] }}
                        transition={{ duration: 2, repeat: Infinity }}
                      />
                    </div>
                  </motion.div>
                </motion.div>
              )
            })}

            {/* Divider - ENHANCED */}
            <motion.div
              className="w-px h-8 lg:h-10 bg-gradient-to-b from-transparent via-slate-400 to-transparent opacity-50 mx-3"
              initial={{ opacity: 0 }}
              animate={{ opacity: 0.5 }}
              transition={{ delay: 0.5 }}
            />

            {/* Logout Button - FLOATING ORB */}
            <motion.button
              variants={itemVariants}
              onClick={() => {
                console.log('Logout')
              }}
              className="w-16 h-16 lg:w-18 lg:h-18 rounded-full flex items-center justify-center transition-all duration-300 z-10 relative"
              style={{
                background: 'rgba(51, 65, 85, 0.8)',
                color: '#cbd5e1',
                boxShadow: '0 8px 24px rgba(0, 0, 0, 0.5), inset 0 1px 2px rgba(255, 255, 255, 0.2)',
              }}
              animate={{
                scale: hoveredItem === 'logout' ? 1.3 : 1,
                boxShadow: hoveredItem === 'logout'
                  ? '0 20px 60px rgba(239, 68, 68, 0.7), inset 0 2px 4px rgba(255, 255, 255, 0.4)'
                  : '0 8px 24px rgba(0, 0, 0, 0.5), inset 0 1px 2px rgba(255, 255, 255, 0.2)',
                y: [0, -12, 0],
              }}
              transition={{
                scale: {
                  type: 'spring',
                  stiffness: 400,
                  damping: 10,
                },
                boxShadow: {
                  type: 'spring',
                  stiffness: 400,
                  damping: 10,
                },
                y: {
                  duration: 4.3,
                  repeat: Infinity,
                  repeatType: 'loop' as const,
                  ease: 'easeInOut',
                  delay: getFloatDelay(menuItems.length),
                }
              }}
              whileHover={{ y: -8, rotate: 8 }}
              onMouseEnter={() => setHoveredItem('logout')}
              onMouseLeave={() => setHoveredItem(null)}
              aria-label="Logout"
            >
              <motion.div
                animate={{
                  rotate: hoveredItem === 'logout' ? [0, 15, -15, 0] : 0,
                }}
                transition={{ duration: 0.6 }}
              >
                <LogOut size={32} className="text-red-400 drop-shadow-md" />
              </motion.div>

              {/* Logout Tooltip */}
              <motion.div
                className="absolute top-full mt-6 whitespace-nowrap pointer-events-none"
                animate={{
                  opacity: hoveredItem === 'logout' ? 1 : 0,
                  y: hoveredItem === 'logout' ? 0 : -10,
                }}
                transition={{ duration: 0.25 }}
              >
                <div className="px-5 py-3 rounded-xl text-sm font-semibold text-white"
                  style={{
                    background: 'linear-gradient(135deg, rgba(239, 68, 68, 0.98), rgba(220, 38, 38, 0.98))',
                    boxShadow: '0 20px 50px rgba(239, 68, 68, 0.6), 0 0 3px rgba(255, 255, 255, 0.5)',
                    backdropFilter: 'blur(20px)',
                  }}
                >
                  <div>Logout</div>
                  <div className="text-xs text-red-100 font-normal mt-1 opacity-90">Sign out</div>
                  <motion.div 
                    className="absolute bottom-full left-1/2 transform -translate-x-1/2 w-3 h-3 bg-red-600 rotate-45 mb-0.5"
                    animate={{ y: [0, -5, 0] }}
                    transition={{ duration: 2, repeat: Infinity }}
                  />
                </div>
              </motion.div>
            </motion.button>
          </motion.nav>
        </motion.div>
      )}

      {/* Mobile Menu - Toggle Button */}
      {isMobile && (
        <motion.button
          className="fixed bottom-6 right-6 w-14 h-14 rounded-full flex items-center justify-center"
          style={{
            background: 'linear-gradient(135deg, rgba(59, 130, 246, 0.9), rgba(37, 99, 235, 0.9))',
            boxShadow: '0 12px 32px rgba(59, 130, 246, 0.4)',
            zIndex: 9999,
          }}
          whileTap={{ scale: 0.95 }}
          whileHover={{ scale: 1.1 }}
          onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
          aria-label="Toggle navigation menu"
        >
          <motion.div
            animate={{ rotate: mobileMenuOpen ? 90 : 0 }}
            transition={{ duration: 0.3 }}
          >
            {mobileMenuOpen ? <X size={24} className="text-white" /> : <Menu size={24} className="text-white" />}
          </motion.div>
        </motion.button>
      )}

      {/* Mobile Sidebar Menu */}
      {isMobile && (
        <>
          {/* Backdrop */}
          <motion.div
            className="fixed inset-0"
            style={{ 
              background: 'rgba(0, 0, 0, 0)',
              zIndex: 9998,
            }}
            animate={{ 
              backgroundColor: mobileMenuOpen ? 'rgba(0, 0, 0, 0.5)' : 'rgba(0, 0, 0, 0)',
              pointerEvents: mobileMenuOpen ? 'auto' : 'none',
            }}
            transition={{ duration: 0.3 }}
            onClick={() => setMobileMenuOpen(false)}
          />

          {/* Menu Slide Panel */}
          <motion.nav
            className="fixed bottom-0 right-0 left-0 rounded-t-3xl max-h-[80vh] overflow-y-auto"
            style={{
              background: 'linear-gradient(135deg, rgba(15, 23, 42, 0.95), rgba(30, 41, 59, 0.9))',
              backdropFilter: 'blur(15px)',
              border: '1.5px solid rgba(148, 163, 184, 0.25)',
              boxShadow: '0 -20px 60px rgba(0, 0, 0, 0.5)',
              zIndex: 9998,
            }}
            animate={{ 
              y: mobileMenuOpen ? 0 : '100%',
            }}
            transition={{ duration: 0.4, ease: 'easeOut' }}
          >
            <div className="p-6 space-y-3">
              {/* Header */}
              <div className="flex items-center justify-between mb-6 pb-4 border-b border-slate-700/30">
                <h2 className="text-xl font-bold text-white">Navigation</h2>
                <motion.button
                  onClick={() => setMobileMenuOpen(false)}
                  className="p-2 hover:bg-slate-800/50 rounded-lg transition-colors"
                >
                  <X size={24} className="text-slate-400" />
                </motion.button>
              </div>

              {/* Menu Items */}
              <motion.div
                className="space-y-2"
                variants={containerVariants}
                initial="hidden"
                animate={mobileMenuOpen ? "visible" : "hidden"}
              >
                {menuItems.map((item) => {
                  const Icon = item.icon
                  const active = isActive(item.path)

                  return (
                    <motion.button
                      key={item.id}
                      variants={itemVariants}
                      onClick={() => {
                        navigate(item.path)
                        setSelectedItem(item.id)
                        setMobileMenuOpen(false)
                      }}
                      className="w-full flex items-center gap-4 px-4 py-4 rounded-xl transition-all duration-300 group"
                      style={{
                        background: active
                          ? 'linear-gradient(135deg, rgba(59, 130, 246, 0.3), rgba(37, 99, 235, 0.2))'
                          : 'rgba(51, 65, 85, 0.4)',
                        border: active ? '1.5px solid rgba(59, 130, 246, 0.5)' : '1.5px solid rgba(148, 163, 184, 0.15)',
                      }}
                      whileHover={{
                        backgroundColor: active ? undefined : 'rgba(51, 65, 85, 0.6)',
                        borderColor: 'rgba(59, 130, 246, 0.4)',
                        x: 4,
                      }}
                    >
                      <motion.div
                        className={`p-3 rounded-lg ${active ? 'bg-gradient-to-br ' + item.color : 'bg-slate-700/50'}`}
                        whileHover={{ scale: 1.1 }}
                      >
                        <Icon size={22} className={active ? 'text-white' : 'text-slate-300'} />
                      </motion.div>
                      <div className="flex-1 text-left">
                        <p className={`font-semibold ${active ? 'text-white' : 'text-slate-300'}`}>{item.label}</p>
                        <p className="text-xs text-slate-500">{item.description}</p>
                      </div>
                      {active && (
                        <motion.div
                          className="w-2 h-2 rounded-full bg-blue-400"
                          animate={{ scale: [1, 1.2, 1] }}
                          transition={{ duration: 2, repeat: Infinity }}
                        />
                      )}
                    </motion.button>
                  )
                })}

                {/* Logout Item */}
                <motion.button
                  variants={itemVariants}
                  onClick={() => {
                    console.log('Logout')
                    setMobileMenuOpen(false)
                  }}
                  className="w-full flex items-center gap-4 px-4 py-4 rounded-xl transition-all duration-300"
                  style={{
                    background: 'rgba(51, 65, 85, 0.4)',
                    border: '1.5px solid rgba(148, 163, 184, 0.15)',
                  }}
                  whileHover={{
                    backgroundColor: 'rgba(239, 68, 68, 0.2)',
                    borderColor: 'rgba(239, 68, 68, 0.4)',
                    x: 4,
                  }}
                >
                  <motion.div
                    className="p-3 rounded-lg bg-slate-700/50"
                    whileHover={{ scale: 1.1 }}
                  >
                    <LogOut size={22} className="text-red-400" />
                  </motion.div>
                  <div className="flex-1 text-left">
                    <p className="font-semibold text-slate-300">Logout</p>
                    <p className="text-xs text-slate-500">Sign out</p>
                  </div>
                </motion.button>
              </motion.div>
            </div>
          </motion.nav>
        </>
      )}

      {/* Role Badge - Bottom Left Corner */}
      <motion.div
        className="fixed bottom-6 left-6 px-5 py-4 rounded-xl text-xs"
        style={{
          background: 'linear-gradient(135deg, rgba(15, 23, 42, 0.85), rgba(30, 41, 59, 0.75))',
          border: '2px solid rgba(59, 130, 246, 0.3)',
          color: '#cbd5e1',
          backdropFilter: 'blur(15px)',
          boxShadow: '0 12px 40px rgba(0, 0, 0, 0.4), inset 0 1px 2px rgba(255, 255, 255, 0.1)',
          zIndex: 50,
        }}
        initial={{ opacity: 0, x: -30 }}
        animate={{ opacity: 1, x: 0 }}
        transition={{ delay: 0.4, duration: 0.6 }}
      >
        <p className="text-slate-400 font-medium">Role</p>
        <p className="text-blue-400 font-bold mt-2 text-sm">FORENSICS_ADMIN</p>
      </motion.div>
    </>
  )
}

export default Sidebar
