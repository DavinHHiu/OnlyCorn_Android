package com.example.onlycorn.models;

public class Notification {
    private String postId;
    private String timestamp;
    private String notification;
    private String uid;
    private String username;
    private String userAva;

    public Notification(String postId, String timestamp, String notification, String uid, String username) {
        this.postId = postId;
        this.timestamp = timestamp;
        this.notification = notification;
        this.uid = uid;
        this.username = username;
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

    public String getNotification() {
        return notification;
    }

    public void setNotification(String notification) {
        this.notification = notification;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserAva() {
        return userAva;
    }

    public void setUserAva(String userAva) {
        this.userAva = userAva;
    }
}
