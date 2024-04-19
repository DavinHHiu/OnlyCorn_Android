package com.example.onlycorn.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.onlycorn.R;
import com.example.onlycorn.activities.OtherProfileActivity;
import com.example.onlycorn.models.User;
import com.example.onlycorn.utils.Pop;
import com.squareup.picasso.Picasso;

import java.util.List;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UsersViewHolder> {
    private Context context;
    private List<User> userList;

    public UsersAdapter(Context context, List<User> userList) {
        this.context = context;
        this.userList = userList;
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
        String name = user.getName() == null ? "" : user.getName();
        holder.usernameTv.setText(user.getUsername());
        holder.nameTv.setText(name);
        try {
            Picasso.get().load(user.getImage()).placeholder(R.drawable.icon_home).into(holder.avatarIv);
        } catch (Exception e) {

        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Pop.pop(context.getApplicationContext(), user.getUsername());
                Intent intent = new Intent(context, OtherProfileActivity.class);
                intent.putExtra("uid", user.getUserId());
                context.startActivity(intent);
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

        public UsersViewHolder(@NonNull View itemView) {
            super(itemView);
            avatarIv = itemView.findViewById(R.id.avatarIv);
            nameTv = itemView.findViewById(R.id.nameTv);
            usernameTv = itemView.findViewById(R.id.usernameTv);
        }
    }
}
