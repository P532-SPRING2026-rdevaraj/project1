package com.tradesimulator.order;

/**
 * DTO for incoming order placement requests from the REST API.
 */
public class OrderRequest {

    private String ticker;
    private String orderKind;   // "MARKET" or "LIMIT"
    private String orderType;   // "BUY" or "SELL"
    private int quantity;
    private double limitPrice;  // 0 for market orders

    public String getTicker() { return ticker; }
    public void setTicker(String ticker) { this.ticker = ticker; }

    public String getOrderKind() { return orderKind; }
    public void setOrderKind(String orderKind) { this.orderKind = orderKind; }

    public String getOrderType() { return orderType; }
    public void setOrderType(String orderType) { this.orderType = orderType; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getLimitPrice() { return limitPrice; }
    public void setLimitPrice(double limitPrice) { this.limitPrice = limitPrice; }
}
