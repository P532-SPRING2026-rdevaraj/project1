package com.tradesimulator;

import com.tradesimulator.portfolio.Portfolio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for order execution — corresponds to the Portfolio / Singleton pattern.
 * Verifies cash and holdings change correctly for buy/sell and that constraints are enforced.
 */
class OrderExecutionTest {

    private Portfolio portfolio;

    @BeforeEach
    void setUp() {
        // Arrange — fresh portfolio with $10,000 cash
        portfolio = new Portfolio();
    }

    @Test
    void marketBuyReducesCashAndIncreasesHoldings() {
        // Arrange
        double price = 100.0;
        int qty = 5;

        // Act
        boolean result = portfolio.buy("AAPL", qty, price);

        // Assert
        assertTrue(result, "Buy should succeed when funds are sufficient");
        assertEquals(9500.0, portfolio.getCash(), 0.001);
        assertEquals(5, portfolio.getHoldings().get("AAPL"));
    }

    @Test
    void marketSellIncreasesCashAndReducesHoldings() {
        // Arrange — pre-load shares
        portfolio.buy("AAPL", 10, 100.0);
        double sellPrice = 120.0;

        // Act
        boolean result = portfolio.sell("AAPL", 5, sellPrice);

        // Assert
        assertTrue(result, "Sell should succeed when enough shares are held");
        assertEquals(9000.0 + 600.0, portfolio.getCash(), 0.001); // 10000 - 1000 + 600
        assertEquals(5, portfolio.getHoldings().get("AAPL"));
    }

    @Test
    void sellAllSharesRemovesTickerFromHoldings() {
        // Arrange
        portfolio.buy("TSLA", 3, 200.0);

        // Act
        portfolio.sell("TSLA", 3, 200.0);

        // Assert
        assertFalse(portfolio.getHoldings().containsKey("TSLA"),
                "Ticker should be removed after selling all shares");
    }

    @Test
    void buyWithInsufficientFundsIsRejected() {
        // Arrange — try to buy more than $10,000 worth
        double price = 5000.0;
        int qty = 3; // cost = $15,000

        // Act
        boolean result = portfolio.buy("GOOG", qty, price);

        // Assert
        assertFalse(result, "Buy should be rejected when funds are insufficient");
        assertEquals(10_000.0, portfolio.getCash(), 0.001, "Cash must not change on rejected buy");
        assertFalse(portfolio.getHoldings().containsKey("GOOG"),
                "Holdings must not change on rejected buy");
    }

    @Test
    void sellUnownedSharesIsRejected() {
        // Arrange — no shares of MSFT held

        // Act
        boolean result = portfolio.sell("MSFT", 1, 380.0);

        // Assert
        assertFalse(result, "Sell should be rejected when no shares are held");
        assertEquals(10_000.0, portfolio.getCash(), 0.001, "Cash must not change on rejected sell");
    }

    @Test
    void sellMoreSharesThanOwnedIsRejected() {
        // Arrange
        portfolio.buy("NVDA", 2, 100.0);

        // Act
        boolean result = portfolio.sell("NVDA", 5, 100.0);

        // Assert
        assertFalse(result, "Sell should be rejected when quantity exceeds holdings");
        assertEquals(2, portfolio.getHoldings().get("NVDA"),
                "Holdings must not change on rejected sell");
    }
}
