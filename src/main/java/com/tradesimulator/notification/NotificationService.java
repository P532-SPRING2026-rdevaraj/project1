package com.tradesimulator.notification;

/**
 * Notification interface (Decorator pattern root).
 * Implementations and decorators stack additional channels (email, SMS, dashboard)
 * without modifying existing notifiers.
 */
public interface NotificationService {
    void notify(String message);
}
