package com.example.onlycorn.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.SearchView;

import com.example.onlycorn.R;
import com.example.onlycorn.adapters.UsersAdapter;
import com.example.onlycorn.models.User;
import com.example.onlycorn.utils.FirebaseUtils;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FollowingActivity extends AppCompatActivity {
    private SearchView searchInput;
    private RecyclerView userRec;
    private UsersAdapter usersAdapter;
    private List<User> userList;
    FirebaseUser meFb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_following);
        initViews();
        loadData();


    }

    private void loadData() {
        meFb = FirebaseUtils.getUserAuth();
        userList = new ArrayList<>();
        DocumentReference followingRef = FirebaseUtils.getDocumentRef(User.FOLLOWING_COLLECTION, meFb.getUid());
        followingRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                userList.clear();
                if (documentSnapshot != null) {
                    Map<String, Object> data = documentSnapshot.getData();
                    if (data != null) {
                        for (String userId : data.keySet()) {
                            DocumentReference userRef = FirebaseUtils.getDocumentRef(User.COLLECTION, userId);
                            userRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                @Override
                                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                                    if (documentSnapshot != null) {
                                        User user = documentSnapshot.toObject(User.class);
                                        userList.add(user);

                                        usersAdapter = new UsersAdapter(FollowingActivity.this, userList, meFb.getUid());
                                        userRec.setAdapter(usersAdapter);
                                    }
                                }
                            });
                        }

                    }
                }
            }
        });
    }

    private void initViews() {
        searchInput = findViewById(R.id.search_view_users);
        userRec = findViewById(R.id.user_recycleView);
        userRec.setHasFixedSize(true);
        userRec.setLayoutManager(new LinearLayoutManager(this));
    }
}