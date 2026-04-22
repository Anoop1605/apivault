import React from 'react'

interface BorderGlowProps {
  glowColor: string // Format: "r g b" (0-255)
  children: React.ReactNode
  intensity?: number // 0-100
  className?: string
}

export const BorderGlow: React.FC<BorderGlowProps> = ({
  glowColor,
  children,
  intensity = 50,
  className = ''
}) => {
  const opacity = intensity / 100
  const [r, g, b] = glowColor.split(' ').map(Number)

  return (
    <div
      className={`relative overflow-hidden ${className}`}
      style={{
        boxShadow: `inset 0 0 12px rgba(${r}, ${g}, ${b}, ${opacity * 0.2})`,
      }}
    >
      <div className="absolute inset-0 rounded-lg pointer-events-none"
        style={{
          background: `radial-gradient(circle at top-left, rgba(${r}, ${g}, ${b}, ${opacity * 0.08}), transparent)`,
          borderRadius: 'inherit',
        }}
      />
      <div 
        className="relative"
        style={{
          boxShadow: `inset 0 0 10px rgba(${r}, ${g}, ${b}, ${opacity * 0.15})`
        }}
      >
        {children}
      </div>
    </div>
  )
}
