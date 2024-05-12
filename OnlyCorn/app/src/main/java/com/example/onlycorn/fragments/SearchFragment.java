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
import com.example.onlycorn.adapters.PostAdapter;
import com.example.onlycorn.adapters.UsersAdapter;
import com.example.onlycorn.models.Post;
import com.example.onlycorn.models.User;
import com.example.onlycorn.utils.FirebaseUtils;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {
    private RecyclerView recyclerView;
    private SearchView searchView;

    private List<Post> postList;
    private PostAdapter postAdapter;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.search_fragment, container, false);

        recyclerView = view.findViewById(R.id.postRecycleView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        searchView = view.findViewById(R.id.search_view_posts);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!TextUtils.isEmpty(query.trim())) {
                    searchUsers(query);
                } else {
                    getAllPosts();
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (!TextUtils.isEmpty(newText.trim())) {
                    searchUsers(newText);
                } else {
                    getAllPosts();
                }
                return false;
            }
        });

        postList = new ArrayList<>();
        getAllPosts();

        return view;
    }

    private void searchUsers(String query) {
        CollectionReference collectionRef = FirebaseUtils.getCollectionRef(Post.COLLECTION);

        collectionRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                postList.clear();
                for(QueryDocumentSnapshot qds : queryDocumentSnapshots) {
                    Post post = qds.toObject(Post.class);

                    if (post.getCaption().toLowerCase().contains(query.trim().toLowerCase())) {
                        postList.add(post);
                    }

                    postAdapter = new PostAdapter(getActivity(), postList);
                    recyclerView.setAdapter(postAdapter);
                }
            }
        });
    }

    private void getAllPosts() {
        CollectionReference collectionRef = FirebaseUtils.getCollectionRef(Post.COLLECTION);
        collectionRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                postList.clear();
                for(QueryDocumentSnapshot qs : queryDocumentSnapshots) {
                    Post post = qs.toObject(Post.class);

                    postList.add(post);

                    postAdapter = new PostAdapter(getActivity(), postList);
                    recyclerView.setAdapter(postAdapter);
                }
            }
        });
    }

}
