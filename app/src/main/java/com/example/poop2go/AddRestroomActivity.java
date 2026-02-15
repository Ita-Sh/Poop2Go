package com.example.poop2go;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AddRestroomActivity extends AppCompatActivity {

    private TextView tvCoordinates;
    private Button btnPickMap, btnConfirm, btnBack, btnAddPhotos;
    private EditText etRestroomName, etLocation;
    private SwitchCompat swIsSeparated, swIsPaid;

    private double selectedLat = 0, selectedLng = 0;

    // Registering the Activity Result Launcher
    private final ActivityResultLauncher<Intent> mapPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    selectedLat = result.getData().getDoubleExtra("lat", 0);
                    selectedLng = result.getData().getDoubleExtra("lng", 0);
                    tvCoordinates.setText(String.format("Location: %.4f, %.4f", selectedLat, selectedLng));
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_restroom);

        tvCoordinates = findViewById(R.id.tv_coordinates_display);
        btnPickMap = findViewById(R.id.btn_pick_on_map);
        etRestroomName = findViewById(R.id.et_restroom_name);
        etLocation = findViewById(R.id.et_location);
        swIsSeparated = findViewById(R.id.sw_is_separated);
        swIsPaid = findViewById(R.id.sw_is_paid);
        btnConfirm = findViewById(R.id.btn_confirm);
        btnBack = findViewById(R.id.btn_back);
        btnAddPhotos = findViewById(R.id.btn_add_photos);

        // Fetch current GPS as default if possible (logic can be added here)

        btnPickMap.setOnClickListener(v -> {
            Intent intent = new Intent(this, MapPickerActivity.class);
            mapPickerLauncher.launch(intent);
        });

        // Set up button click listeners
        btnConfirm.setOnClickListener(v -> {
            if (etRestroomName.getText().toString().isEmpty() ||
                    etLocation.getText().toString().isEmpty() ||
                    (selectedLat == 0 && selectedLng == 0)) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            } else {
                saveRestroomToDatabase();
                finish();
            }
        });


        btnBack.setOnClickListener(v -> {
            finish();
        });

        // Photos will be implemented later
        btnAddPhotos.setOnClickListener(v -> {
            // Handle add photos button click
        });


        // Other button initializations (Confirm, Back, Add Photos) remain here...
    }

    private void saveRestroomToDatabase() {
        // Getting a unique Id for the restroom
        DatabaseReference restroomRef = FirebaseDatabase.getInstance().getReference("Restrooms");
        String uniqueId = restroomRef.push().getKey();

        // Creating a new Restroom object
        Restroom newRestroom = new Restroom(uniqueId, etRestroomName.getText().toString(),
                selectedLng, selectedLat, etLocation.getText().toString(),
                swIsPaid.isChecked(), swIsSeparated.isChecked());

        // Saving the new Restroom object to the database
        if (uniqueId != null) {
            restroomRef.child(uniqueId).setValue(newRestroom)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Restroom added successfully!", Toast.LENGTH_SHORT).show();
                        finish(); // Return to Map
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }
}