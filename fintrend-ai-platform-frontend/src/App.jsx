import { useEffect, useState, useRef } from 'react'
import SockJS from 'sockjs-client'
import { Client } from '@stomp/stompjs'
import { createChart, CrosshairMode } from 'lightweight-charts'
import './App.css'

function App() {
  const chartContainerRef = useRef(null)
  const chartRef = useRef(null)
  const seriesRef = useRef(null)
  const [connected, setConnected] = useState(false)

  useEffect(() => {
    if (!chartContainerRef.current) return

    // Create chart only once
    if (!chartRef.current) {
      chartRef.current = createChart(chartContainerRef.current, {
        width: 1000,
        height: 500,
        layout: {
          background: { color: '#000000' },
          textColor: 'rgba(255, 255, 255, 0.9)',
        },
        grid: {
          vertLines: { color: 'rgba(197, 203, 206, 0.5)' },
          horzLines: { color: 'rgba(197, 203, 206, 0.5)' },
        },
        crosshair: { mode: CrosshairMode.Normal },
        priceScale: { borderColor: 'rgba(197, 203, 206, 0.8)' },
        timeScale: {
          borderColor: 'rgba(197, 203, 206, 0.8)',
          timeVisible: true,
          secondsVisible: false,
        },
      })
      seriesRef.current = chartRef.current.addCandlestickSeries({
        upColor: '#00ff00',
        downColor: '#ff0000',
        borderDownColor: 'rgba(255, 144, 0, 1)',
        borderUpColor: 'rgba(255, 144, 0, 1)',
        wickDownColor: 'rgba(255, 144, 0, 1)',
        wickUpColor: 'rgba(255, 144, 0, 1)',
      })
    }

    // WebSocket connection to backend
    const socket = new SockJS('http://localhost:8080/ws')
    const stompClient = new Client({
      webSocketFactory: () => socket,
      onConnect: () => {
        setConnected(true)
        stompClient.subscribe('/topic/kline', (msg) => {
          const raw = JSON.parse(msg.body)
          // Only add the candle when it is closed (x === true)
          seriesRef.current.update({
              time: Math.floor(raw.t / 1000),
              open: parseFloat(raw.o),
              high: parseFloat(raw.h),
              low: parseFloat(raw.l),
              close: parseFloat(raw.c),
          })
        })
      },
      onDisconnect: () => setConnected(false),
      debug: () => {},
      reconnectDelay: 5000,
    })
    stompClient.activate()

    // Resize chart on window resize
    const handleResize = () => {
      if (chartRef.current && chartContainerRef.current) {
        chartRef.current.applyOptions({ width: chartContainerRef.current.clientWidth })
      }
    }
    window.addEventListener('resize', handleResize)

    return () => {
      stompClient.deactivate()
      window.removeEventListener('resize', handleResize)
      if (chartRef.current) {
        chartRef.current.remove()
        chartRef.current = null
        seriesRef.current = null
      }
    }
  }, [])

  return (
    <div className="kline-container">
      <h1>Live Kline Candlestick Chart</h1>
      <div className="status">
        Status: <span className={connected ? 'online' : 'offline'}>
          {connected ? 'Connected' : 'Disconnected'}
        </span>
      </div>
      <div ref={chartContainerRef} style={{ width: '100%', minHeight: 500, marginBottom: 24 }} />
    </div>
  )
}

export default App

