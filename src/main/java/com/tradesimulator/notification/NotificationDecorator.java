package com.tradesimulator.notification;

/**
 * Abstract Decorator base for NotificationService.
 * Subclasses call super.notify() to delegate to the wrapped service,
 * then add their own behaviour (SMS, dashboard, email, etc.).
 *
 * Future change protected: stacking multiple channels requires no modification to
 * ConsoleNotificationService or any existing decorator.
 */
public abstract class NotificationDecorator implements NotificationService {

    protected final NotificationService wrapped;

    protected NotificationDecorator(NotificationService wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public void notify(String message) {
        wrapped.notify(message);
    }
}
