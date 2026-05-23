package com.example.poop2go;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder> {
    private List<Photo> photoList; //The list of photos
    private Context context; //The context

    public PhotoAdapter(Context context, List<Photo> photoList) {
        //Builder
        this.context = context;
        this.photoList = photoList;
    }

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflates the item layout
        View v = LayoutInflater.from(context).inflate(R.layout.item_photo, parent, false);
        return new PhotoViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
        Photo photo = photoList.get(position);

        // Loads the image using Glide
        Glide.with(context)
                .load(photo.getDownloadUrl())
                .centerCrop()
                .into(holder.ivPhoto);

        /* Sets the onClickListener for each photo,
        which sends You to a full-screen photo activity */
        holder.itemView.setOnClickListener(v -> {
            var intent = new Intent(context, FullScreenPhotoActivity.class);
            intent.putExtra("PHOTO_URL", photo.getDownloadUrl());
            intent.putExtra("UPLOADER_UID", photo.getUId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() { return photoList.size(); } // Returns the number of photos

    public static class PhotoViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPhoto;
        public PhotoViewHolder(View itemView) {
            super(itemView);
            ivPhoto = itemView.findViewById(R.id.iv_item_photo);
        }
    }
}