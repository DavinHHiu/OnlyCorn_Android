package com.example.onlycorn.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.onlycorn.R;
import com.example.onlycorn.adapters.UsersAdapter;
import com.example.onlycorn.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import java.util.List;

public class UserFragment extends Fragment {

    private RecyclerView recyclerView;

    private SearchView searchView;

    private UsersAdapter usersAdapter;

    private List<User> userList;

    public UserFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.user_fragment, container, false);
        initViews(view);
        return view;
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.user_recycleView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        searchView = view.findViewById(R.id.search_view_users);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!TextUtils.isEmpty(query.trim())) {
                    searchUsers(query);
                } else {
                    getAllUsers();
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (!TextUtils.isEmpty(newText.trim())) {
                    searchUsers(newText);
                } else {
                    getAllUsers();
                }
                return false;
            }
        });

        userList = new ArrayList<>();

        getAllUsers();
    }

    private void searchUsers(String query) {
        FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
        CollectionReference collectionRef = FirebaseFirestore.getInstance().collection(User.COLLECTION);

        collectionRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                userList.clear();
                for(QueryDocumentSnapshot qds : queryDocumentSnapshots) {
                    User user = qds.toObject(User.class);

                    if (!user.getUserId().equals(fUser.getUid())
                            && (user.getUsername().toLowerCase().contains(query.trim().toLowerCase()) ||
                                user.getName().toLowerCase().contains(query.trim().toLowerCase()))) {
                        userList.add(user);
                    }

                    usersAdapter = new UsersAdapter(getActivity(), userList);
                    recyclerView.setAdapter(usersAdapter);
                }
            }
        });
    }

    private void getAllUsers() {
        FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
        CollectionReference collectionRef = FirebaseFirestore.getInstance().collection(User.COLLECTION);

        collectionRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                userList.clear();
                for(QueryDocumentSnapshot qs : queryDocumentSnapshots) {
                    User user = qs.toObject(User.class);

                    if (!user.getUserId().equals(fUser.getUid())) {
                        userList.add(user);
                    }

                    usersAdapter = new UsersAdapter(getActivity(), userList);
                    recyclerView.setAdapter(usersAdapter);
                }
            }
        });
    }
}
