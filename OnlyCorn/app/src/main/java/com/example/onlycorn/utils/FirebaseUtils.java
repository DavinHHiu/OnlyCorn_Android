package com.example.onlycorn.utils;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class FirebaseUtils {
    public static CollectionReference getCollectionRef(String collection) {
        return FirebaseFirestore.getInstance().collection(collection);
    }

    public static DocumentReference getDocumentRef(String collection, String document) {
        return FirebaseFirestore.getInstance().collection(collection).document(document);
    }

    public static StorageReference getStorageRef(String url) {
        return FirebaseStorage.getInstance().getReference(url);
    }

    public static StorageReference getStorageRefFromUrl(String url) {
        return FirebaseStorage.getInstance().getReferenceFromUrl(url);
    }

    public static FirebaseUser getUserAuth() {
        return FirebaseAuth.getInstance().getCurrentUser();
    }
}
