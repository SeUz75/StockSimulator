package com.example.programmingandroid;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Arrays;
import java.util.List;

public class AddMoney extends AppCompatActivity {
    private DataBaseHelper dbHelper;
    private Spinner quantitySpinner;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_money);

        // Retrieve the username from the Intent
        username = getIntent().getStringExtra("USERNAME");


        dbHelper = new DataBaseHelper(this);

        quantitySpinner = findViewById(R.id.quantity_spinner);

        List<Integer> quantityOptions = Arrays.asList(100, 200, 500, 1000, 1500);
        ArrayAdapter<Integer> quantityAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, quantityOptions);
        quantityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        quantitySpinner.setAdapter(quantityAdapter);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    // This method will be called when the button is clicked
    public void addMoney(View view) {
        // Get the selected amount from the Spinner
        int amountToAdd = (int) quantitySpinner.getSelectedItem();

        // Get the user ID based on the username
        int userId = dbHelper.getUserIdByUsername(username);

        // Get the current balance of the user
        double currentBalance = dbHelper.getUserBalance(userId);

        // Calculate the new balance
        double newBalance = currentBalance + amountToAdd;

        // Update the user's balance in the database
        dbHelper.updateUserBalance(userId, newBalance);

        // Show a confirmation message
        Toast.makeText(this, "Added $" + amountToAdd + " to your account. New balance: $" + newBalance, Toast.LENGTH_LONG).show();

        // Optionally, you could finish the activity and return to the previous screen
        finish();
    }
}