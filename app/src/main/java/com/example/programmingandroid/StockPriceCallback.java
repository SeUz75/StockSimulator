package com.example.programmingandroid;

public interface StockPriceCallback {
    void onSuccess(double price);
    void onFailure(Throwable t);
}
