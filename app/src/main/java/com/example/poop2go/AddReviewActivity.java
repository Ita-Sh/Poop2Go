package com.example.poop2go;

import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

public class AddReviewActivity extends AppCompatActivity {
    private RatingBar rb;
    private EditText et;
    private String restroomId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_review);

        restroomId = getIntent().getStringExtra("RESTROOM_ID");
        rb = findViewById(R.id.rb_new_rating);
        et = findViewById(R.id.et_review_content);

        findViewById(R.id.btn_confirm_review).setOnClickListener(v -> submit());
        findViewById(R.id.btn_cancel_review).setOnClickListener(v -> finish());
    }

    private void submit() {
        String comment = et.getText().toString().trim();
        int rating = (int) rb.getRating();

        if (rating == 0 || comment.isEmpty()) return;

        // Word count check
        if (comment.split("\\s+").length > 100) {
            et.setError("Max 100 words allowed");
            return;
        }

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Reviews").child(restroomId);
        String rId = ref.push().getKey();
        String uId = FirebaseAuth.getInstance().getUid();

        Review r = new Review(rId, restroomId, uId, rating, comment, System.currentTimeMillis());
        ref.child(rId).setValue(r).addOnSuccessListener(aVoid -> finish());
    }
}