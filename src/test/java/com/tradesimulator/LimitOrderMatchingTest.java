package com.tradesimulator;

import com.tradesimulator.model.LimitOrder;
import com.tradesimulator.model.OrderType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for limit order matching logic — corresponds to the Factory pattern (LimitOrder).
 * Verifies that canExecute() fires only when the price threshold condition is met.
 */
class LimitOrderMatchingTest {

    @Test
    void limitBuyExecutesWhenPriceAtOrBelowThreshold() {
        // Arrange
        LimitOrder order = new LimitOrder("AAPL", OrderType.BUY, 5, 150.0);

        // Act & Assert — price exactly at threshold
        assertTrue(order.canExecute(150.0), "Limit buy should execute at exactly the limit price");
    }

    @Test
    void limitBuyExecutesWhenPriceBelowThreshold() {
        // Arrange
        LimitOrder order = new LimitOrder("AAPL", OrderType.BUY, 5, 150.0);

        // Act & Assert — price below threshold
        assertTrue(order.canExecute(149.0), "Limit buy should execute when price falls below limit");
    }

    @Test
    void limitBuyDoesNotExecuteWhenPriceAboveThreshold() {
        // Arrange
        LimitOrder order = new LimitOrder("AAPL", OrderType.BUY, 5, 150.0);

        // Act & Assert
        assertFalse(order.canExecute(151.0), "Limit buy must NOT execute when price is above limit");
    }

    @Test
    void limitSellExecutesWhenPriceAtOrAboveThreshold() {
        // Arrange
        LimitOrder order = new LimitOrder("TSLA", OrderType.SELL, 3, 300.0);

        // Act & Assert — price exactly at threshold
        assertTrue(order.canExecute(300.0), "Limit sell should execute at exactly the limit price");
    }

    @Test
    void limitSellExecutesWhenPriceAboveThreshold() {
        // Arrange
        LimitOrder order = new LimitOrder("TSLA", OrderType.SELL, 3, 300.0);

        // Act & Assert
        assertTrue(order.canExecute(305.0), "Limit sell should execute when price rises above limit");
    }

    @Test
    void limitSellDoesNotExecuteWhenPriceBelowThreshold() {
        // Arrange
        LimitOrder order = new LimitOrder("TSLA", OrderType.SELL, 3, 300.0);

        // Act & Assert
        assertFalse(order.canExecute(299.0), "Limit sell must NOT execute when price is below limit");
    }
}
