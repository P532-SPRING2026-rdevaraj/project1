package com.tradesimulator.order;

import com.tradesimulator.model.LimitOrder;
import com.tradesimulator.model.MarketOrder;
import com.tradesimulator.model.Order;
import com.tradesimulator.model.OrderType;
import org.springframework.stereotype.Component;

/**
 * Factory pattern: single creation point for all Order subtypes.
 * Adding a new order type (e.g. StopLossOrder) only requires adding a new case here,
 * not changing any caller.
 */
@Component
public class OrderFactory {

    /**
     * @param kind       "MARKET" or "LIMIT" (case-insensitive)
     * @param ticker     stock symbol
     * @param orderType  BUY or SELL
     * @param quantity   number of shares
     * @param limitPrice threshold price (ignored for MARKET orders)
     * @throws IllegalArgumentException for unknown order kinds
     */
    public Order createOrder(String kind, String ticker, OrderType orderType,
                             int quantity, double limitPrice) {
        return switch (kind.toUpperCase()) {
            case "MARKET" -> new MarketOrder(ticker, orderType, quantity);
            case "LIMIT"  -> new LimitOrder(ticker, orderType, quantity, limitPrice);
            default -> throw new IllegalArgumentException(
                    "Unknown order kind: '" + kind + "'. Expected MARKET or LIMIT.");
        };
    }
}
