package com.tradesimulator.market;

import java.util.Map;

/**
 * Strategy pattern interface for price update algorithms.
 * Swap implementations (e.g. RandomWalk -> MeanReversion) without touching MarketFeed.
 */
public interface PriceUpdateStrategy {
    /**
     * Given the current price map, return an updated price map.
     *
     * @param currentPrices snapshot of current ticker -> price
     * @return new ticker -> price map with updated values
     */
    Map<String, Double> updatePrices(Map<String, Double> currentPrices);
}
