const ALL_CHANNELS = [
  { id: 'console',   label: 'Console' },
  { id: 'email',     label: 'Email'   },
  { id: 'sms',       label: 'SMS'     },
  { id: 'dashboard', label: 'Dashboard' },
]

export default function Notifications({ notifications, channels, onChannelChange }) {
  const recent = [...notifications].reverse().slice(0, 20)

  const toggle = (channelId) => {
    const next = new Set(channels)
    // console and dashboard are always enabled
    if (channelId === 'console' || channelId === 'dashboard') return
    if (next.has(channelId)) next.delete(channelId)
    else next.add(channelId)
    onChannelChange(next)
  }

  return (
    <div className="card">
      <h2 className="card-title">
        Notifications
        {notifications.length > 0 && (
          <span className="count-badge">{notifications.length}</span>
        )}
      </h2>

      {/* Notification channel toggles — Change 2 */}
      <div className="channel-toggles">
        {ALL_CHANNELS.map(ch => (
          <label key={ch.id} className="channel-label">
            <input
              type="checkbox"
              checked={channels.has(ch.id)}
              onChange={() => toggle(ch.id)}
              disabled={ch.id === 'console' || ch.id === 'dashboard'}
            />
            {ch.label}
          </label>
        ))}
      </div>

      {recent.length === 0 ? (
        <p className="empty-msg">No notifications yet.</p>
      ) : (
        <div className="notif-list">
          {recent.map((msg, i) => {
            const isExecuted = msg.includes('EXECUTED')
            const isRejected = msg.includes('REJECTED')
            return (
              <div
                key={i}
                className={`notif-item ${isExecuted ? 'notif-success' : isRejected ? 'notif-error' : ''}`}
              >
                <span className="notif-dot" />
                {msg}
              </div>
            )
          })}
        </div>
      )}
    </div>
  )
}
