package com.tradesimulator.model;

import java.time.LocalDateTime;

/**
 * An immutable record of an executed trade, stored in the Portfolio's trade history.
 */
public class Trade {

    private final String ticker;
    private final OrderType side;
    private final int quantity;
    private final double price;
    private final double totalValue;
    private final LocalDateTime timestamp;

    public Trade(String ticker, OrderType side, int quantity, double price) {
        this.ticker = ticker;
        this.side = side;
        this.quantity = quantity;
        this.price = price;
        this.totalValue = Math.round(price * quantity * 100.0) / 100.0;
        this.timestamp = LocalDateTime.now();
    }

    public String getTicker() { return ticker; }
    public OrderType getSide() { return side; }
    public int getQuantity() { return quantity; }
    public double getPrice() { return price; }
    public double getTotalValue() { return totalValue; }
    public LocalDateTime getTimestamp() { return timestamp; }
}
