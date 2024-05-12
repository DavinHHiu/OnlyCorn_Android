package com.example.onlycorn.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.onlycorn.activities.AddPostActivity;
import com.example.onlycorn.activities.EditProfileActivity;
import com.example.onlycorn.activities.FollowerActivity;
import com.example.onlycorn.activities.FollowingActivity;
import com.example.onlycorn.activities.LoginActivity;
import com.example.onlycorn.R;
import com.example.onlycorn.activities.PostDetailActivity;
import com.example.onlycorn.adapters.PostAdapter;
import com.example.onlycorn.models.Post;
import com.example.onlycorn.models.User;
import com.example.onlycorn.utils.FirebaseUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class ProfileFragment extends Fragment {
    private ImageView avatarIv;
    private ImageButton moreButton;
    private TextView usernameTv, nameTv;
    private TextView posts;
    private TextView followers;
    private TextView followings;
    private LinearLayout followingBtn, followerBtn;
    private Button editButton;
    private RecyclerView recyclerViewPosts;

    private FirebaseUser authUser;
    private FirebaseAuth firebaseAuth;

    private User user;
    private Context context;
    private List<Post> postList;
    private PostAdapter adapter;

    public ProfileFragment(Context context) {
        this.context = context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.profile_fragment, container, false);
        initViews(view);
        loadData();

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, EditProfileActivity.class);
                startActivity(intent);
            }
        });
        followingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(context, FollowingActivity.class));
            }
        });
        followers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(context, FollowerActivity.class));
            }
        });

        return view;
    }

    private void loadData() {
        firebaseAuth = FirebaseAuth.getInstance();
        authUser = FirebaseUtils.getUserAuth();
        postList = new ArrayList<>();
        loadUserInfo();
        loadMyPosts();
        loadFollow();
    }

    private void loadFollow() {
        DocumentReference followerRef = FirebaseUtils.getDocumentRef(User.FOLLOWER_COLLECTION, authUser.getUid());
        followerRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (documentSnapshot != null) {
                    Map<String, Object> data = documentSnapshot.getData();
                    if (data != null) {
                        int follower = data.size();
                        followers.setText(String.valueOf(follower));
                    }
                }
            }
        });

        DocumentReference followingRef = FirebaseUtils.getDocumentRef(User.FOLLOWING_COLLECTION, authUser.getUid());
        followingRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (documentSnapshot != null) {
                    Map<String, Object> data = documentSnapshot.getData();
                    if (data != null) {
                        int following = data.size();
                        followings.setText(String.valueOf(following));
                    }
                }
            }
        });
    }

    private void loadUserInfo() {
        DocumentReference userRef = FirebaseUtils.getDocumentRef(User.COLLECTION, authUser.getUid());
        userRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (documentSnapshot != null) {
                    user = documentSnapshot.toObject(User.class);

                    if (user != null) {
                        usernameTv.setText(user.getUsername());
                        nameTv.setText(user.getName());

                        try {
                            Glide.with(context).load(Uri.parse(user.getImage()))
                                    .apply(RequestOptions.circleCropTransform()).into(avatarIv);
                        } catch (Exception ex) {
                            System.out.println(ex.getMessage());
                        }
                    }
                }
            }
        });
    }

    private void loadMyPosts() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        recyclerViewPosts.setLayoutManager(layoutManager);

        postList = new ArrayList<>();

        CollectionReference ref = FirebaseUtils.getCollectionRef(Post.COLLECTION);
        Query query = ref.orderBy("uid").whereEqualTo("uid", authUser.getUid());
        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                postList.clear();
                for (QueryDocumentSnapshot qds : queryDocumentSnapshots) {
                    Post post = qds.toObject(Post.class);

                    postList.add(post);
                    adapter = new PostAdapter(context, postList);
                    recyclerViewPosts.setAdapter(adapter);
                }
                posts.setText(String.valueOf(postList.size()));
            }
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        checkUserStatus();
    }

    private void checkUserStatus() {
        if (authUser != null) {
        } else {
            startActivity(new Intent(getActivity(), LoginActivity.class));
            if (getActivity() != null) {
                getActivity().finish();
            }
        }
    }

    private void initViews(View view) {
        avatarIv = view.findViewById(R.id.avatarIv);
        usernameTv = view.findViewById(R.id.usernameTv);
        nameTv = view.findViewById(R.id.nameTv);
        posts = view.findViewById(R.id.posts);
        followings = view.findViewById(R.id.followings);
        followers = view.findViewById(R.id.followers);
        editButton = view.findViewById(R.id.editButton);
        recyclerViewPosts = view.findViewById(R.id.recycleViewPosts);

        moreButton = view.findViewById(R.id.moreButton);
        followingBtn = view.findViewById(R.id.followingButton);
        followerBtn = view.findViewById(R.id.followerButton);

        moreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMoreOptions(moreButton);
            }
        });
    }

    private void showMoreOptions(ImageButton moreButton) {
        PopupMenu popupMenu = new PopupMenu(context, moreButton, Gravity.END);

        popupMenu.getMenu().add(Menu.NONE, 0, 0, "Đăng xuất");
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == 0) {
                    firebaseAuth.signOut();
                    checkUserStatus();
                }
                return false;
            }
        });

        popupMenu.show();
    }
}
