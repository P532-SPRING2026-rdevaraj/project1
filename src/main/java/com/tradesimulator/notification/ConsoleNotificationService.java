package com.tradesimulator.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Base (concrete) implementation of NotificationService.
 * Logs notifications to the console — acceptable for Week 1.
 * Can be wrapped by decorators (e.g. SmsNotificationDecorator) without modification.
 */
@Service
public class ConsoleNotificationService implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(ConsoleNotificationService.class);

    @Override
    public void notify(String message) {
        log.info("[NOTIFICATION] {}", message);
    }
}
