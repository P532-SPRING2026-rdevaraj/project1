package com.tradesimulator.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Decorator that sends a mock email notification (logged to console)
 * in addition to delegating to the wrapped NotificationService.
 */
public class EmailNotificationDecorator extends NotificationDecorator {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificationDecorator.class);

    private final String recipientAddress;
    private final DashboardNotificationDecorator dashboard;

    public EmailNotificationDecorator(NotificationService wrapped, String recipientAddress) {
        this(wrapped, recipientAddress, null);
    }

    public EmailNotificationDecorator(NotificationService wrapped, String recipientAddress,
                                      DashboardNotificationDecorator dashboard) {
        super(wrapped);
        this.recipientAddress = recipientAddress;
        this.dashboard = dashboard;
    }

    @Override
    public void notify(String message) {
        super.notify(message);
        String formatted = "[EMAIL] To: " + recipientAddress + " | Subject: Trade Notification | Body: " + message;
        log.info(formatted);
        if (dashboard != null) dashboard.addMessage(formatted);
    }
}
