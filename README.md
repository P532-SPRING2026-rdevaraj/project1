# TradeSimulator v1.0

A paper-trading platform built for CSCI-P532 (Spring 2026).
Analysts can place buy/sell orders, monitor a live portfolio, and receive notifications when orders execute.

---

## Live Demo

 `https://tradesimulator-7rir.onrender.com`

**Note:** Free-tier Render services spin down after 15 minutes of inactivity and take up to 60 seconds to cold-start.

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

## Design Patterns

| Pattern | Location | Future change protected |
|---------|----------|-------------------------|
| **Strategy** | `PriceUpdateStrategy` interface + `RandomWalkStrategy` | Swap pricing algorithms (e.g. add `MeanReversionStrategy`) without touching `MarketFeed` |
| **Observer** | `PriceObserver` interface; `MarketFeed` (subject) + `OrderService` (observer) | New consumers (risk engine, analytics) register without modifying `MarketFeed` |
| **Decorator** | `NotificationDecorator` + `DashboardNotificationDecorator` wrapping `ConsoleNotificationService` | Stack SMS/email channels without modifying existing notifiers |
| **Factory** | `OrderFactory` | New order types (StopLoss, etc.) added in one place; callers unchanged |
| **Singleton** | `Portfolio`, `MarketFeed`, `OrderService` as Spring `@Service` beans | Application-scoped state has exactly one instance; avoids inconsistent portfolio state |

---

## Architecture

```
MarketFeed (Singleton, Observable)
  ├── PriceUpdateStrategy (Strategy) ← RandomWalkStrategy
  └── notifies → PriceObserver implementations
                   └── OrderService (evaluates pending LimitOrders)
                         ├── OrderFactory (Factory) → MarketOrder / LimitOrder
                         ├── Portfolio (Singleton) — cash + holdings + trade history
                         └── NotificationService (Decorator chain)
                               ConsoleNotificationService
                               └── DashboardNotificationDecorator
```

---

## CI/CD

GitHub Actions workflow at [`.github/workflows/ci.yml`](.github/workflows/ci.yml):

1. **test** — runs `mvn test`, uploads Surefire reports
2. **build** — packages the fat JAR, builds the Docker image
3. **deploy** — triggers Render deploy hook (main branch only)

The Render deploy hook URL is stored as the GitHub secret `RENDER_DEPLOY_HOOK`.

---

## Technology Stack

- Java 17 + Spring Boot 3.2
- Plain HTML/CSS/JavaScript frontend (served as static resource)
- In-memory persistence (no database)
- Maven build tool
- Docker (multi-stage build)
- JUnit 5 + Mockito for unit tests
- GitHub Actions for CI/CD
- Render.com for deployment
