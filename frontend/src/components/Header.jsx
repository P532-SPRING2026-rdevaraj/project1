export default function Header({ lastRefresh }) {
  return (
    <header className="header">
      <div className="header-brand">
        <div className="header-wordmark">
          <h1 className="header-title">Trade<span>Sim</span></h1>
        </div>
        <div className="header-divider" />
        <span className="header-sub">Paper Trading Platform</span>
      </div>
      <div className="header-meta">
        {lastRefresh && (
          <span className="header-refresh">
            {lastRefresh.toLocaleTimeString()}
          </span>
        )}
        <div className="header-live">
          <span className="live-dot" />
          LIVE
        </div>
      </div>
    </header>
  )
}
