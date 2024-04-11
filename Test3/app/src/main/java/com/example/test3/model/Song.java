package com.example.test3.model;

import java.io.Serializable;

public class Song implements Serializable {
    private long id;
    private String name;
    private String singer;
    private String album;

    private String genre;

    public Song() {
    }

    public Song(String name, String singer, String album, String genre) {
        this.name = name;
        this.singer = singer;
        this.album = album;
        this.genre = genre;
    }

    public Song(long id, String name, String singer, String album, String genre) {
        this.id = id;
        this.name = name;
        this.singer = singer;
        this.album = album;
        this.genre = genre;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSinger() {
        return singer;
    }

    public void setSinger(String singer) {
        this.singer = singer;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }
}
