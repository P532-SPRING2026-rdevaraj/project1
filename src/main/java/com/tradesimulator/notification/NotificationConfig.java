package com.tradesimulator.notification;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Spring configuration that wires the notification chain:
 * ConsoleNotificationService -> DashboardNotificationDecorator
 *
 * To add SMS in Week 2, wrap dashboardDecorator with SmsNotificationDecorator here —
 * no other class needs to change.
 */
@Configuration
public class NotificationConfig {

    @Bean
    @Primary
    DashboardNotificationDecorator notificationService(
            ConsoleNotificationService consoleNotificationService) {
        return new DashboardNotificationDecorator(consoleNotificationService);
    }
}
