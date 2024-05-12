package com.example.onlycorn.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;

import com.example.onlycorn.R;
import com.example.onlycorn.adapters.UsersAdapter;
import com.example.onlycorn.models.Like;
import com.example.onlycorn.models.Post;
import com.example.onlycorn.models.User;
import com.example.onlycorn.utils.FirebaseUtils;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PostLikeByActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private UsersAdapter usersAdapter;

    private String postId;
    private List<User> userList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_like_by);

        initViews();
        initData();
    }

    private void initData() {
        Intent intent = getIntent();
        postId = intent.getStringExtra("postId");
        userList = new ArrayList<>();

        DocumentReference likeRef = FirebaseUtils.getDocumentRef(Like.COLLECTION, postId);
        likeRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot,
                                @Nullable FirebaseFirestoreException e) {
                userList.clear();
                if (documentSnapshot != null) {
                    for (Map.Entry<String, Object> entry : documentSnapshot.getData().entrySet()) {
                        String otherId = entry.getKey();

                        getUser(otherId);
//                        usersAdapter = new UsersAdapter(PostLikeByActivity.this, userList);
//                        recyclerView.setAdapter(usersAdapter);
                    }
                }
            }
        });
    }

    private void getUser(String otherId) {
        DocumentReference userRef = FirebaseUtils.getDocumentRef(User.COLLECTION, otherId);
        userRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (documentSnapshot != null) {
                    User user = documentSnapshot.toObject(User.class);
                    userList.add(user);
                }
            }
        });
    }

    private void initViews() {
        recyclerView = findViewById(R.id.userslikeRecView);
    }
}