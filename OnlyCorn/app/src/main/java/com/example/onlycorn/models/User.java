package com.example.onlycorn.models;

public class User {
    public static final String COLLECTION = "users";
    private String userId;
    private String name;
    private String email;
    private String username;
    private String image;
    private String status;

    public User() {
    }

    public User(String userId, String email, String status) {
        this.userId = userId;
        this.email = email;
        this.status = status;
        this.setFirstUsername();
    }

    public User(String userId, String email, String username, String status) {
        this.userId = userId;
        this.email = email;
        this.username = username;
        this.status = status;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    private void setFirstUsername() {
        String name = this.email.split("@")[0];
        this.username = name;
        this.name = name;

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
