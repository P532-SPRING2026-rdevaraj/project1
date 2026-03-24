package com.tradesimulator.market;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Concrete Strategy: mean-reversion pricing.
 * Each price drifts back toward a moving average of its recent values.
 * Stocks that have moved far from their mean experience a stronger pull.
 *
 * Random is injected so tests can supply a seeded instance for deterministic behaviour.
 */
public class MeanReversionStrategy implements PriceUpdateStrategy {

    private static final int   WINDOW      = 10;
    private static final double REVERSION  = 0.30;  // 30% of deviation pulled back per tick
    private static final double NOISE      = 0.005; // ±0.5% random noise
    private static final double MAX_CHANGE = 0.03;  // cap at ±3%

    private final Random random;
    private final Map<String, List<Double>> history = new HashMap<>();

    public MeanReversionStrategy(Random random) {
        this.random = random;
    }

    @Override
    public Map<String, Double> updatePrices(Map<String, Double> currentPrices) {
        Map<String, Double> updated = new HashMap<>();
        for (Map.Entry<String, Double> entry : currentPrices.entrySet()) {
            String ticker = entry.getKey();
            double price  = entry.getValue();

            List<Double> hist = history.computeIfAbsent(ticker, k -> new ArrayList<>());
            hist.add(price);
            if (hist.size() > WINDOW) hist.remove(0);

            double mean      = hist.stream().mapToDouble(Double::doubleValue).average().orElse(price);
            double deviation = (price - mean) / mean;           // positive = above mean
            double pull      = -deviation * REVERSION;          // pull toward mean
            double noise     = (random.nextDouble() * 2 - 1) * NOISE;
            double change    = Math.max(-MAX_CHANGE, Math.min(MAX_CHANGE, pull + noise));

            double newPrice = Math.max(1.0, price * (1 + change));
            updated.put(ticker, Math.round(newPrice * 100.0) / 100.0);
        }
        return updated;
    }

    @Override
    public String getName() {
        return "mean-reversion";
    }
}
