package com.tradesimulator;

import com.tradesimulator.market.MeanReversionStrategy;
import com.tradesimulator.market.RandomWalkStrategy;
import com.tradesimulator.market.TrendFollowingStrategy;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the Strategy pattern — all three PriceUpdateStrategy implementations.
 * Uses a seeded Random to make tests deterministic.
 */
class PricingStrategyTest {

    private static final double MAX_CHANGE = 0.02;

    // ── RandomWalkStrategy ────────────────────────────────────────────────────

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
        assertNotEquals(updated.get("AAPL"), updated.get("GOOG"));
    }

    @Test
    void randomWalkReturnsCorrectStrategyName() {
        // Arrange
        RandomWalkStrategy strategy = new RandomWalkStrategy(new Random());
        // Act & Assert
        assertEquals("random-walk", strategy.getName());
    }

    // ── MeanReversionStrategy ─────────────────────────────────────────────────

    @Test
    void meanReversionPullsPriceTowardMeanAfterLargeDeviation() {
        // Arrange — start well above mean to force a downward pull
        Random seeded = new Random(0L);
        MeanReversionStrategy strategy = new MeanReversionStrategy(seeded);

        // Seed history by running 10 ticks at a base price
        Map<String, Double> prices = new HashMap<>();
        prices.put("AAPL", 100.0);
        for (int i = 0; i < 10; i++) {
            prices = strategy.updatePrices(prices);  // build up 10-tick history near 100
        }

        // Act — inject a price far above the established mean
        prices.put("AAPL", 150.0);
        double sumDelta = 0;
        for (int i = 0; i < 20; i++) {
            double before = prices.get("AAPL");
            prices = strategy.updatePrices(prices);
            double after = prices.get("AAPL");
            sumDelta += (after - before);
        }

        // Assert — on average the price should be drifting downward (toward mean)
        assertTrue(sumDelta < 0,
                "Mean-reversion should pull price down when above mean, net delta=" + sumDelta);
    }

    @Test
    void meanReversionNeverProducesNegativePrice() {
        // Arrange
        Random seeded = new Random(123L);
        MeanReversionStrategy strategy = new MeanReversionStrategy(seeded);
        Map<String, Double> prices = new HashMap<>();
        prices.put("AAPL", 1.0);

        // Act & Assert
        for (int i = 0; i < 500; i++) {
            prices = strategy.updatePrices(prices);
            assertTrue(prices.get("AAPL") > 0, "Price must stay positive");
        }
    }

    @Test
    void meanReversionReturnsCorrectStrategyName() {
        // Arrange
        MeanReversionStrategy strategy = new MeanReversionStrategy(new Random());
        // Act & Assert
        assertEquals("mean-reversion", strategy.getName());
    }

    // ── TrendFollowingStrategy ────────────────────────────────────────────────

    @Test
    void trendFollowingAmplifiesUpwardMomentum() {
        // Arrange — simulate an upward trend by repeatedly raising price
        Random zeroNoise = new Random(0L) {
            @Override public double nextDouble() { return 0.5; } // returns 0.0 noise (0.5*2-1=0)
        };
        TrendFollowingStrategy strategy = new TrendFollowingStrategy(zeroNoise);

        Map<String, Double> prices = new HashMap<>();
        prices.put("AAPL", 100.0);

        // Act — first tick: no prior, just noise (≈ 0 with our seed)
        prices = strategy.updatePrices(prices);         // establishes previousPrice = 100
        prices.put("AAPL", 105.0);                      // inject an upward move of +5%
        Map<String, Double> result = strategy.updatePrices(prices);

        // Assert — with 5% momentum × 0.5 factor, trend component is +2.5% → price should rise
        assertTrue(result.get("AAPL") > 105.0,
                "Trend-following should continue upward move, got: " + result.get("AAPL"));
    }

    @Test
    void trendFollowingNeverProducesNegativePrice() {
        // Arrange
        Random seeded = new Random(77L);
        TrendFollowingStrategy strategy = new TrendFollowingStrategy(seeded);
        Map<String, Double> prices = new HashMap<>();
        prices.put("TSLA", 1.0);

        // Act & Assert
        for (int i = 0; i < 500; i++) {
            prices = strategy.updatePrices(prices);
            assertTrue(prices.get("TSLA") > 0, "Price must stay positive");
        }
    }

    @Test
    void trendFollowingReturnsCorrectStrategyName() {
        // Arrange
        TrendFollowingStrategy strategy = new TrendFollowingStrategy(new Random());
        // Act & Assert
        assertEquals("trend-following", strategy.getName());
    }
}
