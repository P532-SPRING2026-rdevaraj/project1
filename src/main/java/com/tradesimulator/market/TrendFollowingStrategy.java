package com.tradesimulator.market;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Concrete Strategy: trend-following pricing.
 * Each price continues in the direction of its recent momentum.
 * Recent gains make further gains more likely; recent losses deepen.
 *
 * Random is injected so tests can supply a seeded instance for deterministic behaviour.
 */
public class TrendFollowingStrategy implements PriceUpdateStrategy {

    private static final double MOMENTUM_FACTOR = 0.50;  // amplify 50% of last tick's move
    private static final double NOISE           = 0.01;  // ±1% random noise
    private static final double MAX_CHANGE      = 0.04;  // cap at ±4%

    private final Random random;
    private final Map<String, Double> previousPrices = new HashMap<>();

    public TrendFollowingStrategy(Random random) {
        this.random = random;
    }

    @Override
    public Map<String, Double> updatePrices(Map<String, Double> currentPrices) {
        Map<String, Double> updated = new HashMap<>();
        for (Map.Entry<String, Double> entry : currentPrices.entrySet()) {
            String ticker = entry.getKey();
            double price  = entry.getValue();

            double momentum = 0.0;
            if (previousPrices.containsKey(ticker)) {
                double prev = previousPrices.get(ticker);
                momentum = (price - prev) / prev;  // fractional move since last tick
            }

            double trend  = momentum * MOMENTUM_FACTOR;
            double noise  = (random.nextDouble() * 2 - 1) * NOISE;
            double change = Math.max(-MAX_CHANGE, Math.min(MAX_CHANGE, trend + noise));

            previousPrices.put(ticker, price);
            double newPrice = Math.max(1.0, price * (1 + change));
            updated.put(ticker, Math.round(newPrice * 100.0) / 100.0);
        }
        return updated;
    }

    @Override
    public String getName() {
        return "trend-following";
    }
}
