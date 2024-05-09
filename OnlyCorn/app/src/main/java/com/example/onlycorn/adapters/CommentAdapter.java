package com.example.onlycorn.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.example.onlycorn.R;
import com.example.onlycorn.models.Comment;
import com.example.onlycorn.models.Post;
import com.example.onlycorn.utils.DateStringUtils;
import com.example.onlycorn.utils.FirebaseUtils;
import com.example.onlycorn.utils.Pop;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommnentViewHolder> {
    private Context context;
    private List<Comment> listComments;
    private String myUid, postId;

    private boolean processPostComments;

    public CommentAdapter(Context context, List<Comment> listComments, String myUid, String postId) {
        this.context = context;
        this.listComments = listComments;
        this.myUid = myUid;
        this.postId = postId;
    }

    @NonNull
    @Override
    public CommnentViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.comments_item, viewGroup, false);
        return new CommnentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommnentViewHolder commnentViewHolder, int i) {
        Comment comment = listComments.get(i);
        String username = comment.getUsername();
        String userAva = comment.getUserAva();
        String commentText = comment.getComment();
        String timestamp= comment.getTimestamp();

        commnentViewHolder.usernameTv.setText(username);
        commnentViewHolder.commentTv.setText(commentText);
        commnentViewHolder.timestampTv.setText(DateStringUtils.format(timestamp));
        try {
            Picasso.get().load(userAva).placeholder(R.drawable.corn_svgrepo_com).into(commnentViewHolder.avatarIv);
        } catch (Exception e) {
            Picasso.get().load(userAva).placeholder(R.drawable.corn_svgrepo_com).into(commnentViewHolder.avatarIv);
        }
        commnentViewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (myUid.equals(comment.getUserId())) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Delete comment");
                    builder.setMessage("Are you sure to delete this comment?");
                    builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            deleteComment();
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    builder.create().show();
                } else {
                    Pop.pop(context, "Can't delete other's comment");
                }
                return true;
            }
        });
    }

    private void deleteComment() {
        DocumentReference commentRef = FirebaseUtils.getDocumentRef(Comment.COLLECTION, postId);
        Map<String, Object> remove = new HashMap<>();
        remove.put(myUid, FieldValue.delete());
        commentRef.update(remove)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Pop.pop(context, "Comment deleted");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Pop.pop(context, e.getMessage());
                    }
                });
        updatePostComments();
    }

    private void updatePostComments() {
        processPostComments = true;
        DocumentReference postRef = FirebaseUtils.getDocumentRef(Post.COLLECTION, postId);
        postRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (documentSnapshot != null && processPostComments) {
                    String comments = documentSnapshot.getString("comments");
                    int newCommentVal = Integer.parseInt(comments) - 1;
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("comments", String.valueOf(newCommentVal));
                    documentSnapshot.getReference().update(updates);
                    processPostComments = false;
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return listComments.size();
    }

    public class CommnentViewHolder extends RecyclerView.ViewHolder {
        ImageView avatarIv;
        TextView usernameTv, commentTv, timestampTv;
        public CommnentViewHolder(@NonNull View itemView) {
            super(itemView);
            avatarIv = itemView.findViewById(R.id.avatarIv);
            usernameTv = itemView.findViewById(R.id.usernameTv);
            commentTv = itemView.findViewById(R.id.commentTv);
            timestampTv = itemView.findViewById(R.id.timestampTv);
        }
    }
}
