package com.tradesimulator.notification;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Wires the default notification chain: ConsoleNotificationService → DashboardNotificationDecorator.
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
