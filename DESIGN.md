# TradeSimulator v1.0 — Design Document
**CSCI-P532 | Spring 2026 | Rohith Gowda Devaraju**

---

## Component Diagram

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                        REST Layer                                               │
│                     TradeController                                             │
│  GET /api/prices  GET /api/portfolio  POST /api/orders  GET /api/notifications  │
└────────┬──────────────┬──────────────────┬──────────────┬───────────────────────┘
         │              │                  │              │
         ▼              ▼                  ▼              ▼
  ┌─────────────┐ ┌───────────┐  ┌───────────────┐  ┌──────────────────────────┐
  │  MarketFeed │ │ Portfolio │  │  OrderService │  │ DashboardNotification    │
  │ (Singleton) │ │(Singleton)│  │  (Singleton,  │  │ Decorator                │
  │             │ │           │  │ PriceObserver)│  │ (Decorator)              │
  │ @Scheduled  │ │ cash      │  │               │  └──────────┬───────────────┘
  │ every 5s    │ │ holdings  │  │ pendingOrders │             │ wraps
  │             │ │ trades    │  │               │  ┌──────────▼───────────────┐
  └──────┬──────┘ └───────────┘  └──────┬────────┘  │ ConsoleNotification      │
         │                              │           │ Service                  │
         │ uses                         │ uses      │ (NotificationService)    │
         ▼                              ▼           └──────────────────────────┘
  ┌──────────────────┐         ┌──────────────┐
  │ PriceUpdate      │         │ OrderFactory │
  │ Strategy         │         │ (Factory)    │
  │ «interface»      │         │              │
  └──────┬───────────┘         └──────┬───────┘
         │ implements                  │ creates
         ▼                             ▼
  ┌──────────────────┐     ┌───────────┐  ┌────────────┐
  │ RandomWalk       │     │MarketOrder│  │ LimitOrder │
  │ Strategy         │     │           │  │            │
  └──────────────────┘     └───────────┘  └────────────┘
         ▲
         │ notifies (Observer)
  ┌──────┴──────┐
  │PriceObserver│
  │ «interface» │
  └─────────────┘
         ▲
         │ implements
  ┌──────┴──────┐
  │ OrderService│  ← evaluates pending LimitOrders on every price tick
  └─────────────┘
```

---

## Design Patterns

### 1. Strategy — `PriceUpdateStrategy` + `RandomWalkStrategy`
**Classes:** `PriceUpdateStrategy` (interface), `RandomWalkStrategy` (concrete)
**Justification:** Encapsulates the pricing algorithm behind a common interface so a second algorithm (e.g. `MeanReversionStrategy`) can be introduced by adding one class and calling `marketFeed.setStrategy(...)`, with zero changes to `MarketFeed`.

---

### 2. Observer — `PriceObserver` + `MarketFeed` + `OrderService`
**Classes:** `PriceObserver` (interface), `MarketFeed` (subject), `OrderService` (observer)
**Justification:** Decouples the price engine from order evaluation so new consumers (e.g. a risk engine or analytics service) can register as observers without modifying `MarketFeed`.

---

### 3. Decorator — `NotificationDecorator` + `DashboardNotificationDecorator` + `ConsoleNotificationService`
**Classes:** `NotificationService` (interface), `NotificationDecorator` (abstract), `DashboardNotificationDecorator`, `ConsoleNotificationService`
**Justification:** Notification channels (SMS, email) can be stacked at runtime by wrapping the existing chain without modifying any existing notifier class.

---

### 4. Factory — `OrderFactory`
**Classes:** `OrderFactory`, `MarketOrder`, `LimitOrder`, `Order` (abstract)
**Justification:** Centralises order creation so new order types (e.g. `StopLossOrder`) are added in one place — `OrderFactory` — leaving all callers (`OrderService`, `TradeController`) unchanged.

---

### 5. Singleton — `Portfolio`, `MarketFeed`, `OrderService`
**Classes:** `Portfolio`, `MarketFeed`, `OrderService` (all Spring `@Service` beans)
**Justification:** Application-scoped state (cash balance, live prices, pending orders) must have exactly one instance; Spring's default singleton scope enforces this and prevents inconsistent portfolio state from multiple instances.

---

## Key Volatilities Protected

| Axis of change | Pattern protecting it |
|---|---|
| Swap pricing algorithm | Strategy |
| Add new price consumers (risk, analytics) | Observer |
| Add notification channels (SMS, email) | Decorator |
| Add new order types (StopLoss, TrailingStop) | Factory |
| Ensure single source of truth for state | Singleton |
