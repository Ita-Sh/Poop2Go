package com.example.poop2go;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private Button btnLogin, btnRegister, btnEnterWithoutAccount;
    Intent intent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Initialize buttons
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        btnEnterWithoutAccount = findViewById(R.id.btnEnterWithoutAccount);

        // Set click listeners
        btnLogin.setOnClickListener(v -> {
            intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        });

        btnRegister.setOnClickListener(v -> {
            intent = new Intent(this, RegisterActivity.class);
            startActivity(intent);
        });

        btnEnterWithoutAccount.setOnClickListener(v -> {
            // Handle guest entry
        });
    }
}