package com.example.poop2go;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapPickerActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LatLng selectedLatLng;
    private Marker currentMarker;
    private FusedLocationProviderClient fusedLocationClient;

    // Class-level variable to lock down the user's actual physical coordinates
    private Location userPhysicalLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_picker);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_picker_fragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        Button btnConfirm = findViewById(R.id.btn_confirm_location);
        Button btnBack = findViewById(R.id.btn_back);

        btnConfirm.setOnClickListener(v -> {
            if (selectedLatLng != null) {
                // Final safety check before allowing confirmation
                // Checks if the user picked a location
                if (userPhysicalLocation != null) {
                    // Checks if the user allowed location
                    boolean safe = isWithinRadius(
                            userPhysicalLocation.getLatitude(), userPhysicalLocation.getLongitude(),
                            selectedLatLng.latitude, selectedLatLng.longitude,
                            250f
                    );

                    if (safe) {
                        // Checks if the chosen location is within a 50-meter radius from their location
                        // If it is - return to the AddRestroomActivity with the chosen location
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("lat", selectedLatLng.latitude);
                        resultIntent.putExtra("lng", selectedLatLng.longitude);
                        setResult(RESULT_OK, resultIntent);
                        finish();
                    } else {
                        // If the location isn't in the radius - not allowed
                        Toast.makeText(this, "You can only place a restroom within 250 meters of your current location!", Toast.LENGTH_LONG).show();
                    }
                } else {
                    // If the user didn't allow permission to the location - must turn on permission
                    Toast.makeText(this, "Waiting for your physical GPS location to verify...", Toast.LENGTH_SHORT).show();
                }
            } else {
                // If the user didn't pick a location on the map - must pick a location
                Toast.makeText(this, "Please select a location on the map first", Toast.LENGTH_SHORT).show();
            }
        });

        btnBack.setOnClickListener(v -> finish());
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);

            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    // Store the physical location globally for the verification radius
                    userPhysicalLocation = location;

                    LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());

                    // Auto-select the user's current location initially
                    selectedLatLng = userLocation;
                    updateMarker(userLocation);
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 17f)); // Increased zoom to 17f to see 50m clearly
                }
            });
        }

        // Listener for manual selection
        mMap.setOnMapClickListener(latLng -> {
            // Intercept clicks and verify they are within 50 meters before moving the pin
            if (userPhysicalLocation != null) {
                boolean safe = isWithinRadius(
                        userPhysicalLocation.getLatitude(), userPhysicalLocation.getLongitude(),
                        latLng.latitude, latLng.longitude,
                        250f
                );

                if (safe) {
                    selectedLatLng = latLng;
                    updateMarker(latLng);
                } else {
                    Toast.makeText(this, "Cannot place pin: Location is further than 250 meters away.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "GPS location still loading...", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateMarker(LatLng latLng) {
        if (currentMarker != null) currentMarker.remove();
        currentMarker = mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title("Selected Location")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
    }

    public static boolean isWithinRadius(double lat1, double lng1, double lat2, double lng2, float maxRadiusMeters) {
        float[] results = new float[1];
        Location.distanceBetween(lat1, lng1, lat2, lng2, results);
        float distanceInMeters = results[0];
        return distanceInMeters <= maxRadiusMeters;
    }
}