package com.example.recipeapp.models;

import java.util.ArrayList;
import java.util.List;

public class User {
    private int id;
    private String username;
    private String email;
    private String password;
    private List<Integer> favoriteRecipes;

    public User(int id, String username, String email, String password) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.favoriteRecipes = new ArrayList<>();
    }

    // Getters et setters
    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public void setUsername(String username) { this.username = username; }
    public void setEmail(String email) { this.email = email; }
    public List<Integer> getFavoriteRecipes() { return favoriteRecipes; }
    public void addFavoriteRecipe(int recipeId) {
        if (!favoriteRecipes.contains(recipeId)) {
            favoriteRecipes.add(recipeId);
        }
    }
    public void removeFavoriteRecipe(int recipeId) {
        favoriteRecipes.remove(Integer.valueOf(recipeId));
    }
}