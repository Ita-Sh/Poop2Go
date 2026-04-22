package com.example.poop2go;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class FBRef {
    public static FirebaseAuth refAuth = FirebaseAuth.getInstance();

    public static FirebaseDatabase FBDB = FirebaseDatabase.getInstance();
    public static DatabaseReference refUsers = FBDB.getReference("Users");
    public static DatabaseReference refRestrooms = FBDB.getReference("Restrooms");
    public static DatabaseReference refReviews = FBDB.getReference("Reviews");
    public static DatabaseReference refPhotos = FBDB.getReference("RestroomPhotos");


    public static FirebaseStorage FBStorage = FirebaseStorage.getInstance();
    public static StorageReference refStorage = FBStorage.getReference();


}
