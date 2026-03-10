package com.tradesimulator.portfolio;

import com.tradesimulator.model.Trade;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Singleton (Spring @Service = application-scoped singleton) that tracks the user's
 * cash balance, stock holdings, and trade history.
 *
 * All mutation is synchronized to be safe under concurrent price-tick threads.
 * Future change protected: a second user account could be added by making Portfolio
 * a prototype-scoped bean without altering any business logic here.
 */
@Service
public class Portfolio {

    private static final double INITIAL_CASH = 10_000.00;

    private double cash = INITIAL_CASH;
    private final Map<String, Integer> holdings = new HashMap<>();
    private final List<Trade> tradeHistory = new ArrayList<>();

    public synchronized boolean buy(String ticker, int quantity, double price) {
        double cost = Math.round(price * quantity * 100.0) / 100.0;
        if (cost > cash) {
            return false;
        }
        cash = Math.round((cash - cost) * 100.0) / 100.0;
        holdings.merge(ticker, quantity, (existing, added) -> existing + added);
        return true;
    }

    public synchronized boolean sell(String ticker, int quantity, double price) {
        int owned = holdings.getOrDefault(ticker, 0);
        if (owned < quantity) {
            return false;
        }
        double proceeds = Math.round(price * quantity * 100.0) / 100.0;
        cash = Math.round((cash + proceeds) * 100.0) / 100.0;
        int remaining = owned - quantity;
        if (remaining == 0) {
            holdings.remove(ticker);
        } else {
            holdings.put(ticker, remaining);
        }
        return true;
    }

    public synchronized void recordTrade(Trade trade) {
        tradeHistory.add(trade);
    }

    public double getCash() { return cash; }

    public Map<String, Integer> getHoldings() {
        return Collections.unmodifiableMap(holdings);
    }

    public List<Trade> getTradeHistory() {
        return Collections.unmodifiableList(tradeHistory);
    }

    public double getTotalValue(Map<String, Double> prices) {
        double stockValue = holdings.entrySet().stream()
                .mapToDouble(e -> e.getValue() * prices.getOrDefault(e.getKey(), 0.0))
                .sum();
        return Math.round((cash + stockValue) * 100.0) / 100.0;
    }
}
