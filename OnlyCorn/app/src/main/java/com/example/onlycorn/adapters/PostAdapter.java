package com.example.onlycorn.adapters;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.onlycorn.R;
import com.example.onlycorn.activities.AddPostActivity;
import com.example.onlycorn.activities.OtherProfileActivity;
import com.example.onlycorn.activities.PostDetailActivity;
import com.example.onlycorn.activities.PostLikeByActivity;
import com.example.onlycorn.models.Comment;
import com.example.onlycorn.models.Like;
import com.example.onlycorn.models.Post;
import com.example.onlycorn.models.User;
import com.example.onlycorn.utils.FirebaseUtils;
import com.example.onlycorn.utils.ImageUtils;
import com.example.onlycorn.utils.Pop;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {
    private Context context;
    private List<Post> postList;
    private String myUid;
    private User user ;

    private boolean processLike = false;

    public PostAdapter(Context context, List<Post> postList) {
        this.context = context;
        this.postList = postList;
        myUid = FirebaseUtils.getUserAuth().getUid();
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.posts_item, viewGroup, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder postViewHolder, @SuppressLint("RecyclerView") int i) {
        String uId = postList.get(i).getUid();
        String pId = postList.get(i).getPostId();
        String caption = postList.get(i).getCaption();
        String timeStamp = postList.get(i).getTimeStamp();
        String postImage = postList.get(i).getImage();

        loadUserInfo(uId, postViewHolder);
        DocumentReference likeRef = FirebaseUtils.getDocumentRef(Like.COLLECTION, pId);
        likeRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (documentSnapshot != null) {
                    Map<String, Object> data = documentSnapshot.getData();
                    if (data != null) {
                        postViewHolder.likesTv.setText(String.format("%s lượt thích", data.size()));
                    }
                    if (documentSnapshot.contains(myUid)) {
                        postViewHolder.likeButton.setImageResource(R.drawable.icon_heart_filled);
                    } else {
                        postViewHolder.likeButton.setImageResource(R.drawable.icon_heart);
                    }
                }
            }
        });
        DocumentReference commentsRef = FirebaseUtils.getDocumentRef(Comment.COLLECTION, pId);
        commentsRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (documentSnapshot != null) {
                    Map<String, Object> data = documentSnapshot.getData();
                    if (data != null) {
                        postViewHolder.commentsTv.setText(String.format("Xem tất cả %s bình luận", data.size()));
                    }
                }
            }
        });

        String comments = postList.get(i).getComments();

        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(timeStamp));
        @SuppressLint("SimpleDateFormat") String time = new SimpleDateFormat("dd/MM/yyyy hh:mm aa").format(calendar.getTime());

        String type = postList.get(i).getType();
        if ("noMedia".equals(type)) {
            postViewHolder.postImage.setVisibility(View.GONE);
            postViewHolder.playerView.setVisibility(View.GONE);
        } if ("image".equals(type)) {
            postViewHolder.playerView.setVisibility(View.GONE);
            postViewHolder.postImage.setVisibility(View.VISIBLE);

            postViewHolder.postImage.setMinimumHeight(470);
            postViewHolder.postImage.setMaxHeight(470);
            try {
                Picasso.get().load(postImage).placeholder(R.drawable.corn_svgrepo_com).into(postViewHolder.postImage);
            } catch (Exception e) {
            }
        } else if ("video".equals(type)) {
            postViewHolder.playerView.setVisibility(View.VISIBLE);
            postViewHolder.postImage.setVisibility(View.GONE);
            SimpleExoPlayer player = new SimpleExoPlayer.Builder(context).build();
            player.setMediaItem(MediaItem.fromUri(Uri.parse(postImage)));
            player.prepare();
            player.play();

            postViewHolder.playerView.setPlayer(player);
        }

        postViewHolder.captionTv.setText(caption);
        postViewHolder.timestampTv.setText(time);
        postViewHolder.commentsTv.setText(String.format("%s Comments", comments));

        postViewHolder.moreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMoreOptions(postViewHolder.moreButton, uId, pId, postImage);
            }
        });

        postViewHolder.likeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                processLike = true;
                CollectionReference likesRef = FirebaseUtils.getCollectionRef(Like.COLLECTION);
                String postId = postList.get(i).getPostId();
                likesRef.document(postId).get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if (documentSnapshot.contains(myUid) && processLike) {
                                    Map<String, Object> removeUserLike = new HashMap<>();
                                    removeUserLike.put(myUid, FieldValue.delete());
                                    likesRef.document(postId).update(removeUserLike);
                                    processLike = false;
                                } else {
                                    Map<String, Object> newUserLike = new HashMap<>();
                                    newUserLike.put(myUid, "Liked");
                                    likesRef.document(postId).set(newUserLike, SetOptions.merge());
                                    sendLikeNotification(postList.get(i));
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
        });

        postViewHolder.commentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PostDetailActivity.class);
                intent.putExtra("postId", pId);
                context.startActivity(intent);
            }
        });

        postViewHolder.shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BitmapDrawable bitmapDrawable = (BitmapDrawable)postViewHolder.postImage.getDrawable();
                if (bitmapDrawable == null) {
                    shareTextOnly(caption);
                } else {
                    Bitmap bitmap = bitmapDrawable.getBitmap();
                    shareImageAndText(caption, bitmap);
                }
            }
        });

        postViewHolder.profileLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, OtherProfileActivity.class);
                intent.putExtra("uid", uId);
                context.startActivity(intent);
            }
        });

        postViewHolder.likesTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PostLikeByActivity.class);
                intent.putExtra("postId", pId);
                context.startActivity(intent);
            }
        });
    }

    private void sendLikeNotification(Post post) {
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


    private void loadUserInfo(String uId, PostViewHolder postViewHolder) {
        DocumentReference userRef = FirebaseUtils.getDocumentRef(User.COLLECTION, uId);
        userRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (documentSnapshot != null) {
                    user = documentSnapshot.toObject(User.class);

                    if (user != null) {
                        postViewHolder.usernameTv.setText(user.getUsername());

                        try {
                            Glide.with(context).load(Uri.parse(user.getImage()))
                                    .apply(RequestOptions.circleCropTransform()).into(postViewHolder.avatarIv);
                        } catch (Exception ex) {
                            System.out.println(ex.getMessage());
                        }
                    }
                }
            }
        });
    }

    private Uri saveImageToShare(Bitmap bitmap) {
        File imageFolder = new File(context.getCacheDir(), "images");
        Uri uri = null;
        try {
            imageFolder.mkdirs();
            File file = new File(imageFolder, "shared_image.png");
            FileOutputStream stream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream);
            stream.flush();
            stream.close();
            uri = FileProvider.getUriForFile(context, "com.example.onlycorn", file);
        } catch (Exception e) {
            Pop.pop(context, e.getMessage());
        }
        return uri;
    }

    private void shareImageAndText(String caption, Bitmap bitmap) {
        String shareBody = caption;

        Uri uri = saveImageToShare(bitmap);

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("image/png");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject here");
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
        context.startActivity(Intent.createChooser(shareIntent, "Share Via"));
    }

    private void shareTextOnly(String caption) {
        String shareBody = caption;

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("textplain/");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject Here");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
        context.startActivity(Intent.createChooser(shareIntent, "Share Via"));
    }

    private void updateCommentCount(String postId, int likes) {
        DocumentReference postRef = FirebaseUtils.getDocumentRef(Post.COLLECTION, postId);
        postRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (documentSnapshot != null && processLike) {
                    documentSnapshot.getReference().update("likes", String.valueOf(likes));
                    processLike = false;
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public class PostViewHolder extends RecyclerView.ViewHolder {
        ImageView avatarIv, postImage;
        PlayerView playerView;

        TextView usernameTv, timestampTv, captionTv, descriptionTv, likesTv, commentsTv;

        ImageButton moreButton, likeButton, commentButton, shareButton;

        LinearLayout profileLayout;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            avatarIv = itemView.findViewById(R.id.avatarIv);
            postImage = itemView.findViewById(R.id.postImage);
            playerView = itemView.findViewById(R.id.playerView);
            usernameTv = itemView.findViewById(R.id.usernameTv);
            timestampTv = itemView.findViewById(R.id.timeStampTv);
            captionTv = itemView.findViewById(R.id.captionTv);
            likesTv = itemView.findViewById(R.id.likes);
            commentsTv = itemView.findViewById(R.id.comments);
            moreButton = itemView.findViewById(R.id.moreButton);
            likeButton = itemView.findViewById(R.id.likeButton);
            commentButton = itemView.findViewById(R.id.commentButton);
            shareButton = itemView.findViewById(R.id.shareButton);
            profileLayout = itemView.findViewById(R.id.profileLayout);
        }
    }
    private void showMoreOptions(ImageButton moreButton, String uid, String pId, String postImage) {
        PopupMenu popupMenu = new PopupMenu(context, moreButton, Gravity.END);

        if (uid.equals(myUid)) {
            popupMenu.getMenu().add(Menu.NONE, 0, 0, "Delete");
            popupMenu.getMenu().add(Menu.NONE, 1, 0, "Edit");
        }
        popupMenu.getMenu().add(Menu.NONE, 2, 0, "View detail");
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == 0) {
                    beginDelete(pId, postImage);
                } else if (id == 1) {
                    Intent intent = new Intent(context, AddPostActivity.class);
                    intent.putExtra("key", "editPost");
                    intent.putExtra("postId", pId);
                    context.startActivity(intent);
                } else if (id == 2) {
                    Intent intent = new Intent(context, PostDetailActivity.class);
                    intent.putExtra("postId", pId);
                    context.startActivity(intent);
                }
                return false;
            }
        });

        popupMenu.show();
    }

    private void beginDelete(String pId, String postImage) {
        if (postImage.equals("noImage")) {
            deleteWithoutImage(pId);
        } else {
            deleteWithImage(pId, postImage);
        }
    }

    private void deleteWithImage(String pId, String postImage) {
        ProgressDialog pd = new ProgressDialog(context);
        pd.setMessage("Deleting...");
        pd.show();

        StorageReference ref = FirebaseUtils.getStorageRefFromUrl(postImage);
        ref.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Query query = FirebaseUtils.getCollectionRef(Post.COLLECTION)
                                .orderBy("postId").whereEqualTo("postId", pId);
                        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
                            @Override
                            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                                for(QueryDocumentSnapshot qds : queryDocumentSnapshots) {
                                    qds.getReference().delete();
                                }
                                pd.dismiss();
                                Pop.pop(context, "Delete successfully");
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pd.dismiss();
                        Pop.pop(context, e.getMessage());
                    }
                });
    }

    private void deleteWithoutImage(String pId) {
        ProgressDialog pd = new ProgressDialog(context);
        pd.setMessage("Deleting...");

        Query query = FirebaseUtils.getCollectionRef(Post.COLLECTION)
                .orderBy("postId").whereEqualTo("postId", pId);
        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                for(QueryDocumentSnapshot qds : queryDocumentSnapshots) {
                    qds.getReference().delete();
                }
                Pop.pop(context, "Delete successfully");
            }
        });
    }
}
