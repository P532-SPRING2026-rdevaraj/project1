package com.tradesimulator.notification;

/**
 * Clean notification interface (Decorator pattern root + Week 2 extension point).
 * Implementations and decorators stack additional channels (email, SMS, dashboard)
 * without modifying existing notifiers.
 */
public interface NotificationService {
    void notify(String message);
}
