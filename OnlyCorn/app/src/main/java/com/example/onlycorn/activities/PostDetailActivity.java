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

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.onlycorn.R;
import com.example.onlycorn.adapters.CommentAdapter;
import com.example.onlycorn.models.Comment;
import com.example.onlycorn.models.Like;
import com.example.onlycorn.models.Post;
import com.example.onlycorn.models.User;
import com.example.onlycorn.utils.FirebaseUtils;
import com.example.onlycorn.utils.Pop;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.PlayerView;
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

import org.json.JSONObject;

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
    private TextView usernameTv, timestampTv, captionTv, likesTv, commentTv;
    private ImageButton moreButton, likeButton, shareButton;
    private LinearLayout profileLayout;
    private RecyclerView commentRecView;
    private PlayerView playerView;

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
        processLike = true;
        CollectionReference likesRef = FirebaseUtils.getCollectionRef(Like.COLLECTION);
        String postId = post.getPostId();
        String uId = user.getUserId();
        likesRef.document(postId).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.contains(uId) && processLike) {
                            Map<String, Object> removeUserLike = new HashMap<>();
                            removeUserLike.put(uId, FieldValue.delete());
                            likesRef.document(postId).update(removeUserLike);
                            processLike = false;
                        } else {
                            Map<String, Object> newUserLike = new HashMap<>();
                            newUserLike.put(uId, "Liked");
                            likesRef.document(postId).set(newUserLike, SetOptions.merge());
                            sendLikeNotification();
                            processLike = false;
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    }
                });
    }

    private void sendLikeNotification() {
        String postOwnerId = post.getUid();
        FirebaseUtils.getDocumentRef(User.COLLECTION, postOwnerId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (documentSnapshot != null) {
                    String fcmToken = documentSnapshot.getString("fcmToken");
                    if (fcmToken != null) {
                        try{
                            JSONObject jsonObject = new JSONObject();

                            JSONObject notificationObj = new JSONObject();
                            notificationObj.put("title", "Only Corn");
                            notificationObj.put("body", String.format("%s vừa thích bài viết của bạn.", user.getUsername()));

                            JSONObject dataObj = new JSONObject();
                            dataObj.put("postId", post.getPostId());

                            jsonObject.put("notification", notificationObj);
                            jsonObject.put("data", dataObj);
                            jsonObject.put("to", fcmToken);

                            FirebaseUtils.callApi(jsonObject);
                        } catch (Exception ex) {

                        }
                    }
                }
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
        commentRef.set(userComment)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        pd.dismiss();
                        commentEt.setText("");
                        sendCommentNotification();
                        Pop.pop(PostDetailActivity.this, "Comment added");
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

    private void sendCommentNotification() {
        String postOwnerId = post.getUid();
        FirebaseUtils.getDocumentRef(User.COLLECTION, postOwnerId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (documentSnapshot != null) {
                    String fcmToken = documentSnapshot.getString("fcmToken");
                    if (fcmToken != null) {
                        try{
                            JSONObject jsonObject = new JSONObject();

                            JSONObject notificationObj = new JSONObject();
                            notificationObj.put("title", "Only Corn");
                            notificationObj.put("body", String.format("%s vừa bình luận bài viết của bạn.", user.getUsername()));

                            JSONObject dataObj = new JSONObject();
                            dataObj.put("postId", post.getPostId());

                            jsonObject.put("notification", notificationObj);
                            jsonObject.put("data", dataObj);
                            jsonObject.put("to", fcmToken);

                            FirebaseUtils.callApi(jsonObject);
                        } catch (Exception ex) {

                        }
                    }
                }
            }
        });
    }

    private void initData() {
        String uid = FirebaseUtils.getUserAuth().getUid();
        loadPost();
        loadUser(uid, myAvatarIv, null, true);
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

                        if ("noMedia".equals(post.getType())) {
                            postImage.setVisibility(View.GONE);
                            playerView.setVisibility(View.GONE);
                        } if ("image".equals(post.getType())) {
                            playerView.setVisibility(View.GONE);
                            postImage.setVisibility(View.VISIBLE);

                            postImage.setMinimumHeight(470);
                            postImage.setMaxHeight(470);
                            try {
                                Picasso.get().load(post.getImage()).placeholder(R.drawable.corn_svgrepo_com).into(postImage);
                            } catch (Exception ex) {
                            }
                        } else if ("video".equals(post.getType())) {
                            playerView.setVisibility(View.VISIBLE);
                            postImage.setVisibility(View.GONE);
                            SimpleExoPlayer player = new SimpleExoPlayer.Builder(PostDetailActivity.this).build();
                            player.setMediaItem(MediaItem.fromUri(Uri.parse(post.getImage())));
                            player.prepare();
                            player.play();

                            playerView.setPlayer(player);
                        }
                        loadUser(post.getUid(), avatarIv, usernameTv, false);
                        loadComments(post.getPostId());
                        loadLikes(post.getPostId());
                    }
                }
            }
        });
    }

    private void loadLikes(String postId) {
        DocumentReference likeRef = FirebaseUtils.getDocumentRef(Like.COLLECTION, postId);
        likeRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (documentSnapshot != null) {
                    Map<String, Object> data = documentSnapshot.getData();
                    if (data != null) {
                        likesTv.setText(String.format("%s lượt thích", data.size()));
                    }
                    if (documentSnapshot.contains(user.getUserId())) {
                        likeButton.setImageResource(R.drawable.icon_heart_filled);
                    } else {
                        likeButton.setImageResource(R.drawable.icon_heart);
                    }
                }
            }
        });
    }

    private void loadUser(String userId, ImageView userPhoto, TextView userName, boolean saveGlobal) {
        DocumentReference userRef = FirebaseUtils.getDocumentRef(User.COLLECTION, userId);
        userRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (documentSnapshot != null) {
                    User userDB = documentSnapshot.toObject(User.class);

                    if (userDB != null) {
                        if (userName != null) {
                            userName.setText(userDB.getUsername());
                        }
                        try {
                            Glide.with(getApplicationContext()).load(Uri.parse(userDB.getImage()))
                                    .apply(RequestOptions.circleCropTransform()).into(userPhoto);
                        } catch (Exception ex) {
                            System.out.println(ex.getMessage());
                        }
                    }
                    if (saveGlobal) {
                        user = userDB;
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
                    if (fields != null) {
                        commentTv.setText(String.format("%s bình luận", fields.size()));
                        for (Map.Entry<String, Object> entry: fields.entrySet()) {
                            Comment comment = Comment.convertFromMap((Map<String, Object>)entry.getValue());
                            listComments.add(comment);
                            commentAdapter = new CommentAdapter(PostDetailActivity.this, listComments, user.getUserId(), post.getPostId());
                            commentRecView.setAdapter(commentAdapter);
                        }
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

    private void shareImageAndText(String caption, Bitmap bitmap) {
        Uri uri = saveImageToShare(bitmap);

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("image/png");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject here");
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        shareIntent.putExtra(Intent.EXTRA_TEXT, caption);
        startActivity(Intent.createChooser(shareIntent, "Share Via"));
    }

    private void shareTextOnly(String caption) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("textplain/");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject Here");
        shareIntent.putExtra(Intent.EXTRA_TEXT, caption);
        startActivity(Intent.createChooser(shareIntent, "Share Via"));
    }

    private void initViews() {
        avatarIv = findViewById(R.id.avatarIv);
        postImage = findViewById(R.id.postImage);
        playerView = findViewById(R.id.playerView);
        usernameTv = findViewById(R.id.usernameTv);
        timestampTv = findViewById(R.id.timeStampTv);
        captionTv = findViewById(R.id.captionTv);
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

                BitmapDrawable bitmapDrawable = (BitmapDrawable)postImage.getDrawable();
                if (bitmapDrawable == null) {
                    shareTextOnly(caption);
                } else {
                    Bitmap bitmap = bitmapDrawable.getBitmap();
                    shareImageAndText(caption, bitmap);
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