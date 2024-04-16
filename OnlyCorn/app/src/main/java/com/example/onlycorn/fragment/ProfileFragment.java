package com.example.onlycorn.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.onlycorn.activity.EditProfileActivity;
import com.example.onlycorn.activity.LoginActivity;
import com.example.onlycorn.R;
import com.example.onlycorn.activity.MainActivity;
import com.example.onlycorn.activity.ProfileActivity;
import com.example.onlycorn.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

public class ProfileFragment extends Fragment {

    private FirebaseAuth mAuth;

    private FirebaseFirestore database;

    private FirebaseUser authUser;

    private User user;

    private Context context;

    private ImageView avatarIv;
    private TextView usernameTv;

    private TextView following;
    private TextView followers;

    private Button editButton;

    public ProfileFragment(Context context) {
        this.context = context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.profile_fragment, container, false);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseFirestore.getInstance();
        authUser = mAuth.getCurrentUser();

        avatarIv = view.findViewById(R.id.avatarIv);
        usernameTv = view.findViewById(R.id.usernameTv);
        following = view.findViewById(R.id.following);
        followers = view.findViewById(R.id.followers);
        editButton = view.findViewById(R.id.editButton);

        DocumentReference docRef = database.collection("users").document(authUser.getUid());
        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (documentSnapshot != null) {
                    String username = documentSnapshot.getString("username");

                    usernameTv.setText(username);
                }

            }
        });

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, EditProfileActivity.class);
                startActivity(intent);
            }
        });
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
