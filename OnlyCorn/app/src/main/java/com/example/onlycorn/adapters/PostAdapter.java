package com.example.onlycorn.adapters;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
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
import androidx.recyclerview.widget.RecyclerView;

import com.example.onlycorn.R;
import com.example.onlycorn.activities.AddPostActivity;
import com.example.onlycorn.activities.OtherProfileActivity;
import com.example.onlycorn.activities.PostDetailActivity;
import com.example.onlycorn.models.Post;
import com.example.onlycorn.utils.FirebaseUtils;
import com.example.onlycorn.utils.Pop;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {
    private Context context;
    private List<Post> postList;
    private String myUid;
    private CollectionReference likesRef;
    private CollectionReference postsRef;

    private boolean processLike = false;

    public PostAdapter(Context context, List<Post> postList) {
        this.context = context;
        this.postList = postList;
        myUid = FirebaseUtils.getUserAuth().getUid();
        postsRef = FirebaseUtils.getCollectionRef(Post.COLLECTION);
        likesRef = FirebaseUtils.getCollectionRef(Post.LIKE_COLLECTION);
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
        String username = postList.get(i).getUsername();
        String useAva = postList.get(i).getUserAva();
        String pId = postList.get(i).getPostId();
        String caption = postList.get(i).getCaption();
        String desc = postList.get(i).getDescription();
        String timeStamp = postList.get(i).getTimeStamp();
        String postImage = postList.get(i).getImage();
        String likes = postList.get(i).getLikes();

        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(timeStamp));
        @SuppressLint("SimpleDateFormat") String time = new SimpleDateFormat("dd/MM/yyyy hh:mm aa").format(calendar.getTime());

        try {
            Picasso.get().load(useAva).placeholder(R.drawable.icon_home).into(postViewHolder.avatarIv);
        } catch (Exception e) {
        }

        if (postImage.equals("noImage")) {
            postViewHolder.postImage.setVisibility(View.GONE);
        } else {
            postViewHolder.postImage.setVisibility(View.VISIBLE);
            try {
                Picasso.get().load(postImage).placeholder(R.drawable.corn_svgrepo_com).into(postViewHolder.postImage);
            } catch (Exception e) {
            }
        }

        postViewHolder.usernameTv.setText(username);
        postViewHolder.captionTv.setText(caption);
        postViewHolder.descriptionTv.setText(desc);
        postViewHolder.timestampTv.setText(time);
        postViewHolder.likesTv.setText(String.format("%s Likes", likes));
        setLikes(postViewHolder, pId);

        postViewHolder.moreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMoreOptions(postViewHolder.moreButton, uId, pId, postImage);
            }
        });

        postViewHolder.likeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int likes = Integer.parseInt(postList.get(i).getLikes());
                processLike = true;
                String postId = postList.get(i).getPostId();
                likesRef.document(postId).get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if (documentSnapshot.contains(uId)) {
                                    postsRef.document(timeStamp).update("likes",  String.valueOf(likes - 1));

                                    Map<String, Object> deleteField = new HashMap<>();
                                    deleteField.put(uId, FieldValue.delete());
                                    likesRef.document(postId).update(deleteField);
                                    processLike = false;
                                } else {
                                    postsRef.document(timeStamp).update("likes",  String.valueOf(likes + 1));

                                    Map<String, Object> newUserLike = new HashMap<>();
                                    newUserLike.put(uId, "Liked");
                                    likesRef.document(postId).set(newUserLike);
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
                Pop.pop(context, "Share");
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
    }

    private void setLikes(PostViewHolder postViewHolder, String postId) {
        likesRef.document(postId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (documentSnapshot != null && documentSnapshot.contains(myUid)) {
                    postViewHolder.likeButton.setImageResource(R.drawable.icon_heart_filled);
                } else {
                    postViewHolder.likeButton.setImageResource(R.drawable.icon_heart);
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

        TextView usernameTv, timestampTv, captionTv, descriptionTv, likesTv;

        ImageButton moreButton, likeButton, commentButton, shareButton;
        LinearLayout profileLayout;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            avatarIv = itemView.findViewById(R.id.avatarIv);
            postImage = itemView.findViewById(R.id.postImage);
            usernameTv = itemView.findViewById(R.id.usernameTv);
            timestampTv = itemView.findViewById(R.id.timeStampTv);
            captionTv = itemView.findViewById(R.id.captionTv);
            descriptionTv = itemView.findViewById(R.id.descriptionTv);
            likesTv = itemView.findViewById(R.id.likes);
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
