package com.example.programmingandroid;

public class User_Stock {
    private int stockId;
    private String symbol;
    private int quantity;
    private int currectPrice;

    public User_Stock(String symbol, int quantity) {
        this.symbol = symbol;
        this.quantity = quantity;
    }

    public int getStockId() {
        return stockId;
    }

    public int getCurrectPrice(){
        return currectPrice;
    }

    public String getSymbol() {
        return symbol;
    }


    public int getQuantity() {
        return quantity;
    }
}