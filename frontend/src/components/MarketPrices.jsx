const fmt = (n) => '$' + Number(n).toFixed(2)

export default function MarketPrices({ prices, prevPrices }) {
  return (
    <div className="card">
      <h2 className="card-title">Live Market Prices</h2>
      <div className="prices-grid">
        {Object.entries(prices).map(([ticker, price]) => {
          const prev = prevPrices[ticker]
          let changeStr = ''
          let changeClass = ''
          let arrow = ''

          if (prev !== undefined && prev !== price) {
            const diff = price - prev
            const pct = ((diff / prev) * 100).toFixed(2)
            changeStr = (diff >= 0 ? '+' : '') + pct + '%'
            changeClass = diff >= 0 ? 'price-up' : 'price-down'
            arrow = diff >= 0 ? '▲' : '▼'
          }

          return (
            <div key={ticker} className={`price-card ${changeClass}`}>
              <span className="price-ticker">{ticker}</span>
              <span className="price-value">{fmt(price)}</span>
              {changeStr && (
                <span className={`price-change ${changeClass}`}>
                  {arrow} {changeStr}
                </span>
              )}
            </div>
          )
        })}
      </div>
    </div>
  )
}
