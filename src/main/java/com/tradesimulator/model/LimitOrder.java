package com.tradesimulator.model;

/**
 * A limit order executes only when the price crosses the specified threshold.
 * BUY limit: executes when currentPrice <= limitPrice.
 * SELL limit: executes when currentPrice >= limitPrice.
 * Created exclusively through OrderFactory (Factory pattern).
 */
public class LimitOrder extends Order {

    private final double limitPrice;

    public LimitOrder(String ticker, OrderType orderType, int quantity, double limitPrice) {
        super(ticker, orderType, quantity);
        this.limitPrice = limitPrice;
    }

    @Override
    public boolean canExecute(double currentPrice) {
        if (getOrderType() == OrderType.BUY) {
            return currentPrice <= limitPrice;
        } else {
            return currentPrice >= limitPrice;
        }
    }

    @Override
    public String getOrderKind() {
        return "LIMIT";
    }

    public double getLimitPrice() {
        return limitPrice;
    }
}
