package com.tradesimulator.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Decorator that sends a mock email notification in addition to delegating
 * to the wrapped NotificationService.
 *
 * "Mock" means the email is logged to the console rather than delivered via SMTP.
 * Swap for a real EmailSender without touching any other class.
 */
public class EmailNotificationDecorator extends NotificationDecorator {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificationDecorator.class);

    private final String recipientAddress;

    public EmailNotificationDecorator(NotificationService wrapped, String recipientAddress) {
        super(wrapped);
        this.recipientAddress = recipientAddress;
    }

    @Override
    public void notify(String message) {
        super.notify(message);
        log.info("[EMAIL] To: {} | Subject: Trade Notification | Body: {}", recipientAddress, message);
    }
}
