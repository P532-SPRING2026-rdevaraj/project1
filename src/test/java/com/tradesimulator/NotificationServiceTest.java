package com.tradesimulator;

import com.tradesimulator.notification.ConsoleNotificationService;
import com.tradesimulator.notification.DashboardNotificationDecorator;
import com.tradesimulator.notification.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for the Decorator pattern — NotificationService chain.
 * Uses a mock to verify delegation and a DashboardNotificationDecorator
 * to verify the in-memory capture behaviour.
 */
@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationService mockNotificationService;

    private DashboardNotificationDecorator dashboardDecorator;

    @BeforeEach
    void setUp() {
        // Arrange — wrap a real console service with the dashboard decorator
        dashboardDecorator = new DashboardNotificationDecorator(new ConsoleNotificationService());
    }

    @Test
    void dashboardDecoratorCapturesNotificationMessage() {
        // Arrange
        String message = "Order EXECUTED: BUY 5 shares of AAPL @ $175.00";

        // Act
        dashboardDecorator.notify(message);

        // Assert
        List<String> captured = dashboardDecorator.getDashboardMessages();
        assertEquals(1, captured.size());
        assertEquals(message, captured.get(0));
    }

    @Test
    void dashboardDecoratorDelegatesToWrappedService() {
        // Arrange — wrap the mock so we can verify delegation
        DashboardNotificationDecorator decoratorWithMock =
                new DashboardNotificationDecorator(mockNotificationService);
        String message = "Order EXECUTED: SELL 2 shares of TSLA @ $250.00";

        // Act
        decoratorWithMock.notify(message);

        // Assert — wrapped service was called exactly once with the same message
        verify(mockNotificationService, times(1)).notify(message);
    }

    @Test
    void multipleNotificationsAreAllCaptured() {
        // Arrange
        String msg1 = "Notification 1";
        String msg2 = "Notification 2";
        String msg3 = "Notification 3";

        // Act
        dashboardDecorator.notify(msg1);
        dashboardDecorator.notify(msg2);
        dashboardDecorator.notify(msg3);

        // Assert
        List<String> captured = dashboardDecorator.getDashboardMessages();
        assertEquals(3, captured.size());
        assertTrue(captured.contains(msg1));
        assertTrue(captured.contains(msg2));
        assertTrue(captured.contains(msg3));
    }

    @Test
    void wrappedServiceReceivesCorrectMessageWhenOrderFails() {
        // Arrange
        DashboardNotificationDecorator decoratorWithMock =
                new DashboardNotificationDecorator(mockNotificationService);
        String rejectionMsg = "Order REJECTED: BUY 100 shares of GOOG — insufficient funds";

        // Act
        decoratorWithMock.notify(rejectionMsg);

        // Assert
        verify(mockNotificationService).notify(rejectionMsg);
    }
}
