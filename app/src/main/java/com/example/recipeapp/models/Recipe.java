package com.example.recipeapp.models;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class Recipe {
    private long id;
    private String name;
    private String description;
    private String steps;
    private String imageUrl;
    private int cookingTime;
    private String difficulty;
    private int servings;
    private String author;
    private int userId;
    private boolean isCommunity;        // Nouveau champ
    private float averageRating;        // Nouveau champ
    private int ratingCount;            // Nouveau champ
    private String dietType = "none";  // valeur par défaut
    private List<String> allergens = new ArrayList<>();  // liste vide par défaut

    public Recipe(long id, String name, String description, String steps, String imageUrl,
                  int cookingTime, String difficulty, int servings, String author, int userId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.steps = steps;
        this.imageUrl = imageUrl;
        this.cookingTime = cookingTime;
        this.difficulty = difficulty;
        this.servings = servings;
        this.author = author;
        this.userId = userId;
        this.isCommunity = false;       // Valeur par défaut
        this.averageRating = 0.0f;      // Valeur par défaut
        this.ratingCount = 0;           // Valeur par défaut
        this.allergens = new ArrayList<>();
    }

    public String getDietType() { return dietType; }
    public void setDietType(String dietType) { this.dietType = dietType; }

    public List<String> getAllergens() { return allergens; }
    public void setAllergens(List<String> allergens) { this.allergens = allergens; }

    // Méthode pour convertir les allergènes en JSON
    public String getAllergensJson() {
        return new JSONArray(allergens).toString();
    }

    public void setAllergensFromJson(String json) {
        try {
            JSONArray jsonArray = new JSONArray(json);
            allergens = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); i++) {
                allergens.add(jsonArray.getString(i));
            }
        } catch (JSONException e) {
            allergens = new ArrayList<>();
        }
    }


    // Getters existants
    public long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getSteps() { return steps; }
    public String getImageUrl() { return imageUrl; }
    public int getCookingTime() { return cookingTime; }
    public String getDifficulty() { return difficulty; }
    public int getServings() { return servings; }
    public String getAuthor() { return author; }
    public int getUserId() { return userId; }

    // Setters existants
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setSteps(String steps) { this.steps = steps; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setCookingTime(int cookingTime) { this.cookingTime = cookingTime; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    public void setServings(int servings) { this.servings = servings; }
    public void setAuthor(String author) { this.author = author; }
    public void setUserId(int userId) { this.userId = userId; }

    // Nouveaux getters et setters pour les notes et le statut communautaire
    public boolean isCommunity() { return isCommunity; }
    public void setCommunity(boolean community) { this.isCommunity = community; }
    public List<String> getAllergensList() {
        if (allergens == null) {
            return new ArrayList<>();
        }
        return allergens;
    }
    public float getAverageRating() { return averageRating; }
    public void setAverageRating(float averageRating) { this.averageRating = averageRating; }

    public int getRatingCount() { return ratingCount; }
    public void setRatingCount(int ratingCount) { this.ratingCount = ratingCount; }

    @Override
    public String toString() {
        return name;
    }
}