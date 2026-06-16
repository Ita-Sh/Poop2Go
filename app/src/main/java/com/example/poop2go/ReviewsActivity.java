package com.example.poop2go;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.*;
import java.util.ArrayList;
import java.util.List;

public class ReviewsActivity extends AppCompatActivity {
    // The id of the restroom - given in the Intent.putExtra()
    private String restroomId;

    // UI variables
    private RecyclerView rv;
    private ReviewAdapter adapter;
    private List<Review> list = new ArrayList<>();
    private TextView tvEmpty;
    private Button btnNewReview, btnBack;


    // Variables for checking if the user is in a 250-meter radius from the restroom
    private com.google.android.gms.location.FusedLocationProviderClient fusedLocationClient;
    private double restroomLat = 0;
    private double restroomLng = 0;
    private boolean isRestroomDataLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reviews);

        restroomId = getIntent().getStringExtra("RESTROOM_ID");

        if (restroomId == null || restroomId.isEmpty()) {
            Toast.makeText(this, "Error: Could not load reviews", Toast.LENGTH_SHORT).show();
            finish(); // Closes the activity before it can crash
            return;
        }

        // Initialize the location services client
        fusedLocationClient = com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(this);

        // Fetch the specific restroom coordinates from the database
        FirebaseDatabase.getInstance().getReference("Restrooms").child(restroomId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            // Extract coordinates dynamically
                            Double lat = snapshot.child("latitude").getValue(Double.class);
                            Double lng = snapshot.child("longitude").getValue(Double.class);
                            if (lat != null && lng != null) {
                                restroomLat = lat;
                                restroomLng = lng;
                                isRestroomDataLoaded = true;
                            }
                        }
                    }
                    @Override public void onCancelled(@NonNull DatabaseError error) {}
                });

        rv = findViewById(R.id.rv_reviews_list);
        tvEmpty = findViewById(R.id.tv_no_reviews);
        btnNewReview = findViewById(R.id.btn_add_review);
        btnBack = findViewById(R.id.btn_back);

        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ReviewAdapter(list);
        rv.setAdapter(adapter);

        btnNewReview.setOnClickListener(v -> {
            // 1. Check if the restroom data has finished downloading yet
            if (!isRestroomDataLoaded) {
                Toast.makeText(this, "Loading restroom data, please try again...", Toast.LENGTH_SHORT).show();
                return;
            }

            // 2. Verify that location permissions are active
            if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED) {

                // 3. Fetch live user GPS coordinates
                fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                    if (location != null) {
                        // 4. Calculate if the user is within 250 meters
                        boolean insideGeofence = MapPickerActivity.isWithinRadius(
                                location.getLatitude(), location.getLongitude(),
                                restroomLat, restroomLng,
                                250f
                        );

                        if (insideGeofence) {
                            // Allowed! Launch the review addition form
                            Intent intent = new Intent(ReviewsActivity.this, AddReviewActivity.class);
                            intent.putExtra("RESTROOM_ID", restroomId);
                            startActivity(intent);
                        } else {
                            Toast.makeText(this, "You must be within 250 meters of this restroom to write a review.", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(this, "Unable to verify your location. Please ensure GPS is turned on.", Toast.LENGTH_SHORT).show();
                    }
                });

            } else {
                // Permission is missing: block access and show Toast
                Toast.makeText(this, "You must activate location permissions to write a review.", Toast.LENGTH_LONG).show();
            }
        });

        btnBack.setOnClickListener(v -> finish());

        FirebaseDatabase.getInstance().getReference("Reviews").child(restroomId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        list.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            Review r = ds.getValue(Review.class);
                            if (r != null) list.add(r);
                        }
                        tvEmpty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
                        adapter.notifyDataSetChanged();
                    }
                    @Override public void onCancelled(@NonNull DatabaseError error) {}
                });
    }
}