package com.example.onlycorn.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.onlycorn.activity.LoginActivity;
import com.example.onlycorn.R;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileFragment extends Fragment {

    private FirebaseAuth mAuth;

    private FirebaseFirestore database;

    private FirebaseUser user;

    private Context context;

    private ImageView avatarIv;
    private TextView usernameTv;

    private TextView following;
    private TextView followers;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.profile_fragment, container, false);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseFirestore.getInstance();
        user = mAuth.getCurrentUser();

        avatarIv = view.findViewById(R.id.avatarIv);
        usernameTv = view.findViewById(R.id.usernameTv);
        following = view.findViewById(R.id.following);
        followers = view.findViewById(R.id.followers);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        checkUserStatus();
    }

    private void checkUserStatus() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {

        } else {
            startActivity(new Intent(getActivity(), LoginActivity.class));
            if (getActivity() != null) {
                getActivity().finish();
            }
        }
    }
}
