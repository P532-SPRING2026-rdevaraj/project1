import { useState } from 'react'

const fmt = (n) => '$' + Number(n).toFixed(2)

export default function SellModal({ ticker, maxShares, currentPrice, onSubmit, onClose }) {
  const [quantity, setQuantity] = useState(1)
  const [orderKind, setOrderKind] = useState('MARKET')
  const [limitPrice, setLimitPrice] = useState(currentPrice ? currentPrice.toFixed(2) : '')
  const [msg, setMsg] = useState(null)
  const [loading, setLoading] = useState(false)

  const estimatedValue =
    orderKind === 'MARKET'
      ? (currentPrice || 0) * quantity
      : (Number(limitPrice) || 0) * quantity

  const handleSubmit = async () => {
    const qty = Number(quantity)
    if (!qty || qty < 1) {
      setMsg({ type: 'error', text: 'Enter a valid quantity.' })
      return
    }
    if (qty > maxShares) {
      setMsg({ type: 'error', text: `You only own ${maxShares} shares.` })
      return
    }
    if (orderKind === 'LIMIT' && (!limitPrice || Number(limitPrice) <= 0)) {
      setMsg({ type: 'error', text: 'Enter a valid limit price.' })
      return
    }

    setLoading(true)
    setMsg(null)

    const { ok, data } = await onSubmit({
      ticker,
      orderType: 'SELL',
      orderKind,
      quantity: qty,
      limitPrice: orderKind === 'LIMIT' ? Number(limitPrice) : 0,
    })

    setLoading(false)

    if (!ok) {
      setMsg({ type: 'error', text: data.error || 'Order failed.' })
    } else {
      setMsg({
        type: data.status === 'EXECUTED' ? 'success' : 'error',
        text: `Order ${data.status}`,
      })
    }
  }

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal" onClick={(e) => e.stopPropagation()}>

        <div className="modal-header">
          <div className="modal-title">
            <span className="badge badge-sell">SELL</span>
            <h3>{ticker}</h3>
          </div>
          <button className="modal-close" onClick={onClose}>✕</button>
        </div>

        <div className="modal-info-bar">
          <span>You own: <strong>{maxShares} shares</strong></span>
          {currentPrice && (
            <span>Current price: <strong className="price-highlight">{fmt(currentPrice)}</strong></span>
          )}
        </div>

        <div className="modal-body">

          <div className="form-row">
            <div className="form-group">
              <label className="form-label">Order Kind</label>
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
              <label className="form-label">Quantity (max {maxShares})</label>
              <input
                type="number"
                className="form-control"
                min={1}
                max={maxShares}
                value={quantity}
                onChange={(e) => setQuantity(e.target.value)}
              />
            </div>

            {orderKind === 'LIMIT' && (
              <div className="form-group">
                <label className="form-label">Limit Price ($)</label>
                <input
                  type="number"
                  className="form-control"
                  min={0.01}
                  step={0.01}
                  value={limitPrice}
                  onChange={(e) => setLimitPrice(e.target.value)}
                />
              </div>
            )}
          </div>

          {estimatedValue > 0 && (
            <div className="modal-estimate">
              Estimated {orderKind === 'MARKET' ? 'proceeds' : 'target value'}:{' '}
              <strong>{fmt(estimatedValue)}</strong>
            </div>
          )}

          {orderKind === 'LIMIT' && (
            <p className="modal-hint">
              This order will execute when {ticker} reaches or exceeds {fmt(Number(limitPrice) || 0)}.
            </p>
          )}

          {msg && <div className={`order-msg ${msg.type}`}>{msg.text}</div>}
        </div>

        <div className="modal-footer">
          <button className="btn btn-secondary" onClick={onClose} disabled={loading}>
            Cancel
          </button>
          <button className="btn btn-danger" onClick={handleSubmit} disabled={loading}>
            {loading ? 'Processing…' : `Sell ${quantity} × ${ticker}`}
          </button>
        </div>

      </div>
    </div>
  )
}
