package com.tradesimulator.order;

import com.tradesimulator.model.Order;
import com.tradesimulator.model.OrderType;

/**
 * Factory pattern: single creation point for all Order subtypes.
 * Adding a new order type only requires a new implementation — no callers change.
 */
public interface OrderFactory {
    Order createOrder(String kind, String ticker, OrderType orderType,
                      int quantity, double limitPrice);
}
