package com.tradesimulator.controller;

import com.tradesimulator.market.MarketFeed;
import com.tradesimulator.market.MeanReversionStrategy;
import com.tradesimulator.market.RandomWalkStrategy;
import com.tradesimulator.market.TrendFollowingStrategy;
import com.tradesimulator.model.Order;
import com.tradesimulator.model.OrderType;
import com.tradesimulator.model.Trade;
import com.tradesimulator.model.User;
import com.tradesimulator.order.OrderRequest;
import com.tradesimulator.portfolio.UserRegistry;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * REST controller for the TradeSimulator API.
 *
 * All trading endpoints accept a ?userId= query parameter (default: "alice")
 * so each analyst sees their own portfolio, orders, trades, and notifications.
 *
 * Strategy switching (Change 1) is handled via GET/POST /api/strategy.
 * Notification channel configuration (Change 2) is via POST /api/notifications/channels.
 * User listing (Change 3) is via GET /api/users.
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class TradeController {

    private static final String DEFAULT_USER = "alice";

    private final MarketFeed   marketFeed;
    private final UserRegistry userRegistry;
    private final Random       random;

    public TradeController(MarketFeed marketFeed,
                           UserRegistry userRegistry,
                           Random random) {
        this.marketFeed   = marketFeed;
        this.userRegistry = userRegistry;
        this.random       = random;
    }

    // ── Market prices ────────────────────────────────────────────────────────

    @GetMapping("/prices")
    public Map<String, Double> getPrices() {
        return marketFeed.getPrices();
    }

    // ── Pricing strategy (Change 1) ──────────────────────────────────────────

    @GetMapping("/strategy")
    public Map<String, String> getStrategy() {
        return Map.of("strategy", marketFeed.getStrategy().getName());
    }

    @PostMapping("/strategy")
    public Map<String, String> setStrategy(@RequestParam String name) {
        switch (name) {
            case "random-walk"    -> marketFeed.setStrategy(new RandomWalkStrategy(random));
            case "mean-reversion" -> marketFeed.setStrategy(new MeanReversionStrategy(random));
            case "trend-following"-> marketFeed.setStrategy(new TrendFollowingStrategy(random));
            default -> throw new IllegalArgumentException("Unknown strategy: " + name);
        }
        return Map.of("strategy", marketFeed.getStrategy().getName());
    }

    // ── Users (Change 3) ─────────────────────────────────────────────────────

    @GetMapping("/users")
    public List<Map<String, Object>> getUsers() {
        return userRegistry.getAllUsers().stream()
                .map(u -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id",   u.getId());
                    m.put("name", u.getName());
                    return m;
                })
                .collect(Collectors.toList());
    }

    // ── Portfolio ────────────────────────────────────────────────────────────

    @GetMapping("/portfolio")
    public Map<String, Object> getPortfolio(@RequestParam(defaultValue = DEFAULT_USER) String userId) {
        User user = userRegistry.getUser(userId);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("cash",       user.getPortfolio().getCash());
        result.put("holdings",   user.getPortfolio().getHoldings());
        result.put("totalValue", user.getPortfolio().getTotalValue(marketFeed.getPrices()));
        return result;
    }

    // ── Trade history ────────────────────────────────────────────────────────

    @GetMapping("/trades")
    public List<Trade> getTrades(@RequestParam(defaultValue = DEFAULT_USER) String userId) {
        return userRegistry.getUser(userId).getPortfolio().getTradeHistory();
    }

    // ── Orders ───────────────────────────────────────────────────────────────

    @GetMapping("/orders/pending")
    public List<Order> getPendingOrders(@RequestParam(defaultValue = DEFAULT_USER) String userId) {
        return userRegistry.getUser(userId).getPendingOrders();
    }

    @PostMapping("/orders")
    public ResponseEntity<?> placeOrder(
            @RequestParam(defaultValue = DEFAULT_USER) String userId,
            @RequestBody OrderRequest request) {
        try {
            OrderType orderType = OrderType.valueOf(request.getOrderType().toUpperCase());
            Order order = userRegistry.placeOrder(
                    userId,
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

    // ── Notifications (Change 2 + 3) ─────────────────────────────────────────

    @GetMapping("/notifications")
    public List<String> getNotifications(@RequestParam(defaultValue = DEFAULT_USER) String userId) {
        return userRegistry.getUser(userId).getDashboardMessages();
    }

    @GetMapping("/notifications/channels")
    public Map<String, Object> getNotificationChannels(
            @RequestParam(defaultValue = DEFAULT_USER) String userId) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("userId",   userId);
        result.put("channels", userRegistry.getUser(userId).getEnabledChannels());
        return result;
    }

    @PostMapping("/notifications/channels")
    public Map<String, Object> setNotificationChannels(
            @RequestParam(defaultValue = DEFAULT_USER) String userId,
            @RequestBody Set<String> channels) {
        userRegistry.setNotificationChannels(userId, channels);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("userId",   userId);
        result.put("channels", userRegistry.getUser(userId).getEnabledChannels());
        return result;
    }
}
