package com.example.poop2go;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {
    private List<Review> reviewsList;
    private Map<String, String> nameCache = new HashMap<>(); // Prevents redundant lookups

    public ReviewAdapter(List<Review> reviewsList) {
        this.reviewsList = reviewsList;
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_review, parent, false);
        return new ReviewViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        Review review = reviewsList.get(position);
        holder.tvRating.setText("Rating: ★ " + review.getRating());
        holder.tvComment.setText(review.getComment());

        // Name Lookup logic
        String uId = review.getUId();
        if (nameCache.containsKey(uId)) {
            holder.tvName.setText(nameCache.get(uId));
        } else {
            holder.tvName.setText("Loading...");
            FirebaseDatabase.getInstance().getReference("Users").child(uId).child("name")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot s) {
                            String name = s.getValue(String.class);
                            name = (name == null) ? "Unknown User" : name;
                            nameCache.put(uId, name);
                            holder.tvName.setText(name);
                        }
                        @Override public void onCancelled(@NonNull DatabaseError e) {}
                    });
        }
    }

    @Override public int getItemCount() { return reviewsList.size(); }

    public static class ReviewViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvRating, tvComment;
        public ReviewViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_reviewer_name);
            tvRating = itemView.findViewById(R.id.tv_review_rating);
            tvComment = itemView.findViewById(R.id.tv_review_content);
        }
    }
}