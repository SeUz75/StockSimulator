package com.example.programmingandroid;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MarketActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private StockAdapter stockAdapter;
    private List<StockResponse.StockData> stockDataList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_market);

        recyclerView = findViewById(R.id.stockstable);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        stockAdapter = new StockAdapter(stockDataList, "");
        recyclerView.setAdapter(stockAdapter);

        Button buyStockButton = findViewById(R.id.buystock);
        buyStockButton.setOnClickListener(v -> {
            Intent intent = new Intent(MarketActivity.this, item_stock.class);
            startActivity(intent);
        });

        // Load stock data and update the RecyclerView
        loadStockData();
    }

    private void loadStockData() {
        // Simulate loading stock data
        // This should be replaced with actual data fetching
        // For example, using StockRepository or another approach
        // Update stockDataList and notify adapter
    }
}