package com.example.test3.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.test3.R;
import com.example.test3.model.Song;

import java.util.ArrayList;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder> {
    private Context context;
    private ArrayList<Song> listSong;
    private SongItemListener songItemListener;

    @SuppressLint("NotifyDataSetChanged")
    public void setList(ArrayList<Song> list) {
        this.listSong = list;
        notifyDataSetChanged();
    }

    public Song getItem(int position) {
        return listSong.get(position);
    }

    public void setSongItemListener(SongItemListener songItemListener) {
        this.songItemListener = songItemListener;
    }

    public SongAdapter(Context context, ArrayList<Song> listSong) {
        this.context = context;
        this.listSong = listSong;
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.song_item, parent, false);
        return new SongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        Song song = listSong.get(position);
        holder.songName.setText(song.getName());
        holder.albumName.setText(song.getAlbum());
        holder.singerName.setText(song.getSinger());
        holder.genreName.setText(song.getGenre());
    }

    @Override
    public int getItemCount() {
        return listSong != null ? listSong.size():  0;
    }

    public class SongViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView songName, singerName, albumName, genreName;
        public SongViewHolder(@NonNull View itemView) {
            super(itemView);
            songName = itemView.findViewById(R.id.song_name);
            singerName = itemView.findViewById(R.id.singer_name);
            albumName = itemView.findViewById(R.id.album_name);
            genreName = itemView.findViewById(R.id.genre_name);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (songItemListener != null) {
                songItemListener.onItemClick(view, getAdapterPosition());
            }
        }
    }

    public interface SongItemListener {
        void onItemClick(View view, int position);
    }
}
