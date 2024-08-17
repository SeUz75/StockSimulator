package com.example.programmingandroid;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

public class StockViewModel extends ViewModel {

    private StockRepository stockRepository;
    private LiveData<StockResponse> stockData;

    public StockViewModel() {
        stockRepository = new StockRepository();
    }

    public LiveData<StockResponse> getStockData(String symbol, String apiKey) {
        if (stockData == null) {
            stockData = stockRepository.getStockData(symbol, apiKey);
        }
        return stockData;
    }
}
