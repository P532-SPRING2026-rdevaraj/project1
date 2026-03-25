import { useState, useEffect, useRef, useCallback } from 'react'
import Header from './components/Header'
import Portfolio from './components/Portfolio'
import MarketPrices from './components/MarketPrices'
import PlaceOrder from './components/PlaceOrder'
import PendingOrders from './components/PendingOrders'
import TradeHistory from './components/TradeHistory'
import Notifications from './components/Notifications'
import SellModal from './components/SellModal'
import StrategySelector from './components/StrategySelector'

// In production the env var points to the Render backend URL.
// In development it is empty and Vite's proxy forwards /api to localhost:8080.
const API_BASE = import.meta.env.VITE_API_BASE_URL || ''

export function apiUrl(path) {
  return `${API_BASE}${path}`
}

export default function App() {
  const [prices, setPrices]               = useState({})
  const [prevPrices, setPrevPrices]       = useState({})
  const prevPricesRef                     = useRef({})
  const [portfolio, setPortfolio]         = useState({ cash: 0, holdings: {}, totalValue: 0 })
  const [pendingOrders, setPendingOrders] = useState([])
  const [trades, setTrades]               = useState([])
  const [notifications, setNotifications] = useState([])
  const [sellTarget, setSellTarget]       = useState(null)
  const [lastRefresh, setLastRefresh]     = useState(null)

  // Week 2 state
  const [users, setUsers]         = useState([])
  const [userId, setUserId]       = useState('alice')
  const [strategy, setStrategy]   = useState('random-walk')
  const [channels, setChannels]   = useState(new Set(['console', 'dashboard']))

  // Fetch users list once on mount
  useEffect(() => {
    fetch(apiUrl('/api/users'))
      .then(r => r.json())
      .then(setUsers)
      .catch(console.error)
  }, [])

  // Fetch current strategy once on mount
  useEffect(() => {
    fetch(apiUrl('/api/strategy'))
      .then(r => r.json())
      .then(d => setStrategy(d.strategy))
      .catch(console.error)
  }, [])

  // Fetch notification channels whenever userId changes
  useEffect(() => {
    fetch(apiUrl(`/api/notifications/channels?userId=${userId}`))
      .then(r => r.json())
      .then(d => setChannels(new Set(d.channels)))
      .catch(console.error)
  }, [userId])

  const fetchAll = useCallback(async () => {
    try {
      const [pricesRes, portRes, ordersRes, tradesRes, notifsRes] = await Promise.all([
        fetch(apiUrl('/api/prices')),
        fetch(apiUrl(`/api/portfolio?userId=${userId}`)),
        fetch(apiUrl(`/api/orders/pending?userId=${userId}`)),
        fetch(apiUrl(`/api/trades?userId=${userId}`)),
        fetch(apiUrl(`/api/notifications?userId=${userId}`)),
      ])
      const [newPrices, newPort, newOrders, newTrades, newNotifs] = await Promise.all([
        pricesRes.json(),
        portRes.json(),
        ordersRes.json(),
        tradesRes.json(),
        notifsRes.json(),
      ])
      const hasChanged = Object.keys(newPrices).some(k => newPrices[k] !== prevPricesRef.current[k])
      if (hasChanged) setPrevPrices({ ...prevPricesRef.current })
      prevPricesRef.current = newPrices
      setPrices(newPrices)
      setPortfolio(newPort)
      setPendingOrders(newOrders)
      setTrades(newTrades)
      setNotifications(newNotifs)
      setLastRefresh(new Date())
    } catch (e) {
      console.error('Fetch error:', e)
    }
  }, [userId])

  useEffect(() => {
    fetchAll()
    const id = setInterval(fetchAll, 5000)
    return () => clearInterval(id)
  }, [fetchAll])

  const placeOrder = async (body) => {
    const res = await fetch(apiUrl(`/api/orders?userId=${userId}`), {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body),
    })
    const data = await res.json()
    if (res.ok) fetchAll()
    return { ok: res.ok, data }
  }

  const switchStrategy = async (name) => {
    const res = await fetch(apiUrl(`/api/strategy?name=${name}`), { method: 'POST' })
    if (res.ok) {
      const d = await res.json()
      setStrategy(d.strategy)
    }
  }

  const updateChannels = async (newChannels) => {
    const res = await fetch(apiUrl(`/api/notifications/channels?userId=${userId}`), {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify([...newChannels]),
    })
    if (res.ok) {
      const d = await res.json()
      setChannels(new Set(d.channels))
    }
  }

  return (
    <div className="app">
      <Header
        lastRefresh={lastRefresh}
        users={users}
        userId={userId}
        onUserChange={setUserId}
      />
      <div className="layout">

        <Portfolio
          portfolio={portfolio}
          prices={prices}
          onSell={(ticker, maxShares) => setSellTarget({ ticker, maxShares })}
        />
        <MarketPrices prices={prices} prevPrices={prevPrices} />

        <PlaceOrder tickers={Object.keys(prices)} onOrder={placeOrder} />

        <div className="card strategy-card">
          <StrategySelector strategy={strategy} onSwitch={switchStrategy} />
        </div>

        <Notifications
          notifications={notifications}
          channels={channels}
          onChannelChange={updateChannels}
        />

        <PendingOrders orders={pendingOrders} />

        <div className="full-row">
          <TradeHistory trades={trades} />
        </div>

      </div>

      {sellTarget && (
        <SellModal
          ticker={sellTarget.ticker}
          maxShares={sellTarget.maxShares}
          currentPrice={prices[sellTarget.ticker]}
          onSubmit={async (body) => {
            const result = await placeOrder(body)
            if (result.ok) setSellTarget(null)
            return result
          }}
          onClose={() => setSellTarget(null)}
        />
      )}
    </div>
  )
}
