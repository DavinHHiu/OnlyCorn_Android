package com.example.test3.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.test3.R;
import com.example.test3.UpdateDeleteActivity;
import com.example.test3.adapter.SongAdapter;
import com.example.test3.dal.SQLiteHelper;
import com.example.test3.model.Song;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class ListFragment extends Fragment implements SongAdapter.SongItemListener {
    private SongAdapter songAdapter;

    private RecyclerView songRecView;

    private ArrayList<Song> listSong;

    private SQLiteHelper db;

    public ListFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.list_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        songRecView = view.findViewById(R.id.song_rec_view);
        db = new SQLiteHelper(getContext());
        listSong = db.getAll();
        songAdapter = new SongAdapter(view.getContext(), listSong);
        songAdapter.setSongItemListener(this);

        LinearLayoutManager manager = new LinearLayoutManager(view.getContext(), RecyclerView.VERTICAL, false);
        songRecView.setLayoutManager(manager);
        songRecView.setAdapter(songAdapter);
    }

    @Override
    public void onItemClick(View view, int position) {
        Song song = songAdapter.getItem(position);
        Intent intent = new Intent(getActivity(), UpdateDeleteActivity.class);
        intent.putExtra("song", song);
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        ArrayList<Song> list = db.getAll();
        songAdapter.setList(list);
    }
}
