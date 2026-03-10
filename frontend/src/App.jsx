import { useState, useEffect, useRef, useCallback } from 'react'
import Header from './components/Header'
import Portfolio from './components/Portfolio'
import MarketPrices from './components/MarketPrices'
import PlaceOrder from './components/PlaceOrder'
import PendingOrders from './components/PendingOrders'
import TradeHistory from './components/TradeHistory'
import Notifications from './components/Notifications'
import SellModal from './components/SellModal'

export default function App() {
  const [prices, setPrices] = useState({})
  const [prevPrices, setPrevPrices] = useState({})
  const prevPricesRef = useRef({})
  const [portfolio, setPortfolio] = useState({ cash: 0, holdings: {}, totalValue: 0 })
  const [pendingOrders, setPendingOrders] = useState([])
  const [trades, setTrades] = useState([])
  const [notifications, setNotifications] = useState([])
  const [sellTarget, setSellTarget] = useState(null)
  const [lastRefresh, setLastRefresh] = useState(null)

  const fetchAll = useCallback(async () => {
    try {
      const [pricesRes, portRes, ordersRes, tradesRes, notifsRes] = await Promise.all([
        fetch('/api/prices'),
        fetch('/api/portfolio'),
        fetch('/api/orders/pending'),
        fetch('/api/trades'),
        fetch('/api/notifications'),
      ])
      const [newPrices, newPort, newOrders, newTrades, newNotifs] = await Promise.all([
        pricesRes.json(),
        portRes.json(),
        ordersRes.json(),
        tradesRes.json(),
        notifsRes.json(),
      ])
      setPrevPrices({ ...prevPricesRef.current })
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
  }, [])

  useEffect(() => {
    fetchAll()
    const id = setInterval(fetchAll, 5000)
    return () => clearInterval(id)
  }, [fetchAll])

  const placeOrder = async (body) => {
    const res = await fetch('/api/orders', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body),
    })
    const data = await res.json()
    if (res.ok) fetchAll()
    return { ok: res.ok, data }
  }

  return (
    <div className="app">
      <Header lastRefresh={lastRefresh} />
      <div className="layout">

        <Portfolio
          portfolio={portfolio}
          prices={prices}
          onSell={(ticker, maxShares) => setSellTarget({ ticker, maxShares })}
        />
        <MarketPrices prices={prices} prevPrices={prevPrices} />

        <PlaceOrder tickers={Object.keys(prices)} onOrder={placeOrder} />
        <Notifications notifications={notifications} />

        <div className="full-row">
          <PendingOrders orders={pendingOrders} />
        </div>

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
