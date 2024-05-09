package com.example.onlycorn.models;

public class Post {
    public static final String COLLECTION = "posts";

    private String postId;

    private String caption;

    private String description;

    private String image;

    private String timeStamp;

    private String likes;

    private String comments;

    private String uid;

    private String username;

    private String userAva;

    public Post() {
    }

    public Post(String postId, String caption, String description, String image, String timeStamp,
                String likes, String comments, String uid, String username, String userAva) {
        this.postId = postId;
        this.caption = caption;
        this.description = description;
        this.image = image;
        this.timeStamp = timeStamp;
        this.likes = likes;
        this.comments = comments;
        this.uid = uid;
        this.username = username;
        this.userAva = userAva;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getLikes() {
        return likes;
    }

    public void setLikes(String likes) {
        this.likes = likes;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
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
