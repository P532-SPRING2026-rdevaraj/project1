const STRATEGIES = [
  { id: 'random-walk',     label: 'Random Walk',      desc: '±2% random change each tick' },
  { id: 'mean-reversion',  label: 'Mean Reversion',   desc: 'Prices drift back toward their moving average' },
  { id: 'trend-following', label: 'Trend Following',  desc: 'Prices amplify their recent momentum' },
]

export default function StrategySelector({ strategy, onSwitch }) {
  return (
    <div>
      <h2 className="card-title">Pricing Algorithm</h2>
      <div className="strategy-list">
        {STRATEGIES.map(s => (
          <button
            key={s.id}
            className={`strategy-btn ${strategy === s.id ? 'strategy-btn--active' : ''}`}
            onClick={() => onSwitch(s.id)}
            title={s.desc}
          >
            {s.label}
          </button>
        ))}
      </div>
      <p className="strategy-desc">
        {STRATEGIES.find(s => s.id === strategy)?.desc}
      </p>
    </div>
  )
}
