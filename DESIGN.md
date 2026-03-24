# TradeSimulator — Design Document
**CSCI-P532 | Spring 2026 | Rohith Gowda Devaraju**

---

## Component Diagram (Week 2)

```
┌──────────────────────────────────────────────────────────────────────────────────────┐
│                                  REST Layer                                          │
│                               TradeController                                        │
│  /prices  /strategy  /users  /portfolio  /orders  /trades  /notifications  /channels │
└────┬──────────┬──────────────────┬──────────────────────┬────────────────────────────┘
     │          │                  │                      │
     ▼          ▼                  ▼                      ▼
┌───────────┐ ┌──────────────────┐ ┌──────────────────────────────────────────────────┐
│MarketFeed │ │ PriceUpdateStrat │ │               UserRegistry (Singleton)           │
│(Singleton)│ │    «interface»   │ │  users: { alice → User, bob → User, charlie→User}│
│Observable │ ├──────────────────┤ │  implements PriceObserver                        │
│@Scheduled │ │ RandomWalk       │ └───────────────────┬──────────────────────────────┘
│ 5s tick   │ │ MeanReversion    │                     │ each User owns
└────┬──────┘ │ TrendFollowing   │        ┌────────────▼───────────────────────────┐
     │        └──────────────────┘        │  User                                  │
     │ notifies                           │  ├── Portfolio (cash, holdings, trades)│
     ▼                                    │  ├── pendingOrders: List<Order>        │
┌─────────────┐                           │  └── NotificationService (chain)       │
│PriceObserver│                           │       ConsoleNotificationService       │
│ «interface» │                           │       [→ EmailDecorator]               │
└────┬────────┘                           │       [→ SmsDecorator]                 │
     │ implements                         │       → DashboardDecorator             │
     ▼                                    └────────────────────────────────────────┘
┌─────────────┐   ┌──────────────┐
│ UserRegistry│   │ OrderFactory │ → MarketOrder / LimitOrder
└─────────────┘   └──────────────┘
```

---

## Design Patterns — Pattern Audit

### 1. Strategy — `PriceUpdateStrategy`
**Location:** `PriceUpdateStrategy` (interface), `RandomWalkStrategy`, `MeanReversionStrategy`, `TrendFollowingStrategy`; wired into `MarketFeed` which calls `strategy.updatePrices()` every tick; swapped at runtime via `POST /api/strategy`.
**Change it protects against:** Adding a new pricing model requires only one new class implementing `PriceUpdateStrategy` — `MarketFeed`, `TradeController`, and all tests remain untouched.

---

### 2. Observer — `PriceObserver`
**Location:** `PriceObserver` (interface), `MarketFeed` (subject, calls `notifyObservers()` after each tick), `UserRegistry` (observer, evaluates every user's pending limit orders on each price update).
**Change it protects against:** Any new component that needs to react to price changes (e.g. a risk engine or analytics service) registers as a `PriceObserver` without any modification to `MarketFeed`.

---

### 3. Decorator — `NotificationDecorator`
**Location:** `NotificationService` (interface), `NotificationDecorator` (abstract base), `ConsoleNotificationService` (innermost), `EmailNotificationDecorator`, `SmsNotificationDecorator`, `DashboardNotificationDecorator`; rebuilt per user in `User.rebuildNotificationChain()`.
**Change it protects against:** Adding or removing a notification channel requires only a new decorator class — no existing notifier is modified, and channels can be stacked in any combination per user.

---

### 4. Factory — `OrderFactory`
**Location:** `OrderFactory` (interface), `DefaultOrderFactory`; called by `UserRegistry.placeOrder()` to create `MarketOrder` or `LimitOrder` from a plain string kind.
**Change it protects against:** Adding a new order type (e.g. `StopLossOrder`) means adding one class and one `case` in `DefaultOrderFactory` — all callers remain unchanged.

---

### 5. Singleton — `MarketFeed`, `UserRegistry`
**Location:** Both are Spring `@Service` beans (singleton scope by default); `MarketFeed` is the single source of live prices; `UserRegistry` is the single registry of all user state (portfolios, orders, notifications).
**Change it protects against:** Ensures a single source of truth for market prices and user state — multiple instances would cause inconsistent portfolio balances and duplicate price notifications.

---

## Final Report — Volatility Missed in Week 1

**Missed volatility:** Multiple users with independent portfolios.

In Week 1, `Portfolio` was designed as a single application-scoped singleton with one cash balance and one holdings map. There was no concept of a user. When Change 3 required three independent analysts, the singleton Portfolio had to be demoted from a Spring bean to a plain class instantiated per user, and a new `UserRegistry` and `User` model had to be introduced to hold per-user state.

**What I would do differently:** Even in Week 1, I would have stored portfolio state in a structure keyed by user ID rather than a flat singleton. A `UserRegistry` managing a `Map<String, User>` (where each `User` owns a `Portfolio`) would have made Change 3 additive — new files only, zero modifications to existing ones. The Observer wiring also should have been scoped per user from the start, since global observers required filtering when multiple users were introduced.

---

## Key Volatilities and Patterns

| Axis of change | Pattern |
|---|---|
| Swap pricing algorithm | Strategy |
| Add new price consumers (risk, analytics) | Observer |
| Add/remove notification channels per user | Decorator |
| Add new order types (StopLoss, etc.) | Factory |
| Single source of truth for prices and user state | Singleton |
