package com.tradesimulator.model;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Abstract base for all order types.
 * The Factory pattern creates concrete subclasses; subclasses define canExecute() logic.
 */
public abstract class Order {

    private final String id;
    private final String ticker;
    private final OrderType orderType;
    private final int quantity;
    private OrderStatus status;
    private final LocalDateTime createdAt;

    protected Order(String ticker, OrderType orderType, int quantity) {
        this.id = UUID.randomUUID().toString();
        this.ticker = ticker;
        this.orderType = orderType;
        this.quantity = quantity;
        this.status = OrderStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    /** Returns true when this order should execute at the given market price. */
    public abstract boolean canExecute(double currentPrice);

    /** Human-readable kind: "MARKET" or "LIMIT". */
    public abstract String getOrderKind();

    public String getId() { return id; }
    public String getTicker() { return ticker; }
    public OrderType getOrderType() { return orderType; }
    public int getQuantity() { return quantity; }
    public OrderStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setStatus(OrderStatus status) { this.status = status; }
}
