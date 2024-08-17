package com.example.programmingandroid;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.net.HttpURLConnection;
import java.net.URL;

public class item_stock extends AppCompatActivity {
    private List<String> stockSymbols = Arrays.asList("AAPL", "GOOGL", "AMZN", "MSFT", "TSLA");
    private static final String TAG = "item_stock";
    private Button buyButton;
    private DataBaseHelper dbHelper;
    private int userId; // Assume this is set when the user logs in

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_stock);

        dbHelper = new DataBaseHelper(this);  // Initialize your database helper
        userId = getIntent().getIntExtra("USER_ID", -1); // Default value is -1 if USER_ID is not found

        if (userId == -1) {
            // Handle the case where userId is not passed or retrieved correctly
            Log.e(TAG, "Failed to retrieve user ID");
            Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show();
            finish(); // Optionally close the activity if userId is critical
            return;
        }

        Log.d(TAG, "User ID: " + userId); // For debugging

        // Set up spinners
        Spinner stockSpinner = findViewById(R.id.stock_spinner);
        Spinner quantitySpinner = findViewById(R.id.quantity_spinner);

        ArrayAdapter<String> stockAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, stockSymbols);
        stockAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        stockSpinner.setAdapter(stockAdapter);

        List<Integer> quantityOptions = Arrays.asList(1, 5, 10, 20, 50);
        ArrayAdapter<Integer> quantityAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, quantityOptions);
        quantityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        quantitySpinner.setAdapter(quantityAdapter);

        Log.d(TAG, "Activity Created");

        // Fetch and display stock data for multiple stocks
        getStockDataForMultipleStocks();
    }

    public void buyStock(View view) {
        // Get selected stock symbol and quantity
        Spinner stockSpinner = findViewById(R.id.stock_spinner);
        Spinner quantitySpinner = findViewById(R.id.quantity_spinner);

        String selectedStockSymbol = stockSpinner.getSelectedItem().toString();
        int quantity = (int) quantitySpinner.getSelectedItem();

        // Fetch the current stock price from the UI
        LinearLayout stocksContainer = findViewById(R.id.stocks_container);
        double stockPrice = 0.0;
        for (int i = 0; i < stocksContainer.getChildCount(); i++) {
            LinearLayout stockLayout = (LinearLayout) stocksContainer.getChildAt(i);
            TextView symbolView = (TextView) stockLayout.getChildAt(0);
            TextView priceView = (TextView) stockLayout.getChildAt(1);
            if (symbolView.getText().toString().equals(selectedStockSymbol)) {
                String stockPriceString = priceView.getText().toString().replace("Price: ", "");
                stockPrice = Double.parseDouble(stockPriceString);
                break;
            }
        }

        // Get the user's current balance
        double userBalance = dbHelper.getUserBalance(userId);

        // Calculate the total cost of the purchase
        double totalCost = stockPrice * quantity;

        if (totalCost <= userBalance) {
            // Deduct the total cost from the user's balance
            double newBalance = userBalance - totalCost;
            dbHelper.updateUserBalance(userId, newBalance);

            // Add the stock to the user's portfolio
            int stockId = dbHelper.getStockIdBySymbol(selectedStockSymbol);
            if (stockId == -1) {
                // If the stock doesn't exist, add it to the stocks table
                stockId = dbHelper.addStock(selectedStockSymbol, stockPrice);
            }

            // Update the user's stock holdings
            dbHelper.addStockToUser(userId, stockId, quantity);

            // Show a confirmation message
            Toast.makeText(this, "Stock purchased successfully!", Toast.LENGTH_SHORT).show();
        } else {
            // Show an error message if the user doesn't have enough balance
            Toast.makeText(this, "Insufficient balance.", Toast.LENGTH_SHORT).show();
        }
    }


    private void getStockDataForMultipleStocks() {
        StockApi stockApi = ApiClient.getStockApi();
        LinearLayout stocksContainer = findViewById(R.id.stocks_container);

        for (String symbol : stockSymbols) {
            Call<StockResponse> call = stockApi.getIntradayData("TIME_SERIES_INTRADAY", symbol, "1min", "29F3V6EO78GOC5IJ");

            call.enqueue(new Callback<StockResponse>() {
                @Override
                public void onResponse(Call<StockResponse> call, Response<StockResponse> response) {
                    if (response.isSuccessful()) {
                        StockResponse stockResponse = response.body();
                        if (stockResponse != null) {
                            Map<String, StockResponse.StockData> timeSeries = stockResponse.getTimeSeries();
                            if (timeSeries != null && !timeSeries.isEmpty()) {
                                StockResponse.StockData firstDataPoint = timeSeries.values().iterator().next();
                                if (firstDataPoint != null) {
                                    String price = firstDataPoint.getClose();
                                    String volume = firstDataPoint.getVolume();

                                    // Create a new layout for this stock
                                    LinearLayout stockLayout = new LinearLayout(item_stock.this);
                                    stockLayout.setOrientation(LinearLayout.HORIZONTAL);
                                    stockLayout.setPadding(8, 8, 8, 8);

                                    TextView textViewSymbol = new TextView(item_stock.this);
                                    textViewSymbol.setText(symbol);
                                    textViewSymbol.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

                                    TextView textViewPrice = new TextView(item_stock.this);
                                    textViewPrice.setText("Price: " + (price != null ? price : "N/A"));
                                    textViewPrice.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

                                    TextView textViewVolume = new TextView(item_stock.this);
                                    textViewVolume.setText("Volume: " + (volume != null ? volume : "N/A"));
                                    textViewVolume.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

                                    // Add the TextViews to the layout
                                    stockLayout.addView(textViewSymbol);
                                    stockLayout.addView(textViewPrice);
                                    stockLayout.addView(textViewVolume);

                                    // Add the stock layout to the container
                                    stocksContainer.addView(stockLayout);
                                }
                            }
                        }
                    } else {
                        Log.e(TAG, "API Response Error for " + symbol + ": " + response.message());
                    }
                }

                @Override
                public void onFailure(Call<StockResponse> call, Throwable t) {
                    Log.e(TAG, "API Call Failed for " + symbol + ": " + t.getMessage());
                }
            });
        }
    }
}