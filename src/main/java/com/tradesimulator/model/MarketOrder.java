package com.tradesimulator.model;

/**
 * A market order executes immediately at the current price.
 * Created exclusively through OrderFactory (Factory pattern).
 */
public class MarketOrder extends Order {

    public MarketOrder(String ticker, OrderType orderType, int quantity) {
        super(ticker, orderType, quantity);
    }

    @Override
    public boolean canExecute(double currentPrice) {
        return true;
    }

    @Override
    public String getOrderKind() {
        return "MARKET";
    }
}
