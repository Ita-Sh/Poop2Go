package com.example.poop2go;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class AddRestroomActivity extends AppCompatActivity {

    private TextView tvCoordinates;
    private Button btnPickMap, btnConfirm, btnBack, btnAddPhotos;
    private EditText etRestroomName, etLocation;
    private SwitchCompat swIsSeparated, swIsPaid;
    private String currentRestroomId; // Pre-generate the ID
    private StorageReference storageRef;
    private ActivityResultLauncher<String> galleryLauncher;
    private ProgressBar uploadProgressBar;

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

        // 1. Pre-generate the ID so photos have a place to go
        currentRestroomId = FirebaseDatabase.getInstance().getReference("Restrooms").push().getKey();
        storageRef = FirebaseStorage.getInstance().getReference("RestroomPhotos");

        // 2. Initialize UI
        uploadProgressBar = findViewById(R.id.upload_progress_bar_add);
        btnAddPhotos = findViewById(R.id.btn_add_photos);
        tvCoordinates = findViewById(R.id.tv_coordinates_display);
        btnPickMap = findViewById(R.id.btn_pick_on_map);
        etRestroomName = findViewById(R.id.et_restroom_name);
        etLocation = findViewById(R.id.et_location);
        swIsSeparated = findViewById(R.id.sw_is_separated);
        swIsPaid = findViewById(R.id.sw_is_paid);
        btnConfirm = findViewById(R.id.btn_confirm);
        btnBack = findViewById(R.id.btn_back);
        btnAddPhotos = findViewById(R.id.btn_add_photos);

        // 3. Set up Gallery Launcher (Same as MapActivity)
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        handleImageUpload(uri);
                    }
                }
        );


        // Fetch current GPS as default if possible
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
            }
        });

        btnAddPhotos.setOnClickListener(v -> galleryLauncher.launch("image/*"));

        btnBack.setOnClickListener(v -> {
            finish();
        });



        // Other button initializations (Confirm, Back, Add Photos) remain here...
    }

    private void saveRestroomToDatabase() {
        // Getting a unique Id for the restroom
        DatabaseReference restroomRef = FirebaseDatabase.getInstance().getReference("Restrooms");

        // Creating a new Restroom object
        Restroom newRestroom = new Restroom(currentRestroomId, etRestroomName.getText().toString(),
                selectedLng, selectedLat, etLocation.getText().toString(),
                swIsPaid.isChecked(), swIsSeparated.isChecked());

        // Saving the new Restroom object to the database
        restroomRef.child(currentRestroomId).setValue(newRestroom)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Restroom added successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Handle the 'Add Photos' button click
    private void handleImageUpload(Uri uri) {
        uploadProgressBar.setVisibility(View.VISIBLE); // Show the progress bar
        btnAddPhotos.setEnabled(false); // Prevent double-clicks

        byte[] compressedData = compressImage(uri); // Compresses the image
        if (compressedData == null) {
            uploadProgressBar.setVisibility(View.GONE);
            btnAddPhotos.setEnabled(true);
            return;
        }

        // Upload the compressed image to firebase storage
        String photoId = FirebaseDatabase.getInstance().getReference().push().getKey();
        StorageReference fileRef = storageRef.child(currentRestroomId).child(photoId + ".jpg");

        fileRef.putBytes(compressedData).addOnSuccessListener(taskSnapshot -> {
            fileRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                savePhotoMetadata(photoId, downloadUri.toString());
            });
        }).addOnFailureListener(e -> {
            uploadProgressBar.setVisibility(View.GONE);
            btnAddPhotos.setEnabled(true);
            Toast.makeText(this, "Upload failed", Toast.LENGTH_SHORT).show();
        });
    }

    // Save the Image to Realtime Database
    private void savePhotoMetadata(String photoId, String downloadUrl) {
        String uId = FirebaseAuth.getInstance().getUid();
        Photo newPhoto = new Photo(photoId, currentRestroomId, uId, downloadUrl, System.currentTimeMillis());

        FirebaseDatabase.getInstance().getReference("RestroomPhotos")
                .child(currentRestroomId)
                .child(photoId)
                .setValue(newPhoto)
                .addOnSuccessListener(aVoid -> {
                    uploadProgressBar.setVisibility(View.GONE);
                    btnAddPhotos.setEnabled(true);
                    Toast.makeText(this, "Photo added!", Toast.LENGTH_SHORT).show();
                });
    }

    // Compresses the image to a max of 100KB
    private byte[] compressImage(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            Bitmap original = BitmapFactory.decodeStream(inputStream);
            int quality = 100; // This is the percentage of compression. 100 means no compression.
            byte[] imageData; // This will hold the compressed image, which is a byte array
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            original.compress(Bitmap.CompressFormat.JPEG, quality, baos);
            imageData = baos.toByteArray();
            while (imageData.length > 100 * 1024 && quality > 10) {
                // Reduce quality until the image is under 100KB. The minimum quality is 10%.
                baos.reset();
                quality -= 10;
                original.compress(Bitmap.CompressFormat.JPEG, quality, baos);
                imageData = baos.toByteArray();
            }
            return imageData;
        } catch (Exception e) {
            return null;
        }
    }
}