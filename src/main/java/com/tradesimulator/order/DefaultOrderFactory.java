package com.tradesimulator.order;

import com.tradesimulator.model.LimitOrder;
import com.tradesimulator.model.MarketOrder;
import com.tradesimulator.model.Order;
import com.tradesimulator.model.OrderType;
import org.springframework.stereotype.Component;

/**
 * Default implementation of OrderFactory.
 * Swap this bean for a different implementation without changing any caller.
 */
@Component
public class DefaultOrderFactory implements OrderFactory {

    @Override
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
