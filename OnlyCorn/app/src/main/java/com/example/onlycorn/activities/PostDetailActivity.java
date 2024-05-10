package com.example.onlycorn.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.example.onlycorn.R;
import com.example.onlycorn.adapters.CommentAdapter;
import com.example.onlycorn.models.Comment;
import com.example.onlycorn.models.Like;
import com.example.onlycorn.models.Post;
import com.example.onlycorn.models.User;
import com.example.onlycorn.utils.FirebaseUtils;
import com.example.onlycorn.utils.Pop;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PostDetailActivity extends AppCompatActivity {
    private ImageView avatarIv, postImage;
    private TextView usernameTv, timestampTv, captionTv, descriptionTv, likesTv, commentTv;
    private ImageButton moreButton, likeButton, shareButton;
    private LinearLayout profileLayout;
    private RecyclerView commentRecView;

    private ImageView  myAvatarIv;
    private EditText commentEt;
    private ImageButton sendButton;

    private ProgressDialog pd;
    private boolean processComment = false;
    private boolean processLike = false;
    private Post post;
    private User user;
    private List<Comment> listComments;
    private CommentAdapter commentAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        initViews();
        initData();
    }

    private void setLikes(String postId) {
        DocumentReference likesRef = FirebaseUtils.getDocumentRef(Like.COLLECTION, postId);
        likesRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (documentSnapshot != null && documentSnapshot.contains(user.getUserId())) {
                    likeButton.setImageResource(R.drawable.icon_heart_filled);
                } else {
                    likeButton.setImageResource(R.drawable.icon_heart);
                }
            }
        });
    }

    private void showMoreOptions() {
        PopupMenu popupMenu = new PopupMenu(this, moreButton, Gravity.END);

        if (post.getUid().equals(user.getUserId())) {
            popupMenu.getMenu().add(Menu.NONE, 0, 0, "Delete");
            popupMenu.getMenu().add(Menu.NONE, 1, 0, "Edit");
        }
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == 0) {
                    beginDelete();
                } else if (id == 1) {
                    Intent intent = new Intent(PostDetailActivity.this, AddPostActivity.class);
                    intent.putExtra("key", "editPost");
                    intent.putExtra("postId", post.getPostId());
                    startActivity(intent);
                } else if (id == 2) {
                    Intent intent = new Intent(PostDetailActivity.this, PostDetailActivity.class);
                    intent.putExtra("postId", post.getPostId());
                    startActivity(intent);
                }
                return false;
            }
        });

        popupMenu.show();
    }

    private void beginDelete() {
        if (post.getImage().equals("noImage")) {
            deleteWithoutImage();
        } else {
            deleteWithImage();
        }
    }

    private void deleteWithImage() {
        ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Deleting...");
        pd.show();

        StorageReference ref = FirebaseUtils.getStorageRefFromUrl(post.getImage());
        ref.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Query query = FirebaseUtils.getCollectionRef(Post.COLLECTION)
                                .orderBy("postId").whereEqualTo("postId", post.getPostId());
                        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
                            @Override
                            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                                for(QueryDocumentSnapshot qds : queryDocumentSnapshots) {
                                    qds.getReference().delete();
                                }
                                pd.dismiss();
                                Pop.pop(PostDetailActivity.this, "Delete successfully");
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pd.dismiss();
                        Pop.pop(PostDetailActivity.this, e.getMessage());
                    }
                });
    }

    private void deleteWithoutImage() {
        ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Deleting...");

        Query query = FirebaseUtils.getCollectionRef(Post.COLLECTION)
                .orderBy("postId").whereEqualTo("postId", post.getPostId());
        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                for(QueryDocumentSnapshot qds : queryDocumentSnapshots) {
                    qds.getReference().delete();
                }
                Pop.pop(PostDetailActivity.this, "Delete successfully");
            }
        });
    }

    private void likePost() {
        int likes = Integer.parseInt(post.getLikes());
        processLike = true;
        CollectionReference likesRef = FirebaseUtils.getCollectionRef(Like.COLLECTION);
        CollectionReference postsRef = FirebaseUtils.getCollectionRef(Post.COLLECTION);
        String postId = post.getPostId();
        String uId = user.getUserId();
        likesRef.document(postId).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (!documentSnapshot.contains("likes_count")) {
                            Map<String, Object> initLikeCount = new HashMap<>();
                            initLikeCount.put("likes_count", "0");
                            likesRef.document(postId).set(initLikeCount, SetOptions.merge());
                        }
                        if (documentSnapshot.contains(uId) && processLike) {
                            int likes_count = Integer.parseInt((String) documentSnapshot.get("likes_count"));
                            Map<String, Object> removeUserLike = new HashMap<>();
                            removeUserLike.put(uId, FieldValue.delete());
                            removeUserLike.put("likes_count", String.valueOf(likes_count - 1));
                            likesRef.document(postId).update(removeUserLike);
                            processLike = false;

                            likeButton.setImageResource(R.drawable.icon_heart);
                        } else {
                            int likes_count = Integer.parseInt((String) documentSnapshot.get("likes_count"));
                            Map<String, Object> newUserLike = new HashMap<>();
                            newUserLike.put(uId, "Liked");
                            newUserLike.put("likes_count", String.valueOf(likes_count + 1));
                            likesRef.document(postId).set(newUserLike, SetOptions.merge());

                            likeButton.setImageResource(R.drawable.icon_heart_filled);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    }
                });
    }

    private void postComment() {
        pd = new ProgressDialog(this);
        pd.setTitle("Adding comment...");

        String myComment = commentEt.getText().toString();
        if (TextUtils.isEmpty(myComment)) {
            Pop.pop(this, "Nothing to comment");
            return;
        }
        String timestamp = String.valueOf(System.currentTimeMillis());
        DocumentReference commentRef = FirebaseUtils.getDocumentRef(Comment.COLLECTION, post.getPostId());
        HashMap<String, Object> userComment = new HashMap<>();
        Comment comment = new Comment(timestamp, post.getPostId(), user.getUserId(), user.getUsername(),
                user.getImage(), timestamp, myComment);
        userComment.put(user.getUserId() + "_" + timestamp, comment);
        commentRef.update(userComment)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        pd.dismiss();
                        commentEt.setText("");
                        Pop.pop(PostDetailActivity.this, "Comment added");
                        updateCommentCount();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pd.dismiss();
                        Pop.pop(PostDetailActivity.this, e.getMessage());
                    }
                });
    }

    private void updateCommentCount() {
        processComment = true;
        DocumentReference postRef = FirebaseUtils.getDocumentRef(Post.COLLECTION, post.getPostId());
        postRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (documentSnapshot != null && processComment) {
                    String comments = documentSnapshot.getString("comments");
                    int newComments = Integer.parseInt(comments) + 1;
                    documentSnapshot.getReference().update("comments", String.valueOf(newComments));
                    commentTv.setText(String.format("%s comments", newComments));
                    processComment = false;
                }
            }
        });
    }

    private void initData() {
        loadPost();
        loadUser();
    }

    private void loadPost() {
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
                        @SuppressLint("SimpleDateFormat") String time =
                                new SimpleDateFormat("dd/MM/yyyy hh:mm aa").format(calendar.getTime());

                        captionTv.setText(post.getCaption());
                        usernameTv.setText(post.getUsername());
                        DocumentReference likeRef = FirebaseUtils.getDocumentRef(Like.COLLECTION, post.getPostId());
                        likeRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                            @Override
                            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                                if (documentSnapshot != null) {
                                    if (!documentSnapshot.contains("likes_count")) {
                                        Map<String, Object> initLikeCount = new HashMap<>();
                                        initLikeCount.put("likes_count", "0");
                                        likeRef.set(initLikeCount, SetOptions.merge());
                                    }
                                    String likes = documentSnapshot.getString("likes_count");
                                    likesTv.setText(String.format("%s Likes", likes));
                                }
                            }
                        });
                        commentTv.setText(String.format("%s Comments", post.getComments()));
                        timestampTv.setText(time);

                        if (post.getImage().equals("noImage")) {
                            postImage.setVisibility(View.GONE);
                        } else {
                            postImage.setVisibility(View.VISIBLE);
                            try {
                                Picasso.get().load(post.getImage()).into(postImage);
                            } catch (Exception ex) {
                                Picasso.get().load(post.getImage()).into(postImage);
                            }
                        }
                        try {
                            Picasso.get().load(post.getUserAva()).into(avatarIv);
                        } catch (Exception ex) {
                            Picasso.get().load(post.getUserAva()).into(avatarIv);
                        }
                        setLikes(post.getPostId());
                        loadComments(post.getPostId());
                    }
                }
            }
        });
    }

    private void loadUser() {
        String uid = FirebaseUtils.getUserAuth().getUid();
        DocumentReference userRef = FirebaseUtils.getDocumentRef(User.COLLECTION, uid);
        userRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (documentSnapshot != null) {
                    user = documentSnapshot.toObject(User.class);

                    if (user != null) {
                        try {
                            Picasso.get().load(user.getImage()).placeholder(R.drawable.corn_svgrepo_com).into(myAvatarIv);
                        } catch (Exception ex) {
                            Picasso.get().load(user.getImage()).placeholder(R.drawable.corn_svgrepo_com).into(myAvatarIv);
                        }
                    }
                }
            }
        });
    }

    private void loadComments(String postId) {
        listComments = new ArrayList<>();
        DocumentReference commentRef = FirebaseUtils.getDocumentRef(Comment.COLLECTION, postId);
        commentRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                listComments.clear();
                if (documentSnapshot != null) {
                    Map<String, Object> fields = documentSnapshot.getData();
                    for (Map.Entry<String, Object> entry: fields.entrySet()) {
                        Comment comment = Comment.convertFromMap((Map<String, Object>)entry.getValue());
                        listComments.add(comment);
                        commentAdapter = new CommentAdapter(PostDetailActivity.this, listComments, user.getUserId(), post.getPostId());
                        commentRecView.setAdapter(commentAdapter);
                    }
                }
            }
        });
    }

    private Uri saveImageToShare(Bitmap bitmap) {
        File imageFolder = new File(getCacheDir(), "images");
        Uri uri = null;
        try {
            imageFolder.mkdirs();
            File file = new File(imageFolder, "shared_image.png");
            FileOutputStream stream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream);
            stream.flush();
            stream.close();
            uri = FileProvider.getUriForFile(this, "com.example.onlycorn", file);
        } catch (Exception e) {
            Pop.pop(this, e.getMessage());
        }
        return uri;
    }

    private void shareImageAndText(String caption, String desc, Bitmap bitmap) {
        String shareBody = caption + "\n" + desc;

        Uri uri = saveImageToShare(bitmap);

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("image/png");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject here");
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
        startActivity(Intent.createChooser(shareIntent, "Share Via"));
    }

    private void shareTextOnly(String caption, String desc) {
        String shareBody = caption + "\n" + desc;

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("textplain/");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject Here");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
        startActivity(Intent.createChooser(shareIntent, "Share Via"));
    }

    private void initViews() {
        avatarIv = findViewById(R.id.avatarIv);
        postImage = findViewById(R.id.postImage);
        usernameTv = findViewById(R.id.usernameTv);
        timestampTv = findViewById(R.id.timeStampTv);
        captionTv = findViewById(R.id.captionTv);
        descriptionTv = findViewById(R.id.descriptionTv);
        likesTv = findViewById(R.id.likes);
        commentTv = findViewById(R.id.comments);
        moreButton = findViewById(R.id.moreButton);
        likeButton = findViewById(R.id.likeButton);
        shareButton = findViewById(R.id.shareButton);
        profileLayout = findViewById(R.id.profileLayout);
        commentRecView = findViewById(R.id.commentRecView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        commentRecView.setLayoutManager(layoutManager);

        myAvatarIv = findViewById(R.id.myAvatarIv);
        commentEt = findViewById(R.id.commentEt);
        sendButton = findViewById(R.id.sendButton);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postComment();
            }
        });

        likeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                likePost();
            }
        });

        moreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMoreOptions();
            }
        });
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String caption = captionTv.getText().toString();
                String desc = descriptionTv.getText().toString();

                BitmapDrawable bitmapDrawable = (BitmapDrawable)postImage.getDrawable();
                if (bitmapDrawable == null) {
                    shareTextOnly(caption, desc);
                } else {
                    Bitmap bitmap = bitmapDrawable.getBitmap();
                    shareImageAndText(caption, desc, bitmap);
                }
            }
        });
        likesTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PostDetailActivity.this, PostLikeByActivity.class);
                intent.putExtra("postId", post.getPostId());
                startActivity(intent);
            }
        });
    }
}