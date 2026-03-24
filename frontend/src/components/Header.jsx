export default function Header({ lastRefresh, users, userId, onUserChange }) {
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
        {/* User switcher — Change 3 */}
        {users.length > 0 && (
          <div className="user-switcher">
            <label htmlFor="user-select" className="user-label">Analyst:</label>
            <select
              id="user-select"
              className="user-select"
              value={userId}
              onChange={e => onUserChange(e.target.value)}
            >
              {users.map(u => (
                <option key={u.id} value={u.id}>{u.name}</option>
              ))}
            </select>
          </div>
        )}

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
