# TradeSimulator

A paper-trading platform built for CSCI-P532 (Spring 2026).
Analysts can place buy/sell orders, monitor a live portfolio, and receive notifications when orders execute.

---

## Live Demo

| Service | URL |
|---|---|
| **Backend (Render)** | [https://tradesimulator-7rir.onrender.com](https://tradesimulator-7rir.onrender.com) |
| **Frontend (GitHub Pages)** | [https://rohithgowdadevaraju.github.io/Project1/](https://rohithgowdadevaraju.github.io/Project1/) |

> Free-tier Render services spin down after 15 minutes of inactivity and take up to 60 seconds to cold-start.

---

## Running Locally with Docker

No Java installation required — Docker only.

```bash
# Build the image
docker build -t tradesimulator .

# Run the container
docker run -p 8080:8080 tradesimulator
```

Open [http://localhost:8080](http://localhost:8080) in your browser.

---

## Running Locally with Maven (Java 17+ required)

```bash
mvn spring-boot:run
```

---

## Running Tests

```bash
mvn test
```

Test reports are written to `target/surefire-reports/`.

---

## Features (Week 2)

- **3 pricing algorithms** — Random Walk, Mean Reversion, Trend Following; switchable from the UI at runtime
- **Multi-channel notifications** — Console, Email, SMS, Dashboard; configurable per user
- **Multiple users** — Alice, Bob, Charlie each with independent portfolio, order book, trade history, and notification preferences
- **Market + limit orders** — limit orders evaluated automatically on every price tick
- **Live price feed** — prices update every 5 seconds

---

## Design Patterns

| Pattern | Location | Change protected against |
|---|---|---|
| **Strategy** | `PriceUpdateStrategy` + `RandomWalkStrategy`, `MeanReversionStrategy`, `TrendFollowingStrategy` | Add new pricing models without touching `MarketFeed` |
| **Observer** | `PriceObserver` interface; `MarketFeed` (subject) + `UserRegistry` (observer) | Add new price consumers without modifying `MarketFeed` |
| **Decorator** | `NotificationDecorator` + `Email/Sms/DashboardNotificationDecorator` | Stack notification channels without modifying existing notifiers |
| **Factory** | `OrderFactory` + `DefaultOrderFactory` | Add new order types in one place; callers unchanged |
| **Singleton** | `MarketFeed`, `UserRegistry` as Spring `@Service` beans | Single source of truth for prices and user state |

---

## Architecture

```
MarketFeed (Singleton, Observable)
  ├── PriceUpdateStrategy (Strategy) ← RandomWalk / MeanReversion / TrendFollowing
  └── notifies → PriceObserver
                   └── UserRegistry (Singleton)
                         └── User × 3 (alice, bob, charlie)
                               ├── Portfolio — cash + holdings + trade history
                               ├── pending orders: List<Order>  ← OrderFactory
                               └── NotificationService (Decorator chain)
                                     Console → [Email] → [SMS] → Dashboard
```

---

## CI/CD

GitHub Actions workflow at [`.github/workflows/ci.yml`](.github/workflows/ci.yml):

1. **test** — runs `mvn test`, uploads Surefire reports
2. **build** — packages the fat JAR, builds the Docker image
3. **deploy** — triggers Render deploy hook (main branch only)

Frontend deployed separately via [`.github/workflows/frontend.yml`](.github/workflows/frontend.yml) to GitHub Pages.

Secrets required: `RENDER_DEPLOY_HOOK`, `VITE_API_BASE_URL`

---

## Technology Stack

- Java 17 + Spring Boot 3.2
- React 18 + Vite (frontend)
- In-memory persistence (no database)
- Maven build tool
- Docker (multi-stage build)
- JUnit 5 + Mockito (42 unit tests)
- GitHub Actions for CI/CD
- Render.com (backend) + GitHub Pages (frontend)
