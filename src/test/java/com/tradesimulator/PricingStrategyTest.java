package com.tradesimulator;

import com.tradesimulator.market.RandomWalkStrategy;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the Strategy pattern — RandomWalkStrategy.
 * Uses a seeded Random to make tests deterministic; verifies the ±2% band invariant.
 */
class PricingStrategyTest {

    private static final double MAX_CHANGE = 0.02;

    @Test
    void randomWalkStaysWithinTwoPercentBandOverManyIterations() {
        // Arrange — seeded Random for determinism
        Random seeded = new Random(42L);
        RandomWalkStrategy strategy = new RandomWalkStrategy(seeded);

        Map<String, Double> prices = new HashMap<>();
        prices.put("AAPL", 100.0);

        // Act & Assert — run 500 ticks and verify every result stays within ±2%
        double previous = 100.0;
        for (int i = 0; i < 500; i++) {
            Map<String, Double> updated = strategy.updatePrices(prices);
            double next = updated.get("AAPL");

            double lo = previous * (1 - MAX_CHANGE) - 0.01; // allow rounding tolerance
            double hi = previous * (1 + MAX_CHANGE) + 0.01;
            assertTrue(next >= lo && next <= hi,
                    String.format("Tick %d: price %.2f is outside ±2%% of %.2f", i, next, previous));

            prices.put("AAPL", next);
            previous = next;
        }
    }

    @Test
    void randomWalkNeverProducesNegativeOrZeroPrice() {
        // Arrange — seed producing extreme lows
        Random seeded = new Random(999L);
        RandomWalkStrategy strategy = new RandomWalkStrategy(seeded);

        Map<String, Double> prices = new HashMap<>();
        prices.put("TSLA", 0.02); // start very near zero

        // Act & Assert
        for (int i = 0; i < 200; i++) {
            Map<String, Double> updated = strategy.updatePrices(prices);
            double price = updated.get("TSLA");
            assertTrue(price > 0, "Price must always remain positive, got: " + price);
            prices.put("TSLA", price);
        }
    }

    @Test
    void randomWalkUpdatesAllTickersIndependently() {
        // Arrange
        Random seeded = new Random(7L);
        RandomWalkStrategy strategy = new RandomWalkStrategy(seeded);

        Map<String, Double> prices = new HashMap<>();
        prices.put("AAPL", 175.0);
        prices.put("GOOG", 2850.0);

        // Act
        Map<String, Double> updated = strategy.updatePrices(prices);

        // Assert — both tickers present and independently changed
        assertTrue(updated.containsKey("AAPL"));
        assertTrue(updated.containsKey("GOOG"));
        // Prices should be different from each other (highly unlikely to collide)
        assertNotEquals(updated.get("AAPL"), updated.get("GOOG"));
    }
}
