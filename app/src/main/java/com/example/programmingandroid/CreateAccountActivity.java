package com.example.programmingandroid;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class CreateAccountActivity extends AppCompatActivity {
    private DataBaseHelper dbHelper;
    private TextView textViewUsername;
    private TextView textViewPassword;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_account);

        textViewUsername = findViewById(R.id.emailAccountCreating);
        textViewPassword = findViewById(R.id.passwordAccountCreating);

        dbHelper = new DataBaseHelper(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    public void userCreate(View view) {

        String username = textViewUsername.getText().toString().trim();
        String password = textViewPassword.getText().toString().trim();

        dbHelper.createUser(username, password);

        // Validate inputs
        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter both username and password", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if the user already exists
        if (dbHelper.validateUser(username, password)) {
            Toast.makeText(this, "Account already exists", Toast.LENGTH_SHORT).show();
        } else {
            // Create a new user
            dbHelper.createUser(username, password);

            // Display success message
            Toast.makeText(this, "Account created successfully", Toast.LENGTH_SHORT).show();

            // Redirect to MainActivity for login
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();  // Close the current activity so user cannot go back to it


        }

    }
}