package com.example.poop2go;

import static com.example.poop2go.LoginActivity.REMEMBER_ME_KEY;
import static com.example.poop2go.LoginActivity.SHARED_PREFS;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private Button btnLogin, btnRegister, btnEnterWithoutAccount;
    Intent intent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Check if user is already logged in
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        boolean rememberMe = sharedPreferences.getBoolean(REMEMBER_ME_KEY, false);
        if (rememberMe && FBRef.refAuth.getCurrentUser() != null) {
            FirebaseUser user = FBRef.refAuth.getCurrentUser();
            intent = new Intent(this, MapActivity.class);
            startActivity(intent);
        }

        // Initialize buttons
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);

        // btnEnterWithoutAccount = findViewById(R.id.btnEnterWithoutAccount);

        // Set click listeners
        btnLogin.setOnClickListener(v -> {
            intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        });

        btnRegister.setOnClickListener(v -> {
            intent = new Intent(this, RegisterActivity.class);
            startActivity(intent);
        });

        /*
        btnEnterWithoutAccount.setOnClickListener(v -> {
            // Handle guest entry
        });

         */
    }
}