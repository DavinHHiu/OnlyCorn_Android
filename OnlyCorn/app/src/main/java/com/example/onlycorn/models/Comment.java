package com.example.onlycorn.models;

import java.util.Map;

public class Comment {
    public static final String COLLECTION = "comments";

    private String cId;

    private String comment;

    private String postId;

    private String timestamp;

    private String userAva;

    private String userId;

    private String username;

    public Comment(String cId, String postId, String userId, String username,
                   String userAva, String timestamp, String comment) {
        this.cId = cId;
        this.postId = postId;
        this.userId = userId;
        this.username = username;
        this.userAva = userAva;
        this.timestamp = timestamp;
        this.comment = comment;
    }

    public String getcId() {
        return cId;
    }

    public void setcId(String cId) {
        this.cId = cId;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
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

    public String getUserAva() {
        return userAva;
    }

    public void setUserAva(String userAva) {
        this.userAva = userAva;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public static Comment convertFromMap(Map<String, Object> map) {
        String cId = (String) map.get("cId");
        String comment = (String) map.get("comment");
        String postId = (String) map.get("postId");
        String userId = (String) map.get("userId");
        String username = (String) map.get("username");
        String userAva = (String) map.get("userAva");
        String timestamp = (String) map.get("timestamp");

        return new Comment(cId, postId, userId, username, userAva, timestamp, comment);
    }
}
