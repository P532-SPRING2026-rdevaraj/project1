package com.tradesimulator;

import com.tradesimulator.model.LimitOrder;
import com.tradesimulator.model.MarketOrder;
import com.tradesimulator.model.Order;
import com.tradesimulator.model.OrderType;
import com.tradesimulator.order.OrderFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the Factory pattern — OrderFactory.
 * Verifies correct subtype creation and well-defined failure for unknown kinds.
 */
class OrderFactoryTest {

    private OrderFactory factory;

    @BeforeEach
    void setUp() {
        // Arrange
        factory = new OrderFactory();
    }

    @Test
    void factoryCreatesMarketOrderForMarketKind() {
        // Arrange
        String kind = "MARKET";

        // Act
        Order order = factory.createOrder(kind, "AAPL", OrderType.BUY, 10, 0.0);

        // Assert
        assertInstanceOf(MarketOrder.class, order, "Expected a MarketOrder");
        assertEquals("MARKET", order.getOrderKind());
    }

    @Test
    void factoryCreatesLimitOrderForLimitKind() {
        // Arrange
        String kind = "LIMIT";

        // Act
        Order order = factory.createOrder(kind, "TSLA", OrderType.SELL, 5, 300.0);

        // Assert
        assertInstanceOf(LimitOrder.class, order, "Expected a LimitOrder");
        assertEquals("LIMIT", order.getOrderKind());
        assertEquals(300.0, ((LimitOrder) order).getLimitPrice(), 0.001);
    }

    @Test
    void factoryIsCaseInsensitive() {
        // Arrange
        String lowerKind = "market";

        // Act
        Order order = factory.createOrder(lowerKind, "MSFT", OrderType.BUY, 2, 0.0);

        // Assert
        assertInstanceOf(MarketOrder.class, order);
    }

    @Test
    void factoryThrowsIllegalArgumentExceptionForUnknownKind() {
        // Arrange
        String unknownKind = "STOP_LOSS";

        // Act & Assert
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> factory.createOrder(unknownKind, "AAPL", OrderType.BUY, 1, 0.0));
        assertTrue(ex.getMessage().contains("STOP_LOSS"),
                "Exception message should name the bad kind");
    }

    @Test
    void marketOrderAlwaysCanExecute() {
        // Arrange
        Order order = factory.createOrder("MARKET", "NVDA", OrderType.BUY, 1, 0.0);

        // Act & Assert — market orders execute at any price
        assertTrue(order.canExecute(0.01));
        assertTrue(order.canExecute(99999.0));
    }

    @Test
    void createdOrderHasCorrectAttributes() {
        // Arrange
        String ticker = "AMZN";
        OrderType type = OrderType.BUY;
        int qty = 7;

        // Act
        Order order = factory.createOrder("MARKET", ticker, type, qty, 0.0);

        // Assert
        assertEquals(ticker, order.getTicker());
        assertEquals(type, order.getOrderType());
        assertEquals(qty, order.getQuantity());
    }
}
