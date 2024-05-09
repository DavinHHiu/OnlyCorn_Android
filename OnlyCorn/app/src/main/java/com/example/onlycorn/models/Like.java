package com.example.onlycorn.models;

public class Like {
    public static final String COLLECTION = "likes";

    private String lId;

    private String postId;

    private String timestamp;

    private String userAva;

    private String userId;

    private String username;

    public Like(String lId, String postId, String timestamp, String userAva, String userId, String username) {
        this.lId = lId;
        this.postId = postId;
        this.timestamp = timestamp;
        this.userAva = userAva;
        this.userId = userId;
        this.username = username;
    }

    public String getlId() {
        return lId;
    }

    public void setlId(String lId) {
        this.lId = lId;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getUserAva() {
        return userAva;
    }

    public void setUserAva(String userAva) {
        this.userAva = userAva;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
