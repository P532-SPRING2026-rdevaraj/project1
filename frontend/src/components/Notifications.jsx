export default function Notifications({ notifications }) {
  const recent = [...notifications].reverse().slice(0, 20)

  return (
    <div className="card">
      <h2 className="card-title">
        Notifications
        {notifications.length > 0 && (
          <span className="count-badge">{notifications.length}</span>
        )}
      </h2>

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
