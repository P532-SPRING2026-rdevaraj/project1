package com.tradesimulator.market;

import java.util.Map;

/**
 * Observer pattern interface.
 * Any component that needs to react to price changes implements this interface
 * and registers itself with MarketFeed.
 *
 * Future change protected: new consumers (e.g. risk engine, analytics service)
 * can be added without modifying MarketFeed.
 */
public interface PriceObserver {
    void onPriceUpdate(Map<String, Double> prices);
}
