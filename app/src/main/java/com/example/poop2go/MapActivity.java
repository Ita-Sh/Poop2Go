package com.example.poop2go;

import static com.example.poop2go.LoginActivity.REMEMBER_ME_KEY;
import static com.example.poop2go.LoginActivity.SHARED_PREFS;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MapActivity extends AppCompatActivity {
     Button logoutButton;
     TextView isChecked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_map);

        isChecked = findViewById(R.id.tv_is_checked);
        loadData();


        //log out
        logoutButton = findViewById(R.id.logout_button);
        logoutButton.setOnClickListener(v -> {
            FBRef.refAuth.signOut();
            saveData();
            finish();
        });
    }

    private void loadData() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        boolean isRemembered = sharedPreferences.getBoolean(REMEMBER_ME_KEY, false);
        isChecked.setText(String.valueOf(isRemembered));
    }
    private void saveData() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putBoolean(REMEMBER_ME_KEY, false);
        editor.apply();
    }
}