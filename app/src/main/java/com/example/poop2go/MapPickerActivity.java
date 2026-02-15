package com.example.poop2go;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapPickerActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LatLng selectedLatLng;
    private Marker currentMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_picker);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_picker_fragment);
        mapFragment.getMapAsync(this);

        findViewById(R.id.btn_confirm_location).setOnClickListener(v -> {
            if (selectedLatLng != null) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("lat", selectedLatLng.latitude);
                resultIntent.putExtra("lng", selectedLatLng.longitude);
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Default to a known location (Omer/Beer Sheva) if no user location is provided
        LatLng startLoc = new LatLng(31.2589, 34.7997);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startLoc, 15f));

        mMap.setOnMapClickListener(latLng -> {
            selectedLatLng = latLng;
            if (currentMarker != null) currentMarker.remove();
            currentMarker = mMap.addMarker(new MarkerOptions().position(latLng).title("New Restroom Here"));
        });
    }
}