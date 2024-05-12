package com.example.onlycorn.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.onlycorn.R;
import com.example.onlycorn.activities.OtherProfileActivity;
import com.example.onlycorn.models.User;
import com.example.onlycorn.utils.FirebaseUtils;
import com.example.onlycorn.utils.Pop;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.SetOptions;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UsersViewHolder> {
    private Context context;
    private List<User> userList;
    private String myUserId;
    private User user;
    private boolean isFollower, isFollowing;

    public UsersAdapter(Context context, List<User> userList, String myUserId) {
        this.context = context;
        this.userList = userList;
        this.myUserId = myUserId;
    }

    @NonNull
    @Override
    public UsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.users_item, parent, false);
        return new UsersViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UsersViewHolder holder, int position) {
        User user = userList.get(position);
        holder.usernameTv.setText(user.getUsername());
        holder.nameTv.setText(user.getName());
        try {
            Glide.with(context).load(Uri.parse(user.getImage()))
                    .apply(RequestOptions.circleCropTransform()).into(holder.avatarIv);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, OtherProfileActivity.class);
                intent.putExtra("uid", user.getUserId());
                context.startActivity(intent);
            }
        });
        getStatus(user, holder.followButton);
        holder.followButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DocumentReference followingRef = FirebaseUtils.getDocumentRef(User.FOLLOWING_COLLECTION, myUserId);
                HashMap<String, Object> updateFollowing = new HashMap<>();
                updateFollowing.put(user.getUserId(), "follow");
                followingRef.set(updateFollowing, SetOptions.merge());

                DocumentReference followerRef = FirebaseUtils.getDocumentRef(User.FOLLOWER_COLLECTION, user.getUserId());
                HashMap<String, Object> updateFollower = new HashMap<>();
                updateFollower.put(myUserId, "follow");
                followerRef.set(updateFollower, SetOptions.merge());
            }
        });
    }

    private void getStatus(User otherUser, Button button) {
        isFollower = false;
        isFollowing = false;
        DocumentReference followerRef = FirebaseUtils.getDocumentRef(User.FOLLOWER_COLLECTION, myUserId);
        followerRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                isFollower = documentSnapshot != null && documentSnapshot.contains(otherUser.getUserId());
                DocumentReference followingRef = FirebaseUtils.getDocumentRef(User.FOLLOWING_COLLECTION, myUserId);
                followingRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                        isFollowing = documentSnapshot != null && documentSnapshot.contains(otherUser.getUserId());
                        setStatus(otherUser, button);
                    }
                });
            }
        });
        DocumentReference followingRef = FirebaseUtils.getDocumentRef(User.FOLLOWING_COLLECTION, myUserId);
        followingRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                isFollowing = documentSnapshot != null && documentSnapshot.contains(otherUser.getUserId());
                DocumentReference followerRef = FirebaseUtils.getDocumentRef(User.FOLLOWER_COLLECTION, myUserId);
                followerRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                        isFollower = documentSnapshot != null && documentSnapshot.contains(otherUser.getUserId());
                        setStatus(otherUser, button);
                    }
                });
            }
        });
    }

    private void setStatus(User user, Button button) {
        if (isFollower && isFollowing) {
            button.setText("Bạn bè");
            button.setEnabled(false);
            sendFollowNotification(user);
        } else if (!isFollower && isFollowing) {
            button.setText("Đã theo dõi");
            button.setEnabled(false);
            sendFollowNotification(user);
        }else {
            button.setText("Theo dõi");
            button.setEnabled(true);
        }
    }

    private void sendFollowNotification(User otherUser) {
        FirebaseUtils.getDocumentRef(User.COLLECTION, myUserId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (documentSnapshot != null) {
                    user = documentSnapshot.toObject(User.class);
                }
            }
        });
        FirebaseUtils.getDocumentRef(User.COLLECTION, otherUser.getUserId()).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (documentSnapshot != null) {
                    String fcmToken = documentSnapshot.getString("fcmToken");
                    if (fcmToken != null) {
                        try{
                            JSONObject jsonObject = new JSONObject();

                            JSONObject notificationObj = new JSONObject();
                            notificationObj.put("title", "Only Corn");
                            notificationObj.put("body", String.format("%s vừa theo dõi bạn.", user.getUsername()));

                            JSONObject dataObj = new JSONObject();
                            dataObj.put("userId", user.getUserId());

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

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public class UsersViewHolder extends RecyclerView.ViewHolder {
        ImageView avatarIv;
        TextView nameTv, usernameTv;
        Button followButton;

        public UsersViewHolder(@NonNull View itemView) {
            super(itemView);
            avatarIv = itemView.findViewById(R.id.avatarIv);
            nameTv = itemView.findViewById(R.id.nameTv);
            usernameTv = itemView.findViewById(R.id.usernameTv);
            followButton = itemView.findViewById(R.id.followButton);
        }
    }
}
