package com.example.test3.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.test3.R;
import com.example.test3.adapter.SongAdapter;
import com.example.test3.dal.SQLiteHelper;
import com.example.test3.model.Song;

import java.util.ArrayList;

public class SearchFragment extends Fragment {
    private SongAdapter songAdapter;

    private SearchView searchView;

    private RecyclerView recyclerView;

    private Button btnSearch;

    private SQLiteHelper db;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.search_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.search_rec_view);
        searchView = view.findViewById(R.id.search_input);
        btnSearch = view.findViewById(R.id.btnSearch);
        db = new SQLiteHelper(getContext());

        songAdapter = new SongAdapter(view.getContext(), db.getAll());

        LinearLayoutManager manager = new LinearLayoutManager(view.getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(songAdapter);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                ArrayList <Song> list = db.getByName(newText);
                songAdapter.setList(list);
                return false;
            }
        });

//        btnSearch.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String keyword = searchView.
//            }
//        });
    }
}
