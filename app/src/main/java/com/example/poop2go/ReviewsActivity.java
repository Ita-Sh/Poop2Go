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
    private RecyclerView rv;
    private ReviewAdapter adapter;
    private List<Review> list = new ArrayList<>();
    private TextView tvEmpty;
    private String restroomId;
    private Button btnNewReview, btnBack;

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

        rv = findViewById(R.id.rv_reviews_list);
        tvEmpty = findViewById(R.id.tv_no_reviews);
        btnNewReview = findViewById(R.id.btn_add_review);
        btnBack = findViewById(R.id.btn_back);

        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ReviewAdapter(list);
        rv.setAdapter(adapter);

        btnNewReview.setOnClickListener(v -> {
            // Create the Intent to move to AddReviewActivity
            Intent intent = new Intent(ReviewsActivity.this, AddReviewActivity.class);
            intent.putExtra("RESTROOM_ID", restroomId);
            startActivity(intent);
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