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

public class MainActivity extends AppCompatActivity {
    private DataBaseHelper dbHelper;
    private TextView editTextUsername;
    private TextView editTextPassword;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Initialise DB HELPER
        dbHelper = new DataBaseHelper(this);
        dbHelper.getWritableDatabase(); // Ensure that the DB is created.

        // Initialise EditText fields
        editTextUsername = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    public void logIn(View view){
        String username = editTextUsername.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();


        if(dbHelper.validateUser(username,password)){
            //if the user is validated, move to log in page else....
            Intent intent = new Intent(this, LoggedInPage.class);
            intent.putExtra("USERNAME", username); // Pass the username
            startActivity(intent);
        }else{
            // else print an error message.
            Toast.makeText(MainActivity.this,"Invalid username or password", Toast.LENGTH_SHORT).show();
        }
    }

    public void openCreateAccountActivity(View view){
            startActivity(new Intent(this,CreateAccountActivity.class));
    }

}