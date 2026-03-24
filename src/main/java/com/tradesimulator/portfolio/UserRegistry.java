package com.tradesimulator.portfolio;

import com.tradesimulator.market.MarketFeed;
import com.tradesimulator.market.PriceObserver;
import com.tradesimulator.model.Order;
import com.tradesimulator.model.OrderStatus;
import com.tradesimulator.model.OrderType;
import com.tradesimulator.model.Trade;
import com.tradesimulator.model.User;
import com.tradesimulator.notification.ConsoleNotificationService;
import com.tradesimulator.order.OrderFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Singleton (Spring @Service) managing all analyst users.
 * Implements PriceObserver to evaluate each user's pending limit orders on every price tick.
 */
@Service
public class UserRegistry implements PriceObserver {

    private final Map<String, User> users = new LinkedHashMap<>();
    private final OrderFactory orderFactory;
    private final ConsoleNotificationService consoleService;

    public UserRegistry(OrderFactory orderFactory,
                        ConsoleNotificationService consoleService,
                        MarketFeed marketFeed) {
        this.orderFactory   = orderFactory;
        this.consoleService = consoleService;

        users.put("alice",   new User("alice",   "Alice",   consoleService));
        users.put("bob",     new User("bob",     "Bob",     consoleService));
        users.put("charlie", new User("charlie", "Charlie", consoleService));

        marketFeed.addObserver(this);
    }

    @Override
    public void onPriceUpdate(Map<String, Double> prices) {
        for (User user : users.values()) {
            List<Order> toExecute = new ArrayList<>();
            for (Order order : user.getPendingOrders()) {
                Double price = prices.get(order.getTicker());
                if (price != null && order.canExecute(price)) {
                    toExecute.add(order);
                }
            }
            for (Order order : toExecute) {
                user.getPendingOrders().remove(order);
                executeOrderForUser(user, order, prices.get(order.getTicker()));
            }
        }
    }

    public Order placeOrder(String userId, String kind, String ticker,
                            OrderType orderType, int quantity, double limitPrice,
                            Map<String, Double> currentPrices) {
        User user  = getUser(userId);
        Order order = orderFactory.createOrder(kind, ticker, orderType, quantity, limitPrice);
        double price = currentPrices.getOrDefault(ticker, 0.0);

        if (order.canExecute(price)) {
            executeOrderForUser(user, order, price);
        } else {
            user.getPendingOrders().add(order);
        }
        return order;
    }

    public void setNotificationChannels(String userId, Set<String> channels) {
        getUser(userId).rebuildNotificationChain(consoleService, channels);
    }

    public User getUser(String userId) {
        User u = users.get(userId);
        if (u == null) throw new IllegalArgumentException("Unknown user: " + userId);
        return u;
    }

    public Collection<User> getAllUsers() {
        return users.values();
    }

    private void executeOrderForUser(User user, Order order, double price) {
        Portfolio portfolio = user.getPortfolio();
        boolean success;
        if (order.getOrderType() == OrderType.BUY) {
            success = portfolio.buy(order.getTicker(), order.getQuantity(), price);
        } else {
            success = portfolio.sell(order.getTicker(), order.getQuantity(), price);
        }

        if (success) {
            order.setStatus(OrderStatus.EXECUTED);
            portfolio.recordTrade(new Trade(order.getTicker(), order.getOrderType(),
                                            order.getQuantity(), price));
            user.getNotificationService().notify(String.format(
                    "Order EXECUTED: %s %d shares of %s @ $%.2f (total $%.2f)",
                    order.getOrderType(), order.getQuantity(),
                    order.getTicker(), price, price * order.getQuantity()));
        } else {
            order.setStatus(OrderStatus.CANCELLED);
            user.getNotificationService().notify(String.format(
                    "Order REJECTED: %s %d shares of %s — insufficient %s",
                    order.getOrderType(), order.getQuantity(), order.getTicker(),
                    order.getOrderType() == OrderType.BUY ? "funds" : "shares"));
        }
    }
}
