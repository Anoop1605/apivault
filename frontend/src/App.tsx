import { BrowserRouter as Router, Routes, Route } from 'react-router-dom'
import { useEffect } from 'react'
import Lenis from 'lenis'
import Aurora from './components/Aurora'
import GooeyNav from './components/GooeyNav'
import Dashboard from './pages/Dashboard'
import Sessions from './pages/Sessions'
import { SessionDetail } from './pages/SessionDetail'
import { ReplayPage } from './pages/ReplayPage'
import { SimulationPage } from './pages/SimulationPage'
import { Alerts } from './pages/Alerts'
import { Settings } from './pages/Settings'
import Session from './pages/Session';
import './App.css'

// Main App Component with Routing
// Navigation items for GooeyNav
const navItems = [
  { label: 'Dashboard', href: '/' },
  { label: 'Sessions', href: '/sessions' },
  { label: 'Alerts', href: '/alerts' },
  { label: 'Settings', href: '/settings' },
]

function AppContent() {
  useEffect(() => {
    // Initialize Lenis smooth scrolling
    const lenis = new Lenis({
      duration: 1.2,
      smooth: true,
      direction: 'vertical',
      gestureDirection: 'vertical',
      smoothWheel: true,
      smoothTouch: false,
    })

    // RAF loop for smooth scrolling
    function raf(time: number) {
      lenis.raf(time)
      requestAnimationFrame(raf)
    }

    const rafId = requestAnimationFrame(raf)

    // Cleanup
    return () => {
      cancelAnimationFrame(rafId)
      lenis.destroy()
    }
  }, [])

  return (
    <>
      {/* Aurora background - behind all content */}
      <Aurora
        colorStops={['#3A29FF', '#FF94B4', '#FF3232']}
        blend={0.5}
        amplitude={1.0}
        speed={0.5}
      />

      {/* GooeyNav - Always visible at bottom */}
      <GooeyNav
        items={navItems}
        particleCount={15}
        particleDistances={[90, 10]}
        particleR={100}
        initialActiveIndex={0}
        animationTime={600}
        timeVariance={300}
        colors={[1, 2, 3, 1, 2, 3, 1, 4]}
      />

      <Routes>
        <Route path="/" element={<Dashboard />} />
        <Route path="/sessions" element={<Sessions />} />
        <Route path="/sessions/:sessionId" element={<SessionDetail />} />
        <Route path="/replay/:sessionId" element={<ReplayPage />} />
        <Route path="/simulation/:sessionId" element={<SimulationPage />} />
        <Route path="/alerts" element={<Alerts />} />
        <Route path="/settings" element={<Settings />} />
        <Route path="/sessions/:sessionId" element={<Session />} />
        {/* Additional routes will be added here */}
      </Routes>
    </>
  )
}

import { QueryClient, QueryClientProvider } from '@tanstack/react-query'

const queryClient = new QueryClient()

function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <Router>
        <AppContent />
      </Router>
    </QueryClientProvider>
  )
}

export default App
