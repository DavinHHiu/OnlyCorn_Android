package com.example.onlycorn.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.onlycorn.R;
import com.example.onlycorn.models.Notification;
import com.example.onlycorn.utils.DateStringUtils;
import com.squareup.picasso.Picasso;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {
    private Context context;
    private List<Notification> notificationList;

    public NotificationAdapter(Context context, List<Notification> notificationList) {
        this.context = context;
        this.notificationList = notificationList;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
         View view = LayoutInflater.from(context).inflate(R.layout.item_notification, viewGroup, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int i) {
        Notification noti = notificationList.get(i);
        String username = noti.getUsername();
        String userAva = noti.getUserAva();
        String notification = noti.getNotification();
        String timestamp = noti.getTimestamp();

        holder.usernameTv.setText(username);
        holder.notificationTv.setText(notification);
        holder.timestampTv.setText(DateStringUtils.format(timestamp));
        try {
            Picasso.get().load(userAva).into(holder.avatarIv);
        } catch (Exception e) {
            Picasso.get().load(userAva).into(holder.avatarIv);
        }
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    public class NotificationViewHolder extends RecyclerView.ViewHolder {

        ImageView avatarIv;

        TextView usernameTv, notificationTv, timestampTv;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            avatarIv = itemView.findViewById(R.id.avatarIv);
            usernameTv = itemView.findViewById(R.id.usernameTv);
            notificationTv = itemView.findViewById(R.id.notificationTv);
            timestampTv = itemView.findViewById(R.id.timestampTv);
        }
    }
}
