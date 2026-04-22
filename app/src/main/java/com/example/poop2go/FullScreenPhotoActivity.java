package com.example.poop2go;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class FullScreenPhotoActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_photo);

        ImageView ivFull = findViewById(R.id.iv_full_photo);
        TextView tvName = findViewById(R.id.tv_uploader_name);

        String url = getIntent().getStringExtra("PHOTO_URL");
        String uId = getIntent().getStringExtra("UPLOADER_UID");

        Glide.with(this).load(url).into(ivFull);

        // Fetch user name
        FirebaseDatabase.getInstance().getReference("Users").child(uId).child("name")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot s) {
                        String name = s.getValue(String.class);
                        tvName.setText("Uploaded by: " + (name != null ? name : "Unknown"));
                    }
                    @Override public void onCancelled(@NonNull DatabaseError e) {}
                });
    }
}