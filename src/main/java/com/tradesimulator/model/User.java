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
 */
public class User {

    private final String id;
    private final String name;
    private final Portfolio portfolio;
    private final List<Order> pendingOrders;

    private DashboardNotificationDecorator dashboard;
    private NotificationService outerChain;
    private Set<String> enabledChannels;

    public User(String id, String name, ConsoleNotificationService console) {
        this.id              = id;
        this.name            = name;
        this.portfolio       = new Portfolio();
        this.pendingOrders   = new CopyOnWriteArrayList<>();
        this.enabledChannels = Set.of("console", "dashboard");
        this.dashboard       = new DashboardNotificationDecorator(console);
        this.outerChain      = this.dashboard;
    }

    /**
     * Rebuilds the notification decorator chain to match the requested channels.
     * Chain order (inner to outer): Console → Dashboard [→ Email] [→ SMS]
     * Email/SMS also push their formatted mock messages directly to the dashboard list.
     */
    public void rebuildNotificationChain(ConsoleNotificationService console, Set<String> channels) {
        this.enabledChannels = channels;
        DashboardNotificationDecorator newDashboard = new DashboardNotificationDecorator(console);
        NotificationService chain = newDashboard;
        if (channels.contains("email")) {
            chain = new EmailNotificationDecorator(chain, id + "@tradesim.io", newDashboard);
        }
        if (channels.contains("sms")) {
            chain = new SmsNotificationDecorator(chain, "+1-555-" + Math.abs(id.hashCode() % 10000), newDashboard);
        }
        this.dashboard  = newDashboard;
        this.outerChain = chain;
    }

    public String getId()                               { return id; }
    public String getName()                             { return name; }
    public Portfolio getPortfolio()                     { return portfolio; }
    public List<Order> getPendingOrders()               { return pendingOrders; }
    public NotificationService getNotificationService() { return outerChain; }
    public List<String> getDashboardMessages()          { return dashboard.getDashboardMessages(); }
    public Set<String> getEnabledChannels()             { return enabledChannels; }
}
