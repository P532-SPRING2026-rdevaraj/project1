package com.tradesimulator.controller;

import com.tradesimulator.market.MarketFeed;
import com.tradesimulator.model.Order;
import com.tradesimulator.model.OrderType;
import com.tradesimulator.model.Trade;
import com.tradesimulator.notification.DashboardNotificationDecorator;
import com.tradesimulator.order.OrderRequest;
import com.tradesimulator.order.OrderService;
import com.tradesimulator.portfolio.Portfolio;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class TradeController {

    private final MarketFeed marketFeed;
    private final Portfolio portfolio;
    private final OrderService orderService;
    private final DashboardNotificationDecorator notificationService;

    public TradeController(MarketFeed marketFeed,
                           Portfolio portfolio,
                           OrderService orderService,
                           DashboardNotificationDecorator notificationService) {
        this.marketFeed = marketFeed;
        this.portfolio = portfolio;
        this.orderService = orderService;
        this.notificationService = notificationService;
    }

    /** Current prices for all tracked stocks. */
    @GetMapping("/prices")
    public Map<String, Double> getPrices() {
        return marketFeed.getPrices();
    }

    /** Portfolio snapshot: cash, holdings, total value. */
    @GetMapping("/portfolio")
    public Map<String, Object> getPortfolio() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("cash", portfolio.getCash());
        result.put("holdings", portfolio.getHoldings());
        result.put("totalValue", portfolio.getTotalValue(marketFeed.getPrices()));
        return result;
    }

    /** Full trade history. */
    @GetMapping("/trades")
    public List<Trade> getTrades() {
        return portfolio.getTradeHistory();
    }

    /** All currently pending limit orders. */
    @GetMapping("/orders/pending")
    public List<Order> getPendingOrders() {
        return orderService.getPendingOrders();
    }

    /** Place a new market or limit order. */
    @PostMapping("/orders")
    public ResponseEntity<?> placeOrder(@RequestBody OrderRequest request) {
        try {
            OrderType orderType = OrderType.valueOf(request.getOrderType().toUpperCase());
            Order order = orderService.placeOrder(
                    request.getOrderKind(),
                    request.getTicker(),
                    orderType,
                    request.getQuantity(),
                    request.getLimitPrice(),
                    marketFeed.getPrices());
            return ResponseEntity.ok(order);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** Recent notifications for the dashboard panel. */
    @GetMapping("/notifications")
    public List<String> getNotifications() {
        return notificationService.getDashboardMessages();
    }
}
