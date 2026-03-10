const fmt = (n) => '$' + Number(n).toFixed(2)

export default function TradeHistory({ trades }) {
  const sorted = [...trades].reverse()

  return (
    <div className="card">
      <h2 className="card-title">
        Trade History
        {trades.length > 0 && <span className="count-badge">{trades.length}</span>}
      </h2>

      {trades.length === 0 ? (
        <p className="empty-msg">No trades executed yet.</p>
      ) : (
        <div className="table-scroll">
          <table className="table">
            <thead>
              <tr>
                <th>Time</th>
                <th>Ticker</th>
                <th>Side</th>
                <th>Qty</th>
                <th>Price</th>
                <th>Total Value</th>
              </tr>
            </thead>
            <tbody>
              {sorted.map((t, i) => (
                <tr key={i}>
                  <td className="muted">
                    {new Date(t.timestamp).toLocaleTimeString()}
                  </td>
                  <td><strong className="ticker-name">{t.ticker}</strong></td>
                  <td>
                    <span className={`badge badge-${t.side.toLowerCase()}`}>
                      {t.side}
                    </span>
                  </td>
                  <td>{t.quantity}</td>
                  <td>{fmt(t.price)}</td>
                  <td className="value-cell">{fmt(t.totalValue)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  )
}
