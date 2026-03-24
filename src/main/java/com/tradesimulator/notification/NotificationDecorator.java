package com.tradesimulator.notification;

/**
 * Abstract Decorator base for NotificationService.
 * Subclasses call super.notify() to delegate to the wrapped service,
 * then add their own behaviour (email, SMS, dashboard).
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
