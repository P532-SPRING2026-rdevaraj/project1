package com.tradesimulator.order;

import com.tradesimulator.market.MarketFeed;
import com.tradesimulator.market.PriceObserver;
import com.tradesimulator.model.Order;
import com.tradesimulator.model.OrderStatus;
import com.tradesimulator.model.OrderType;
import com.tradesimulator.model.Trade;
import com.tradesimulator.notification.NotificationService;
import com.tradesimulator.portfolio.Portfolio;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Core trading service.
 * Implements PriceObserver so it can evaluate pending limit orders on every price tick.
 * Delegates order creation to OrderFactory, portfolio changes to Portfolio,
 * and notifications to NotificationService.
 */
@Service
public class OrderService implements PriceObserver {

    private final Portfolio portfolio;
    private final NotificationService notificationService;
    private final OrderFactory orderFactory;
    private final List<Order> pendingOrders = new CopyOnWriteArrayList<>();

    public OrderService(Portfolio portfolio,
                        NotificationService notificationService,
                        OrderFactory orderFactory,
                        MarketFeed marketFeed) {
        this.portfolio = portfolio;
        this.notificationService = notificationService;
        this.orderFactory = orderFactory;
        marketFeed.addObserver(this);
    }

    /**
     * Place a new order. Market orders execute immediately;
     * limit orders are queued and checked on every price update.
     */
    public Order placeOrder(String kind, String ticker, OrderType orderType,
                            int quantity, double limitPrice,
                            Map<String, Double> currentPrices) {
        Order order = orderFactory.createOrder(kind, ticker, orderType, quantity, limitPrice);
        double currentPrice = currentPrices.getOrDefault(ticker, 0.0);

        if (order.canExecute(currentPrice)) {
            executeOrder(order, currentPrice);
        } else {
            pendingOrders.add(order);
        }
        return order;
    }

    @Override
    public void onPriceUpdate(Map<String, Double> prices) {
        List<Order> toExecute = new ArrayList<>();
        for (Order order : pendingOrders) {
            Double price = prices.get(order.getTicker());
            if (price != null && order.canExecute(price)) {
                toExecute.add(order);
            }
        }
        for (Order order : toExecute) {
            pendingOrders.remove(order);
            executeOrder(order, prices.get(order.getTicker()));
        }
    }

    private void executeOrder(Order order, double price) {
        boolean success;
        if (order.getOrderType() == OrderType.BUY) {
            success = portfolio.buy(order.getTicker(), order.getQuantity(), price);
        } else {
            success = portfolio.sell(order.getTicker(), order.getQuantity(), price);
        }

        if (success) {
            order.setStatus(OrderStatus.EXECUTED);
            Trade trade = new Trade(order.getTicker(), order.getOrderType(), order.getQuantity(), price);
            portfolio.recordTrade(trade);
            notificationService.notify(String.format(
                    "Order EXECUTED: %s %d shares of %s @ $%.2f (total $%.2f)",
                    order.getOrderType(), order.getQuantity(),
                    order.getTicker(), price, price * order.getQuantity()));
        } else {
            order.setStatus(OrderStatus.CANCELLED);
            notificationService.notify(String.format(
                    "Order REJECTED: %s %d shares of %s — insufficient %s",
                    order.getOrderType(), order.getQuantity(), order.getTicker(),
                    order.getOrderType() == OrderType.BUY ? "funds" : "shares"));
        }
    }

    public List<Order> getPendingOrders() {
        return Collections.unmodifiableList(pendingOrders);
    }
}
