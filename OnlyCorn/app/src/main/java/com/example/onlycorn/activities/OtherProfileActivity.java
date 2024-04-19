package com.example.onlycorn.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.onlycorn.R;
import com.example.onlycorn.adapters.PostAdapter;
import com.example.onlycorn.models.Post;
import com.example.onlycorn.models.User;
import com.example.onlycorn.utils.FirebaseUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class OtherProfileActivity extends AppCompatActivity {
    private ImageView avatarIv;
    private TextView usernameTv;
    private TextView following;
    private TextView followers;
    private RecyclerView recyclerViewPosts;

    private FirebaseUser authUser;

    private String uid;
    private List<Post> postList;
    private PostAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_profile);

        initViews();
        initData();
        loadOtherPosts();
    }

    private void loadOtherPosts() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        recyclerViewPosts.setLayoutManager(layoutManager);

        postList = new ArrayList<>();

        CollectionReference ref = FirebaseUtils.getCollectionRef(Post.COLLECTION);
        Query query = ref.orderBy("uid").whereEqualTo("uid", uid);
        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                postList.clear();
                for (QueryDocumentSnapshot qds : queryDocumentSnapshots) {
                    Post post = qds.toObject(Post.class);

                    postList.add(post);
                    adapter = new PostAdapter(OtherProfileActivity.this, postList);
                    recyclerViewPosts.setAdapter(adapter);
                }
            }
        });
    }

    private void initData() {
        authUser = FirebaseUtils.getUserAuth();
        postList = new ArrayList<>();
        uid = getIntent().getStringExtra("uid");
        DocumentReference docRef = FirebaseUtils.getDocumentRef(User.COLLECTION, authUser.getUid());
        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (documentSnapshot != null) {
                    String username = documentSnapshot.getString("username");
                    usernameTv.setText(username);
                }
            }
        });
    }

    private void initViews() {
        avatarIv = findViewById(R.id.avatarIv);
        usernameTv = findViewById(R.id.usernameTv);
        following = findViewById(R.id.following);
        followers = findViewById(R.id.followers);
        recyclerViewPosts = findViewById(R.id.recycleViewPosts);
    }
}