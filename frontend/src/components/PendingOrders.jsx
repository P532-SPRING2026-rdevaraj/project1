const fmt = (n) => '$' + Number(n).toFixed(2)

export default function PendingOrders({ orders }) {
  return (
    <div className="card">
      <h2 className="card-title">
        Pending Limit Orders
        {orders.length > 0 && <span className="count-badge">{orders.length}</span>}
      </h2>

      {orders.length === 0 ? (
        <p className="empty-msg">No pending orders.</p>
      ) : (
        <table className="table">
          <thead>
            <tr>
              <th>Ticker</th>
              <th>Side</th>
              <th>Kind</th>
              <th>Qty</th>
              <th>Limit Price</th>
              <th>Status</th>
              <th>Created</th>
            </tr>
          </thead>
          <tbody>
            {orders.map((o) => (
              <tr key={o.id}>
                <td><strong className="ticker-name">{o.ticker}</strong></td>
                <td>
                  <span className={`badge badge-${o.orderType.toLowerCase()}`}>
                    {o.orderType}
                  </span>
                </td>
                <td>{o.orderKind}</td>
                <td>{o.quantity}</td>
                <td>{o.limitPrice != null ? fmt(o.limitPrice) : '—'}</td>
                <td>
                  <span className="badge badge-pending">{o.status}</span>
                </td>
                <td className="muted">
                  {o.createdAt ? new Date(o.createdAt).toLocaleTimeString() : '—'}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  )
}
