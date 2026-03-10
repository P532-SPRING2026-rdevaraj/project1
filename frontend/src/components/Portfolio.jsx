const fmt = (n) => '$' + Number(n).toFixed(2)

export default function Portfolio({ portfolio, prices, onSell }) {
  const holdings = portfolio.holdings || {}
  const hasHoldings = Object.keys(holdings).length > 0

  return (
    <div className="card">
      <h2 className="card-title">Portfolio</h2>

      <div className="stats-row">
        <div className="stat">
          <span className="stat-label">Cash Balance</span>
          <span className="stat-value">{fmt(portfolio.cash || 0)}</span>
        </div>
        <div className="stat">
          <span className="stat-label">Total Value</span>
          <span className="stat-value highlight">{fmt(portfolio.totalValue || 0)}</span>
        </div>
      </div>

      <h3 className="section-sub">Holdings</h3>

      {!hasHoldings ? (
        <p className="empty-msg">No holdings yet. Place a buy order to get started.</p>
      ) : (
        <table className="table">
          <thead>
            <tr>
              <th>Ticker</th>
              <th>Shares</th>
              <th>Curr. Price</th>
              <th>Value</th>
              <th>Action</th>
            </tr>
          </thead>
          <tbody>
            {Object.entries(holdings).map(([ticker, shares]) => {
              const price = prices[ticker] || 0
              const value = price * shares
              return (
                <tr key={ticker}>
                  <td><strong className="ticker-name">{ticker}</strong></td>
                  <td>{shares}</td>
                  <td>{fmt(price)}</td>
                  <td className="value-cell">{fmt(value)}</td>
                  <td>
                    <button
                      className="btn btn-danger btn-sm"
                      onClick={() => onSell(ticker, shares)}
                    >
                      Sell
                    </button>
                  </td>
                </tr>
              )
            })}
          </tbody>
        </table>
      )}
    </div>
  )
}
