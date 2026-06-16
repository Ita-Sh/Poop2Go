package com.example.poop2go;

import static com.example.poop2go.LoginActivity.REMEMBER_ME_KEY;
import static com.example.poop2go.LoginActivity.SHARED_PREFS;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.location.Location;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.auth.FirebaseAuth;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    // The selected radius - global variable
    private Restroom currentSelectedRestroom;

    // Elements for the regular map activity
    private GoogleMap mMap;
    private Button btnLogout, btnAddRestroom;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    // Default location: Makif Alef
    private final LatLng MAKIF_ALEF = new LatLng(31.25218866868315, 34.800391651282624);

    // Maps Restroom ID -> Google Maps Marker object
    private HashMap<String, Marker> visibleMarkers = new HashMap<>();

    // Elements for the pop-up menu
    private BottomSheetBehavior<View> sheetBehavior;
    private TextView tvName, tvLocation, tvSeparated, tvPaid, tvRating;
    private Button btnReviews, btnPhotos;
    private ProgressBar uploadProgressBar;
    private String selectedRestroomId;

    // Elements for the Storage of images in Firebase Storage
    private ActivityResultLauncher<String> galleryLauncher;

    // Elements for the display of images in Firebase Storage
    private RecyclerView rvPhotos;
    private PhotoAdapter photoAdapter;
    private List<Photo> restroomPhotosList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // Initialize the FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Initialize UI components
        btnLogout = findViewById(R.id.btn_logout);
        btnAddRestroom = findViewById(R.id.fab_add_restroom);

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

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {/*Empty to disable the back button*/}
        });

        // Navigate to 'Add Restroom' screen (Placeholder)
        btnAddRestroom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check if either FINE or COARSE location permission is granted
                if (ContextCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                    // Permission active: allow them to proceed to AddRestroomActivity
                    Intent intent = new Intent(MapActivity.this, AddRestroomActivity.class);
                    startActivity(intent);

                } else {
                    // Permission missing: block entry and show the Toast message
                    Toast.makeText(MapActivity.this, "You must activate location to add a restroom.", Toast.LENGTH_LONG).show();
                }
            }
        });

        // Initialize the bottom sheet
        initBottomSheet();

        // `
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        handleImageUpload(uri);
                    }
                }
        );
    }

    // Saves the user's choice to not stay logged in
    private void saveData() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putBoolean(REMEMBER_ME_KEY, false);
        editor.apply();
    }

    // This callback is triggered when the map is ready to be used.
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        setupMarkerClick();

        // Ensure the window has focus before asking for permissions
        getWindow().getDecorView().post(() -> {
            checkLocationPermission();
        });

        // Triggered every time the user finishes moving the camera
        // Used to show all restrooms on the screen
        mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                retrieveRestroomsInView();
            }
        });
    }

    // Checks if the app has permission to access the device location.
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
    // Enables the 'My Location' layer on the map and zooms to the current position.

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

    // Centers the map on the default location (Makif Alef).
    private void moveToDefaultLocation() {
        mMap.addMarker(new MarkerOptions().position(MAKIF_ALEF).title("Default Location: Makif Alef"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(MAKIF_ALEF, 16f));
    }

    // Handles the result of the permission request.
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

    // Retrieves restrooms from Firebase and filters them based on screen visibility
    // AND a minimum zoom level.
    private void retrieveRestroomsInView() {
        float currentZoom = mMap.getCameraPosition().zoom;

        if (currentZoom < 15f) {
            // Zoomed out: Clear everything
            for (Marker m : visibleMarkers.values()) m.remove();
            visibleMarkers.clear();
            return;
        }

        DatabaseReference restroomRef = FirebaseDatabase.getInstance().getReference("Restrooms");
        LatLngBounds curScreen = mMap.getProjection().getVisibleRegion().latLngBounds;

        restroomRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Create a set of IDs currently in the database to track what should stay
                HashSet<String> restroomsInQuery = new HashSet<>();

                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    Restroom restroom = postSnapshot.getValue(Restroom.class);
                    if (restroom == null) continue;

                    String id = restroom.getRestroomId();
                    LatLng position = new LatLng(restroom.getLatitude(), restroom.getLongitude());

                    if (curScreen.contains(position)) {
                        restroomsInQuery.add(id);

                        // ONLY add if the marker isn't already there
                        if (!visibleMarkers.containsKey(id)) {
                            Marker m = mMap.addMarker(new MarkerOptions()
                                    .position(position)
                                    .title(restroom.getRestroomName())
                                    .snippet("Tap for details")
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                            m.setTag(id);
                            visibleMarkers.put(id, m);
                        }
                    }
                }

                // Remove markers that are no longer in the view/database
                Iterator<Map.Entry<String, Marker>> it = visibleMarkers.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<String, Marker> entry = it.next();
                    if (!restroomsInQuery.contains(entry.getKey())) {
                        entry.getValue().remove(); // Remove from map
                        it.remove(); // Remove from HashMap
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MapActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Methods for the pop-up menu of each restroom:
    private void initBottomSheet() {
        View bottomSheet = findViewById(R.id.restroom_bottom_sheet);
        sheetBehavior = BottomSheetBehavior.from(bottomSheet);

        // Set initial state to HIDDEN so it doesn't show until a marker is clicked
        sheetBehavior.setHideable(true);
        sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        // This is how much of the card will show when it first appears
        sheetBehavior.setPeekHeight(1750);

        // Initialize UI elements within the sheet
        tvName = findViewById(R.id.tv_detail_name);
        tvLocation = findViewById(R.id.tv_detail_location);
        tvSeparated = findViewById(R.id.tv_detail_separated);
        tvPaid = findViewById(R.id.tv_detail_paid);
        tvRating = findViewById(R.id.tv_detail_rating);
        btnReviews = findViewById(R.id.btn_show_reviews);
        btnPhotos = findViewById(R.id.btn_add_photos);
        uploadProgressBar = findViewById(R.id.upload_progress_bar);

        // Initialize the RecyclerView for displaying images
        rvPhotos = findViewById(R.id.rv_restroom_photos);
        photoAdapter = new PhotoAdapter(this, restroomPhotosList);
        rvPhotos.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvPhotos.setAdapter(photoAdapter);

        // This makes the gallery "snap" to one photo at a time
        androidx.recyclerview.widget.PagerSnapHelper snapHelper = new androidx.recyclerview.widget.PagerSnapHelper();
        snapHelper.attachToRecyclerView(rvPhotos);

        // Handle the 'Reviews' button click
        btnReviews.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedRestroomId != null) {
                    // Create the Intent to move to ReviewsActivity
                    Intent intent = new Intent(MapActivity.this, ReviewsActivity.class);

                    // Pass the restroomId so the next screen knows what to load
                    intent.putExtra("RESTROOM_ID", selectedRestroomId);

                    startActivity(intent);
                }
            }
        });

        // Handle the 'Add Photos' button click
        btnPhotos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Safety checks to ensure we have a restroom and GPS data
                if (selectedRestroomId != null && currentSelectedRestroom != null) {
                    if (ActivityCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                            if (location != null) {
                                // Check if user is within 250 meters of the current restroom's coordinates
                                boolean insideGeofence = MapPickerActivity.isWithinRadius(
                                        location.getLatitude(), location.getLongitude(),
                                        currentSelectedRestroom.getLatitude(), currentSelectedRestroom.getLongitude(),
                                        250f
                                );

                                if (insideGeofence) {
                                    openGalleryToSelectPhoto(); // Allowed!
                                } else {
                                    Toast.makeText(MapActivity.this, "You must be within 250 meters of this restroom to add photos.", Toast.LENGTH_LONG).show();
                                }
                            } else {
                                Toast.makeText(MapActivity.this, "Unable to determine your location. Please ensure GPS is turned on.", Toast.LENGTH_SHORT).show();
                            }
                        });

                    } else {
                        Toast.makeText(MapActivity.this, "Location permission is required to verify your proximity.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    // Update your Marker Click Listener to trigger the sheet
    private void setupMarkerClick() {
        mMap.setOnMarkerClickListener(marker -> {
            String restroomId = (String) marker.getTag();

            if (restroomId != null) {
                // Fetch the restroom details from Firebase
                fetchSingleRestroom(restroomId);
            }

            // Return false to allow the default behavior (centering/showing title)
            return false;
        });
    }

    // Display the details of the selected restroom
    private void displayRestroomDetails(Restroom restroom) {
        // Update what the current restroom is
        this.currentSelectedRestroom = restroom;

        selectedRestroomId = restroom.getRestroomId();

        // Update basic UI
        tvName.setText(restroom.getRestroomName());
        tvLocation.setText(restroom.getAddress());
        tvSeparated.setText(restroom.getIsSeparated() ? "Yes" : "No");
        tvPaid.setText(restroom.getIsPaid() ? "Yes" : "No");
        tvRating.setText("Rating: ★ Loading...");

        // Fetch live reviews for weighted average
        FirebaseDatabase.getInstance().getReference("Reviews").child(selectedRestroomId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<Review> reviews = new ArrayList<>();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            Review r = ds.getValue(Review.class);
                            if (r != null) reviews.add(r);
                        }

                        if (reviews.isEmpty()) {
                            tvRating.setText("Rating: ★ No reviews yet");
                        } else {
                            restroom.calcAvgRating(reviews);
                            tvRating.setText("Rating: ★ " + String.format("%.1f", restroom.getAvgRating()));
                        }
                    }
                    @Override public void onCancelled(@NonNull DatabaseError e) {}
                });

        // Stable Camera: Center on marker, then nudge
        mMap.animateCamera(CameraUpdateFactory.scrollBy(0, 250));

        // Trigger the photo fetch
        fetchRestroomPhotos(selectedRestroomId);

        sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    // Fetch the data for a specific restroom
    private void fetchSingleRestroom(String restroomId) {
        // Create a direct reference to the specific restroom ID
        DatabaseReference specificRestroomRef = FBRef.refRestrooms.child(restroomId);

        // Listener used to read the data once
        specificRestroomRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Check if the data actually exists
                if (snapshot.exists()) {
                    // Map the data directly to your Restroom class
                    Restroom restroom = snapshot.getValue(Restroom.class);

                    if (restroom != null) {
                        displayRestroomDetails(restroom);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Error fetching restroom: " + error.getMessage());
            }
        });
    }

    // Opens the gallery to select a photo
    private void openGalleryToSelectPhoto() {
        // Safety check: ensure we actually have a restroom selected before picking a photo
        if (selectedRestroomId != null) {
            // Disable the button to prevent multiple clicks
            btnPhotos.setEnabled(false);
            // This launches the system gallery picker for images
            galleryLauncher.launch("image/*");
        } else {
            Toast.makeText(this, "Please select a restroom first", Toast.LENGTH_SHORT).show();
        }
    }

    // Upload the image to Firebase Storage
    private void handleImageUpload(Uri uri) {
        // Show the progress bar
        uploadProgressBar.setVisibility(View.VISIBLE);

        // Compress the image before uploading
        byte[] compressedData = compressImage(uri);

        if (compressedData == null) {
            // Error occurred during compression
            uploadProgressBar.setVisibility(View.GONE);
            // Re-enable the button
            btnPhotos.setEnabled(true);
            Toast.makeText(this, "Image compression failed", Toast.LENGTH_SHORT).show();
            return;
        }

        String photoId = FirebaseDatabase.getInstance().getReference().push().getKey();
        StorageReference fileRef = FBRef.refStorage.child(selectedRestroomId).child(photoId + ".jpg");

        // Upload the compressed image
        fileRef.putBytes(compressedData).addOnSuccessListener(taskSnapshot -> {
            fileRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                // Save the Image to Realtime Database
                savePhotoMetadata(photoId, downloadUri.toString());
            });
        }).addOnFailureListener(e -> {
            // Hide the progress bar
            uploadProgressBar.setVisibility(View.GONE);
            // Re-enable the button
            btnPhotos.setEnabled(true);
            Toast.makeText(this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    // Save the Image to Realtime Database
    private void savePhotoMetadata(String photoId, String downloadUrl) {
        String uId = FirebaseAuth.getInstance().getUid();
        long timestamp = System.currentTimeMillis();

        Photo newPhoto = new Photo(photoId, selectedRestroomId, uId, downloadUrl, timestamp);

        FBRef.refPhotos
                .child(selectedRestroomId)
                .child(photoId)
                .setValue(newPhoto)
                .addOnSuccessListener(aVoid -> {
                    // Hide the progress bar only when EVERYTHING is done
                    uploadProgressBar.setVisibility(View.GONE);
                    // Re-enable the button
                    btnPhotos.setEnabled(true);
                    Toast.makeText(this, "Photo uploaded successfully!", Toast.LENGTH_SHORT).show();
                });
    }

    // Method for compressing an image to a max of 100KB
    private byte[] compressImage(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            Bitmap original = BitmapFactory.decodeStream(inputStream);

            int quality = 100;
            byte[] imageData;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            // Initial compression
            original.compress(Bitmap.CompressFormat.JPEG, quality, baos);
            imageData = baos.toByteArray();

            // Loop to reduce quality until size is under 100KB
            while (imageData.length > 100 * 1024 && quality > 10) {
                baos.reset(); // Clear the stream
                quality -= 10; // Reduce quality by 10%
                original.compress(Bitmap.CompressFormat.JPEG, quality, baos);
                imageData = baos.toByteArray();
            }

            return imageData;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Fetch the photos for a specific restroom
    private void fetchRestroomPhotos(String rId) {
        FirebaseDatabase.getInstance().getReference("RestroomPhotos").child(rId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        restroomPhotosList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            // Assuming your Photo.java matches the DB structure
                            Photo p = ds.getValue(Photo.class);
                            if (p != null) restroomPhotosList.add(p);
                        }
                        photoAdapter.notifyDataSetChanged();

                        // Hide the gallery if there are no photos to show
                        rvPhotos.setVisibility(restroomPhotosList.isEmpty() ? View.GONE : View.VISIBLE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError e) {
                        Log.e("Firebase", "Photo fetch failed: " + e.getMessage());
                    }
                });
    }
}