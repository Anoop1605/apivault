import React, { useEffect, useRef } from 'react'
import * as echarts from 'echarts'
import { ReplayStep } from '../../types/api.types'

interface RiskChartProps {
  steps: ReplayStep[]
  currentStep: number
}

export const RiskChart: React.FC<RiskChartProps> = ({ steps, currentStep }) => {
  const chartRef = useRef<HTMLDivElement>(null)
  const chartInstance = useRef<echarts.ECharts | null>(null)

  useEffect(() => {
    if (!chartRef.current) return

    // Initialize chart
    if (!chartInstance.current) {
      chartInstance.current = echarts.init(chartRef.current, 'dark')
    }

    const chart = chartInstance.current

    // Prepare data
    const categories = steps.map((s) => `Step ${s.step}`)
    const riskScores = steps.map((s) => (s.risk_score * 100).toFixed(1))
    const currentStepCoord = [
      categories[currentStep],
      steps[currentStep].risk_score * 100,
    ]

    // Chart options
    const option: echarts.EChartsOption = {
      backgroundColor: 'transparent',
      tooltip: {
        trigger: 'axis',
        backgroundColor: 'rgba(15, 23, 42, 0.9)',
        borderColor: 'rgba(51, 65, 85, 0.5)',
        textStyle: {
          color: '#e2e8f0',
        },
        formatter: (params: any) => {
          if (Array.isArray(params) && params.length > 0) {
            const data = params[0]
            return `<div>
              <div style="font-weight: bold; margin-bottom: 4px;">${data.name}</div>
              <div>Risk Score: <span style="color: #fb923c;">${data.value}%</span></div>
            </div>`
          }
          return ''
        },
      },
      xAxis: {
        type: 'category',
        data: categories,
        boundaryGap: false,
        axisLine: {
          lineStyle: {
            color: 'rgba(148, 163, 184, 0.2)',
          },
        },
        axisLabel: {
          color: 'rgba(148, 163, 184, 0.7)',
          fontSize: 12,
        },
        splitLine: {
          show: false,
        },
      },
      yAxis: {
        type: 'value',
        min: 0,
        max: 100,
        name: 'Risk %',
        nameTextStyle: {
          color: 'rgba(148, 163, 184, 0.7)',
        },
        axisLine: {
          lineStyle: {
            color: 'rgba(148, 163, 184, 0.2)',
          },
        },
        axisLabel: {
          color: 'rgba(148, 163, 184, 0.7)',
          fontSize: 12,
        },
        splitLine: {
          lineStyle: {
            color: 'rgba(148, 163, 184, 0.1)',
          },
        },
      },
      series: [
        {
          name: 'Risk Score',
          type: 'line',
          data: riskScores,
          smooth: true,
          lineStyle: {
            color: 'rgba(59, 130, 246, 1)',
            width: 3,
          },
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(59, 130, 246, 0.3)' },
              { offset: 1, color: 'rgba(59, 130, 246, 0.05)' },
            ]),
          },
          itemStyle: {
            color: 'rgb(59, 130, 246)',
            borderColor: 'rgb(255, 255, 255)',
            borderWidth: 2,
          },
          markPoint: {
            data: [
              {
                coord: currentStepCoord,
                name: 'Current Step',
                itemStyle: {
                  color: 'rgb(34, 197, 94)',
                },
                symbolSize: 10,
              },
            ],
            symbolSize: [40, 40],
            label: {
              color: '#fff',
              fontSize: 11,
              offset: [0, -15],
            },
          },
          markLine: {
            silent: false,
            data: [
              {
                yAxis: 50,
                name: 'Critical',
                lineStyle: {
                  color: 'rgba(239, 68, 68, 0.4)',
                  type: 'dashed',
                },
                label: {
                  position: 'end',
                  color: 'rgba(239, 68, 68, 0.8)',
                  fontSize: 11,
                },
              },
              {
                yAxis: 30,
                name: 'Warning',
                lineStyle: {
                  color: 'rgba(251, 146, 60, 0.4)',
                  type: 'dashed',
                },
                label: {
                  position: 'end',
                  color: 'rgba(251, 146, 60, 0.8)',
                  fontSize: 11,
                },
              },
            ],
          },
          tooltip: {
            backgroundColor: 'rgba(15, 23, 42, 0.95)',
            borderColor: 'rgba(59, 130, 246, 0.5)',
          },
        },
      ],
      grid: {
        left: '5%',
        right: '5%',
        top: '10%',
        bottom: '10%',
        containLabel: true,
      },
    }

    chart.setOption(option)

    // Resize handler
    const handleResize = () => {
      chart.resize()
    }
    window.addEventListener('resize', handleResize)

    return () => {
      window.removeEventListener('resize', handleResize)
    }
  }, [steps, currentStep])

  return (
    <div className="space-y-4">
      <h3 className="text-lg font-bold text-white flex items-center gap-2">
        <div className="w-3 h-3 rounded-full bg-blue-400 animate-pulse" />
        Risk Progression
      </h3>
      <div
        ref={chartRef}
        className="w-full h-96 bg-slate-800/30 border border-slate-700/50 rounded-lg overflow-hidden"
      />
      <div className="grid grid-cols-3 gap-4 text-sm">
        <div className="bg-slate-800/30 border border-slate-700/50 rounded-lg p-3">
          <div className="text-slate-400">Min Risk</div>
          <div className="text-lg font-bold text-green-400">
            {Math.min(...steps.map((s) => s.risk_score * 100)).toFixed(1)}%
          </div>
        </div>
        <div className="bg-slate-800/30 border border-slate-700/50 rounded-lg p-3">
          <div className="text-slate-400">Avg Risk</div>
          <div className="text-lg font-bold text-orange-400">
            {(
              (steps.reduce((sum, s) => sum + s.risk_score, 0) / steps.length) * 100
            ).toFixed(1)}
            %
          </div>
        </div>
        <div className="bg-slate-800/30 border border-slate-700/50 rounded-lg p-3">
          <div className="text-slate-400">Max Risk</div>
          <div className="text-lg font-bold text-red-400">
            {Math.max(...steps.map((s) => s.risk_score * 100)).toFixed(1)}%
          </div>
        </div>
      </div>
    </div>
  )
}
