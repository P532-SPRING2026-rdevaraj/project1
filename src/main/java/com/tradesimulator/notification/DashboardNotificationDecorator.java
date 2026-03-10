package com.tradesimulator.notification;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Decorator that captures notifications in an in-memory list so the UI can
 * display them in a notification panel, in addition to delegating to the wrapped notifier.
 *
 * In Week 2 an SmsNotificationDecorator or EmailNotificationDecorator can be stacked
 * on top without touching this class.
 */
public class DashboardNotificationDecorator extends NotificationDecorator {

    private final List<String> dashboardMessages = new ArrayList<>();

    public DashboardNotificationDecorator(NotificationService wrapped) {
        super(wrapped);
    }

    @Override
    public void notify(String message) {
        super.notify(message);
        dashboardMessages.add(message);
    }

    public List<String> getDashboardMessages() {
        return Collections.unmodifiableList(dashboardMessages);
    }

    public void clearMessages() {
        dashboardMessages.clear();
    }
}
