package com.tradesimulator;

import com.tradesimulator.model.OrderType;
import com.tradesimulator.model.User;
import com.tradesimulator.notification.ConsoleNotificationService;
import com.tradesimulator.order.DefaultOrderFactory;
import com.tradesimulator.portfolio.UserRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests for Change 3 — Multiple Users.
 * Verifies that each user has independent portfolio state, order books,
 * and configurable notification channels.
 */
@ExtendWith(MockitoExtension.class)
class MultiUserTest {

    @Mock
    private com.tradesimulator.market.MarketFeed marketFeed;

    private UserRegistry userRegistry;
    private ConsoleNotificationService console;
    private Map<String, Double> prices;

    @BeforeEach
    void setUp() {
        // Arrange — stub the market feed so observers register without scheduling
        doNothing().when(marketFeed).addObserver(any());

        console = new ConsoleNotificationService();
        userRegistry = new UserRegistry(new DefaultOrderFactory(), console, marketFeed);

        prices = Map.of(
                "AAPL", 100.0,
                "GOOG", 200.0,
                "TSLA", 150.0
        );
    }

    @Test
    void threeUsersAreCreatedByDefault() {
        // Act
        var users = userRegistry.getAllUsers();

        // Assert
        assertEquals(3, users.size());
    }

    @Test
    void eachUserStartsWithTenThousandCash() {
        // Act & Assert
        for (User u : userRegistry.getAllUsers()) {
            assertEquals(10_000.0, u.getPortfolio().getCash(), 0.001,
                    u.getName() + " should start with $10,000");
        }
    }

    @Test
    void buyForAliceDoesNotAffectBobsPortfolio() {
        // Arrange
        User alice = userRegistry.getUser("alice");
        User bob   = userRegistry.getUser("bob");

        // Act
        userRegistry.placeOrder("alice", "market", "AAPL",
                OrderType.BUY, 5, 0.0, prices);

        // Assert — only Alice's cash changed
        assertEquals(9500.0, alice.getPortfolio().getCash(), 0.001);
        assertEquals(10_000.0, bob.getPortfolio().getCash(), 0.001,
                "Bob's cash must not be affected by Alice's trade");
    }

    @Test
    void eachUserHasSeparateTradeHistory() {
        // Arrange
        userRegistry.placeOrder("alice",   "market", "AAPL", OrderType.BUY, 2, 0.0, prices);
        userRegistry.placeOrder("bob",     "market", "GOOG", OrderType.BUY, 1, 0.0, prices);

        // Act & Assert
        assertEquals(1, userRegistry.getUser("alice").getPortfolio().getTradeHistory().size());
        assertEquals(1, userRegistry.getUser("bob").getPortfolio().getTradeHistory().size());
        assertEquals(0, userRegistry.getUser("charlie").getPortfolio().getTradeHistory().size());
        assertEquals("AAPL", userRegistry.getUser("alice").getPortfolio().getTradeHistory().get(0).getTicker());
        assertEquals("GOOG", userRegistry.getUser("bob").getPortfolio().getTradeHistory().get(0).getTicker());
    }

    @Test
    void notificationChannelsArePerUser() {
        // Arrange — give Alice email+sms, Bob just console
        userRegistry.setNotificationChannels("alice",   Set.of("console", "email", "sms", "dashboard"));
        userRegistry.setNotificationChannels("bob",     Set.of("console", "dashboard"));

        // Act
        User alice = userRegistry.getUser("alice");
        User bob   = userRegistry.getUser("bob");

        // Assert
        assertTrue(alice.getEnabledChannels().contains("email"));
        assertTrue(alice.getEnabledChannels().contains("sms"));
        assertFalse(bob.getEnabledChannels().contains("email"));
        assertFalse(bob.getEnabledChannels().contains("sms"));
    }

    @Test
    void unknownUserThrowsIllegalArgumentException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> userRegistry.getUser("nobody"));
    }

    @Test
    void eachUserHasSeparateDashboardMessages() {
        // Arrange
        userRegistry.placeOrder("alice", "market", "AAPL", OrderType.BUY, 1, 0.0, prices);
        userRegistry.placeOrder("bob",   "market", "GOOG", OrderType.BUY, 1, 0.0, prices);

        // Act & Assert — each user only sees their own notifications
        assertEquals(1, userRegistry.getUser("alice").getDashboardMessages().size());
        assertEquals(1, userRegistry.getUser("bob").getDashboardMessages().size());
        assertEquals(0, userRegistry.getUser("charlie").getDashboardMessages().size());

        assertTrue(userRegistry.getUser("alice").getDashboardMessages().get(0).contains("AAPL"));
        assertTrue(userRegistry.getUser("bob").getDashboardMessages().get(0).contains("GOOG"));
    }
}
