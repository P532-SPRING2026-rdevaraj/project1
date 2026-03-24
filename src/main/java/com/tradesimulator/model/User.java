package com.tradesimulator.model;

import com.tradesimulator.notification.ConsoleNotificationService;
import com.tradesimulator.notification.DashboardNotificationDecorator;
import com.tradesimulator.notification.EmailNotificationDecorator;
import com.tradesimulator.notification.NotificationService;
import com.tradesimulator.notification.SmsNotificationDecorator;
import com.tradesimulator.portfolio.Portfolio;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Represents an analyst user with their own isolated portfolio, order book,
 * notification preferences, and dashboard message feed.
 *
 * Each User owns a Portfolio instance and a pluggable NotificationService chain
 * (Decorator pattern). Calling rebuildNotificationChain() rewires the chain
 * without touching any other class.
 */
public class User {

    private final String id;
    private final String name;
    private final Portfolio portfolio;
    private final List<Order> pendingOrders;

    /** The outermost decorator — always a Dashboard so the UI can poll messages. */
    private DashboardNotificationDecorator notificationService;
    private Set<String> enabledChannels;

    public User(String id, String name, ConsoleNotificationService console) {
        this.id              = id;
        this.name            = name;
        this.portfolio       = new Portfolio();
        this.pendingOrders   = new CopyOnWriteArrayList<>();
        this.enabledChannels = Set.of("console", "dashboard");
        // Default chain: Console → Dashboard
        this.notificationService = new DashboardNotificationDecorator(console);
    }

    /**
     * Rebuilds the notification decorator chain to match the requested channels.
     * Always wraps with DashboardNotificationDecorator at the top so the UI still works.
     * Chain (inner→outer): Console [→ Email] [→ SMS] → Dashboard
     */
    public void rebuildNotificationChain(ConsoleNotificationService console, Set<String> channels) {
        this.enabledChannels = channels;
        NotificationService chain = console;
        if (channels.contains("email")) {
            chain = new EmailNotificationDecorator(chain, id + "@tradesim.io");
        }
        if (channels.contains("sms")) {
            chain = new SmsNotificationDecorator(chain, "+1-555-" + id.hashCode() % 10000);
        }
        this.notificationService = new DashboardNotificationDecorator(chain);
    }

    // --- Accessors ---

    public String getId()                              { return id; }
    public String getName()                            { return name; }
    public Portfolio getPortfolio()                    { return portfolio; }
    public List<Order> getPendingOrders()              { return pendingOrders; }
    public NotificationService getNotificationService(){ return notificationService; }
    public List<String> getDashboardMessages()         { return notificationService.getDashboardMessages(); }
    public Set<String> getEnabledChannels()            { return enabledChannels; }
}
