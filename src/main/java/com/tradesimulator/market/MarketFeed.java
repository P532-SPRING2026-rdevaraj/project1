package com.tradesimulator.market;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Singleton (Spring @Service = singleton scope) that acts as the Observable in the Observer pattern.
 * Holds current stock prices and notifies registered PriceObservers every 5 seconds.
 *
 * The pricing algorithm is pluggable via PriceUpdateStrategy (Strategy pattern).
 * Swap strategies at runtime without modifying this class.
 */
@Service
public class MarketFeed {

    private final Map<String, Double> prices = new LinkedHashMap<>();
    private final List<PriceObserver> observers = new ArrayList<>();
    private PriceUpdateStrategy strategy;

    public MarketFeed(PriceUpdateStrategy strategy) {
        prices.put("AAPL",  175.50);
        prices.put("GOOG", 2850.00);
        prices.put("TSLA",  245.30);
        prices.put("AMZN", 3400.00);
        prices.put("MSFT",  380.75);
        prices.put("NVDA",  495.20);
        prices.put("META",  350.10);

        this.strategy = strategy;
    }

    public void setStrategy(PriceUpdateStrategy strategy) {
        this.strategy = strategy;
    }

    public PriceUpdateStrategy getStrategy() {
        return strategy;
    }

    public void addObserver(PriceObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(PriceObserver observer) {
        observers.remove(observer);
    }

    @Scheduled(fixedDelay = 5000)
    public void tick() {
        Map<String, Double> updated = strategy.updatePrices(prices);
        prices.putAll(updated);
        notifyObservers();
    }

    private void notifyObservers() {
        Map<String, Double> snapshot = Collections.unmodifiableMap(new HashMap<>(prices));
        for (PriceObserver observer : observers) {
            observer.onPriceUpdate(snapshot);
        }
    }

    public Map<String, Double> getPrices() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(prices));
    }

    public Double getPrice(String ticker) {
        return prices.get(ticker);
    }
}
