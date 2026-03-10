package com.tradesimulator.market;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Concrete Strategy: random-walk pricing.
 * Each tick, every price changes by a random percentage in the range [-MAX_CHANGE, +MAX_CHANGE].
 *
 * Random is injected so tests can supply a seeded instance for deterministic behaviour.
 */
public class RandomWalkStrategy implements PriceUpdateStrategy {

    private static final double MAX_CHANGE = 0.02;

    private final Random random;

    public RandomWalkStrategy(Random random) {
        this.random = random;
    }

    @Override
    public Map<String, Double> updatePrices(Map<String, Double> currentPrices) {
        Map<String, Double> updated = new HashMap<>();
        for (Map.Entry<String, Double> entry : currentPrices.entrySet()) {
            double factor = 1.0 + (random.nextDouble() * 2 * MAX_CHANGE - MAX_CHANGE);
            double newPrice = Math.max(0.01, entry.getValue() * factor);
            updated.put(entry.getKey(), Math.round(newPrice * 100.0) / 100.0);
        }
        return updated;
    }
}
