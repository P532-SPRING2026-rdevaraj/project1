package com.tradesimulator.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Decorator that sends a mock SMS notification in addition to delegating
 * to the wrapped NotificationService.
 *
 * "Mock" means the SMS is logged to the console rather than sent via a carrier gateway.
 * Swap for a real SmsGateway without touching any other class.
 */
public class SmsNotificationDecorator extends NotificationDecorator {

    private static final Logger log = LoggerFactory.getLogger(SmsNotificationDecorator.class);

    private final String phoneNumber;

    public SmsNotificationDecorator(NotificationService wrapped, String phoneNumber) {
        super(wrapped);
        this.phoneNumber = phoneNumber;
    }

    @Override
    public void notify(String message) {
        super.notify(message);
        // Truncate to 160 chars — typical SMS limit
        String sms = message.length() > 160 ? message.substring(0, 157) + "..." : message;
        log.info("[SMS] To: {} | {}", phoneNumber, sms);
    }
}
