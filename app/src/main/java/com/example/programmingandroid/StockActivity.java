package com.example.programmingandroid;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StockActivity extends AppCompatActivity {

    private StockViewModel stockViewModel;
    private RecyclerView recyclerViewStocks;
    private StockAdapter stockAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_market);

        recyclerViewStocks = findViewById(R.id.stockstable);
        recyclerViewStocks.setLayoutManager(new LinearLayoutManager(this));


        stockViewModel = new ViewModelProvider(this).get(StockViewModel.class);

        String symbol = "AAPL"; // Example stock symbol
        String apiKey = "OAHS92SEYBDD2IGW"; // Your Alpha Vantage API key

        stockViewModel.getStockData(symbol, apiKey).observe(this, new Observer<StockResponse>() {
            @Override
            public void onChanged(StockResponse stockResponse) {
                if (stockResponse != null) {
                    List<StockResponse.StockData> stockDataList =new ArrayList<>(stockResponse.getTimeSeries().values());
                    stockAdapter = new StockAdapter(stockDataList,symbol);
                    recyclerViewStocks.setAdapter(stockAdapter);
                } else{

                }
            }
        });
    }
}
