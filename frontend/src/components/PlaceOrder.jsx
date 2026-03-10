import { useState } from 'react'

export default function PlaceOrder({ tickers, onOrder }) {
  const [ticker, setTicker] = useState('')
  const [orderType, setOrderType] = useState('BUY')
  const [orderKind, setOrderKind] = useState('MARKET')
  const [quantity, setQuantity] = useState(1)
  const [limitPrice, setLimitPrice] = useState('')
  const [msg, setMsg] = useState(null)
  const [loading, setLoading] = useState(false)

  const handleSubmit = async () => {
    if (!ticker) { setMsg({ type: 'error', text: 'Select a ticker.' }); return }
    if (!quantity || quantity < 1) { setMsg({ type: 'error', text: 'Enter a valid quantity.' }); return }
    if (orderKind === 'LIMIT' && (!limitPrice || Number(limitPrice) <= 0)) {
      setMsg({ type: 'error', text: 'Enter a valid limit price.' })
      return
    }

    setLoading(true)
    setMsg(null)

    const { ok, data } = await onOrder({
      ticker,
      orderType,
      orderKind,
      quantity: Number(quantity),
      limitPrice: orderKind === 'LIMIT' ? Number(limitPrice) : 0,
    })

    setLoading(false)

    if (!ok) {
      setMsg({ type: 'error', text: data.error || 'Order failed.' })
    } else {
      setMsg({
        type: data.status === 'EXECUTED' ? 'success' : 'error',
        text: `Order ${data.status}: ${data.orderType} ${data.quantity} ${data.ticker}`,
      })
    }
  }

  return (
    <div className="card">
      <h2 className="card-title">Place Order</h2>

      <div className="form-row">
        <div className="form-group">
          <label className="form-label">Ticker</label>
          <select
            className="form-control"
            value={ticker}
            onChange={(e) => setTicker(e.target.value)}
          >
            <option value="">Select…</option>
            {tickers.map((t) => (
              <option key={t} value={t}>{t}</option>
            ))}
          </select>
        </div>

        <div className="form-group">
          <label className="form-label">Side</label>
          <div className="side-toggle">
            <button
              className={`toggle-btn ${orderType === 'BUY' ? 'active-buy' : ''}`}
              onClick={() => setOrderType('BUY')}
            >
              BUY
            </button>
            <button
              className={`toggle-btn ${orderType === 'SELL' ? 'active-sell' : ''}`}
              onClick={() => setOrderType('SELL')}
            >
              SELL
            </button>
          </div>
        </div>

        <div className="form-group">
          <label className="form-label">Kind</label>
          <select
            className="form-control"
            value={orderKind}
            onChange={(e) => setOrderKind(e.target.value)}
          >
            <option value="MARKET">MARKET</option>
            <option value="LIMIT">LIMIT</option>
          </select>
        </div>

        <div className="form-group">
          <label className="form-label">Qty</label>
          <input
            type="number"
            className="form-control form-control-sm"
            min={1}
            value={quantity}
            onChange={(e) => setQuantity(e.target.value)}
          />
        </div>

        {orderKind === 'LIMIT' && (
          <div className="form-group">
            <label className="form-label">Limit Price ($)</label>
            <input
              type="number"
              className="form-control form-control-sm"
              min={0.01}
              step={0.01}
              value={limitPrice}
              onChange={(e) => setLimitPrice(e.target.value)}
            />
          </div>
        )}

        <div className="form-group form-group-submit">
          <label className="form-label">&nbsp;</label>
          <button
            className={`btn ${orderType === 'BUY' ? 'btn-primary' : 'btn-danger'}`}
            onClick={handleSubmit}
            disabled={loading}
          >
            {loading ? '…' : orderType}
          </button>
        </div>
      </div>

      {msg && <div className={`order-msg ${msg.type}`}>{msg.text}</div>}
    </div>
  )
}
