package com.tradesimulator.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Decorator that sends a mock SMS notification (logged to console)
 * in addition to delegating to the wrapped NotificationService.
 */
public class SmsNotificationDecorator extends NotificationDecorator {

    private static final Logger log = LoggerFactory.getLogger(SmsNotificationDecorator.class);

    private final String phoneNumber;
    private final DashboardNotificationDecorator dashboard;

    public SmsNotificationDecorator(NotificationService wrapped, String phoneNumber) {
        this(wrapped, phoneNumber, null);
    }

    public SmsNotificationDecorator(NotificationService wrapped, String phoneNumber,
                                    DashboardNotificationDecorator dashboard) {
        super(wrapped);
        this.phoneNumber = phoneNumber;
        this.dashboard = dashboard;
    }

    @Override
    public void notify(String message) {
        super.notify(message);
        String sms = message.length() > 160 ? message.substring(0, 157) + "..." : message;
        String formatted = "[SMS] To: " + phoneNumber + " | " + sms;
        log.info(formatted);
        if (dashboard != null) dashboard.addMessage(formatted);
    }
}
