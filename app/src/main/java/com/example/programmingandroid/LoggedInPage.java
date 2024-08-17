package com.example.programmingandroid;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoggedInPage extends AppCompatActivity {
    private DataBaseHelper dbHelper;
    private TextView textViewBalance;
    private TextView textViewUsername;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_logged_in_page);
        // Initialize Views
        textViewUsername = findViewById(R.id.nameofuser);
        textViewBalance = findViewById(R.id.balance);

        // Initialize the database helper
        dbHelper = new DataBaseHelper(this);


        // Get the username passed from the login activity
        String username = getIntent().getStringExtra("USERNAME");
        userId = dbHelper.getUserIdByUsername(username);
        displayUserStocks(); // Make sure this is called after userId is set
        if (username != null) {
            userId = dbHelper.getUserIdByUsername(username);

            // Log the userId to ensure it's correctly set
            Log.d("LoggedInPage", "User ID: " + userId);

            displayUserStocks(); // Make sure this is called after userId is set
        } else {
            Log.d("LoggedInPage", "Username is null, cannot retrieve user ID");
        }

        // Apply window insets for immersive UI
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        String username = getIntent().getStringExtra("USERNAME");
        if (username != null) {
            loadUserData(username);  // Reload data in case of changes
            //displayUserStocks();
        }
    }

    private void loadUserData(String username) {
        Log.d("LoggedInPage", "Loading user data for: " + username);

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] columns = {DataBaseHelper.USER_ID, DataBaseHelper.USERNAME, DataBaseHelper.BALANCE};
        String selection = DataBaseHelper.USERNAME + "= ?";
        String[] selectionArgs = {username};
        Cursor cursor = db.query(
                DataBaseHelper.TABLE_USERS,
                columns,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        if (cursor != null && cursor.moveToFirst()) {
            int userId = cursor.getInt(cursor.getColumnIndexOrThrow(DataBaseHelper.USER_ID));
            String user = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseHelper.USERNAME));
            double balance = cursor.getDouble(cursor.getColumnIndexOrThrow(DataBaseHelper.BALANCE));

            textViewUsername.setText(user);
            textViewBalance.setText("Balance: $" + String.format("%.2f", balance));

            cursor.close();

        } else {
            textViewUsername.setText("Unknown User");
            textViewBalance.setText("Balance: $0.00");
        }
        db.close();
    }


    private void setUpSpinners(List<User_Stock> userStocks) {
        Spinner stockSpinner = findViewById(R.id.sell_stock_spinner);
        Spinner quantitySpinner = findViewById(R.id.sell_quantity_spinner);

        List<String> stockSymbols = new ArrayList<>();
        List<Integer> quantities = new ArrayList<>();

        for (User_Stock stock : userStocks) {
            stockSymbols.add(stock.getSymbol());
            quantities.add(stock.getQuantity());
        }

        // Set up adapters for spinners
        ArrayAdapter<String> stockAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, stockSymbols);
        stockAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        stockSpinner.setAdapter(stockAdapter);

        ArrayAdapter<Integer> quantityAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, quantities);
        quantityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        quantitySpinner.setAdapter(quantityAdapter);
    }



    // Method to open the item stock page
    public void openItemStock(View view) {
        Intent intent = new Intent(this, item_stock.class);
        intent.putExtra("USER_ID", userId); // Pass the userId
        startActivity(intent);
    }

    // Method to open the add money page
    public void openAddMoney(View view) {
        Intent intent = new Intent(LoggedInPage.this, AddMoney.class);
        String username = textViewUsername.getText().toString();
        Log.d("LoggedInPage", "Opening AddMoney with username: " + username);
        intent.putExtra("USERNAME", username);
        startActivity(intent);
    }

    // Method to sell stock
    public void sellStock(View view) {
        Spinner stockSpinner = findViewById(R.id.sell_stock_spinner);
        Spinner quantitySpinner = findViewById(R.id.sell_quantity_spinner);

        String stockSymbol = stockSpinner.getSelectedItem().toString();
        int quantity = (Integer) quantitySpinner.getSelectedItem();

        // Fetch current stock price and update balance
        getStockPrice(stockSymbol, new StockPriceCallback() {
            @Override
            public void onSuccess(double stockPrice) {
                double totalValue = stockPrice * quantity;
                double userBalance = dbHelper.getUserBalance(userId);

                // Update user balance
                dbHelper.updateUserBalance(userId, userBalance + totalValue);

                // Convert stockSymbol to stockId
                int stockId = dbHelper.getStockIdBySymbol(stockSymbol);
                if (stockId != -1) {
                    // Remove stock from user
                    dbHelper.removeStockFromUser(userId, stockId, quantity);
                    Toast.makeText(LoggedInPage.this, "Stock sold successfully!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(LoggedInPage.this, "Stock symbol not found", Toast.LENGTH_SHORT).show();
                }

                // Refresh user data
                loadUserData(getIntent().getStringExtra("USERNAME"));
            }

            @Override
            public void onFailure(Throwable t) {
                Toast.makeText(LoggedInPage.this, "Failed to sell stock: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getStockPrice(String stockSymbol, StockPriceCallback callback) {
        StockApi stockApi = ApiClient.getStockApi();
        Call<StockResponse> call = stockApi.getIntradayData("TIME_SERIES_INTRADAY", stockSymbol, "1min", "29F3V6EO78GOC5IJ");
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
                                String priceStr = firstDataPoint.getClose();
                                double price = Double.parseDouble(priceStr != null ? priceStr : "0.0");
                                callback.onSuccess(price);
                                return;
                            }
                        } else {
                            Log.e("API Error", "Time series data is empty or null for symbol: " + stockSymbol);
                            callback.onFailure(new Exception("No stock data available"));
                        }
                    } else {
                        Log.e("API Error", "Response body is null");
                        callback.onFailure(new Exception("Failed to retrieve stock data"));
                    }
                } else {
                    try {
                        Log.e("API Error", "Unsuccessful response: " + response.errorBody().string());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    callback.onFailure(new Exception("Failed to fetch stock price"));
                }
            }

            @Override
            public void onFailure(Call<StockResponse> call, Throwable t) {
                Log.e("API Failure", "Error: " + t.getMessage(), t);
                callback.onFailure(t);
            }
        });
    }

    private void displayUserStocks() {
        Log.d("Database", "Fetching stocks for user ID: " + userId);
        List<User_Stock> userStocks = dbHelper.getUserStocks(userId);

        // Get the LinearLayout where you want to display the stocks
        LinearLayout stocksContainer = findViewById(R.id.stocks_container1);
        stocksContainer.removeAllViews(); // Clear any existing views

        // Iterate over the list of user stocks and create views for each one
        for (User_Stock userStock : userStocks) {
            Log.d("DisplayUserStocks", "Displaying stock: " + userStock.getSymbol());

            LinearLayout stockLayout = new LinearLayout(this);
            stockLayout.setOrientation(LinearLayout.HORIZONTAL);
            stockLayout.setPadding(8, 8, 8, 8);

            TextView textViewSymbol = new TextView(this);
            textViewSymbol.setText("Symbol: " + userStock.getSymbol());
            textViewSymbol.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

            TextView textViewQuantity = new TextView(this);
            textViewQuantity.setText("Quantity: " + userStock.getQuantity());
            textViewQuantity.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

            stockLayout.addView(textViewSymbol);
            stockLayout.addView(textViewQuantity);

            stocksContainer.addView(stockLayout);
        }
    }
}
