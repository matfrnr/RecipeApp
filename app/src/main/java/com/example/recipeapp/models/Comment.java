package com.example.recipeapp.models;

public class Comment {
    private long id;
    private long userId;
    private long recipeId;
    private String content;
    private long timestamp;
    private String username;
    private boolean isEdited;

    public Comment(long id, long userId, long recipeId, String content, long timestamp, String username) {
        this.id = id;
        this.userId = userId;
        this.recipeId = recipeId;
        this.content = content;
        this.timestamp = timestamp;
        this.username = username;
        this.isEdited = false;
    }

    // Getters
    public long getId() { return id; }
    public long getUserId() { return userId; }
    public long getRecipeId() { return recipeId; }
    public String getContent() { return content; }
    public long getTimestamp() { return timestamp; }
    public String getUsername() { return username; }
    public boolean isEdited() { return isEdited; }

    // Setters
    public void setContent(String content) { this.content = content; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public void setEdited(boolean edited) { isEdited = edited; }
}