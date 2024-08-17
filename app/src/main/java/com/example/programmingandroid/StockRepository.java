package com.example.programmingandroid;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StockRepository {
    private StockApi stockApi;

    public StockRepository() {
        stockApi = ApiClient.getStockApi();
    }

    public LiveData<StockResponse> getStockData(String symbol, String apiKey) {
        MutableLiveData<StockResponse> stockData = new MutableLiveData<>();

        stockApi.getIntradayData("TIME_SERIES_INTRADAY", symbol, "1min", apiKey).enqueue(new Callback<StockResponse>() {
            @Override
            public void onResponse(Call<StockResponse> call, Response<StockResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    stockData.setValue(response.body());
                } else {
                    // Log error message and update UI accordingly
                    Log.e("StockRepository", "Error: " + response.message());
                    stockData.setValue(null);
                }
            }

            @Override
            public void onFailure(Call<StockResponse> call, Throwable t) {
                // Log the failure and notify UI
                Log.e("StockRepository", "Failed to fetch stock data", t);
                stockData.setValue(null);
            }
        });
        return stockData;
    }
}
