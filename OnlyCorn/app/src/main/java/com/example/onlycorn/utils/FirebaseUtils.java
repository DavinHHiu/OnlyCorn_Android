package com.example.onlycorn.utils;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

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

    public static void callApi(JSONObject jsonObject) {
        OkHttpClient client = new OkHttpClient();
        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        String url = "https://fcm.googleapis.com/fcm/send";
        RequestBody body = RequestBody.create(jsonObject.toString(), JSON);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .header("Authorization", "Bearer AAAAcacbC9M:APA91bF0S8IJZfAalBup3iwCFU0w93ojLxWNwxmysc1myhyLNDo1xw2fy054RVSwA0N7oYYK7STXfZCOyu7J5qzlqNXlBhAbLl_7f_c8PhwWdWenpkmgfuigSo6PZtq9eitCexX6WUoT")
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull okhttp3.Response response) throws IOException {

            }
        });
    }
}
