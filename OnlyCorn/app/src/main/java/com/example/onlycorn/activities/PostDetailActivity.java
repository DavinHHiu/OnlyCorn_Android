package com.example.onlycorn.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.onlycorn.R;
import com.example.onlycorn.models.Post;
import com.example.onlycorn.utils.FirebaseUtils;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class PostDetailActivity extends AppCompatActivity {
    private ImageView avatarIv, postImage;
    private TextView usernameTv, timestampTv, captionTv, descriptionTv, likesTv;
    private ImageButton moreButton, likeButton,  shareButton;
    private LinearLayout profileLayout;

    private ImageView  myAvatarIv;
    private EditText commentEt;
    private ImageButton sendButton;

    private Post post;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        initViews();
        initData();
    }

    private void initData() {
        String postId = getIntent().getStringExtra("postId");
        DocumentReference postRef = FirebaseUtils.getDocumentRef(Post.COLLECTION, postId);
        postRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (documentSnapshot != null) {
                    post = documentSnapshot.toObject(Post.class);

                    if (post != null) {
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(Long.parseLong(post.getTimeStamp()));
                        @SuppressLint("SimpleDateFormat") String time = new SimpleDateFormat("dd/MM/yyyy hh:mm aa").format(calendar);

                        captionTv.setText(post.getCaption());
                        descriptionTv.setText(post.getDescription());
                        usernameTv.setText(post.getUsername());
                        likesTv.setText(String.format("%s Likes", post.getLikes()));
                        timestampTv.setText(time);

                        if (post.getImage().equals("noImage")) {
                            postImage.setVisibility(View.GONE);
                        } else {
                            postImage.setVisibility(View.VISIBLE);
                            try {
                                Picasso.get().load(post.getImage()).into(postImage);
                            } catch (Exception ex) {
                            }
                        }

                        try {
                            Picasso.get().load(post.getUserAva()).into(avatarIv);
                        } catch (Exception ex) {
                        }
                    }
                }
            }
        });
    }

    private void initViews() {
        avatarIv = findViewById(R.id.avatarIv);
        postImage = findViewById(R.id.postImage);
        usernameTv = findViewById(R.id.usernameTv);
        timestampTv = findViewById(R.id.timeStampTv);
        captionTv = findViewById(R.id.captionTv);
        descriptionTv = findViewById(R.id.descriptionTv);
        likesTv = findViewById(R.id.likes);
        moreButton = findViewById(R.id.moreButton);
        likeButton = findViewById(R.id.likeButton);
        shareButton = findViewById(R.id.shareButton);
        profileLayout = findViewById(R.id.profileLayout);
        myAvatarIv = findViewById(R.id.myAvatarIv);
        commentEt = findViewById(R.id.commentEt);
        sendButton = findViewById(R.id.sendButton);
    }
}