package com.example.poop2go;

import static com.example.poop2go.LoginActivity.REMEMBER_ME_KEY;
import static com.example.poop2go.LoginActivity.SHARED_PREFS;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.location.Location;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Button btnLogout, btnAddRestroom;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    // Default location: Makif Alef
    private final LatLng MAKIF_ALEF = new LatLng(31.25218866868315, 34.800391651282624);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // Initialize the FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Initialize UI components
        btnLogout = findViewById(R.id.btn_logout);
        btnAddRestroom = findViewById(R.id.btn_add_restroom);

        // Set up the Map Fragment and request the map to be loaded asynchronously
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_fragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Handle Logout functionality
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Save the user's choice to not stay logged in
                saveData();

                // Sign out from Firebase Authentication
                FBRef.refAuth.signOut();
                Toast.makeText(MapActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();

                // Return to the main activity
                Intent intent = new Intent(MapActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // Navigate to 'Add Restroom' screen (Placeholder)
        btnAddRestroom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // To be implemented: Intent to AddRestroomActivity
                Toast.makeText(MapActivity.this, "Opening 'Add Restroom' screen...", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveData() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putBoolean(REMEMBER_ME_KEY, false);
        editor.apply();
    }

    /**
     * This callback is triggered when the map is ready to be used.
     */
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        checkLocationPermission();
    }

    /**
     * Checks if the app has permission to access the device location.
     */
    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            // Permission already granted
            enableUserLocation();
        } else {
            // Request permission from the user
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    /**
     * Enables the 'My Location' layer on the map and zooms to the current position.
     */
    private void enableUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        // Show the blue dot on the map
        mMap.setMyLocationEnabled(true);

        // Fetch the last known location
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            // Center map on user location
                            LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 16f));
                        } else {
                            // Location is null, fall back to Makif Alef
                            moveToDefaultLocation();
                        }
                    }
                });
    }
    /**
     * Centers the map on the default location (Makif Alef).
     */
    private void moveToDefaultLocation() {
        mMap.addMarker(new MarkerOptions().position(MAKIF_ALEF).title("Default Location: Tel Aviv"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(MAKIF_ALEF, 16f));
    }

    /**
     * Handles the result of the permission request.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted by the user
                enableUserLocation();
            } else {
                // Permission denied, use default location
                Toast.makeText(this, "Permission denied. Showing Makif Alef.", Toast.LENGTH_LONG).show();
                moveToDefaultLocation();
            }
        }
    }
}