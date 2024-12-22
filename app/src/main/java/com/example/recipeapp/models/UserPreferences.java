package com.example.recipeapp.models;

import org.json.JSONArray;
import org.json.JSONException;
import java.util.ArrayList;
import java.util.List;

public class UserPreferences {
    private List<String> allergies;
    private String dietType;
    private String cookingExperience;

    public UserPreferences() {
        allergies = new ArrayList<>();
    }

    public List<String> getAllergies() {
        return allergies;
    }

    // Renommer setAllergens en setAllergies pour être cohérent
    public void setAllergies(List<String> allergies) {
        this.allergies = allergies;
    }

    public String getDietType() {
        return dietType;
    }

    public void setDietType(String dietType) {
        this.dietType = dietType;
    }

    public String getCookingExperience() {
        return cookingExperience;
    }

    public void setCookingExperience(String cookingExperience) {
        this.cookingExperience = cookingExperience;
    }

    // Méthode pour convertir les allergies en JSON
    public String getAllergiesJson() {
        JSONArray jsonArray = new JSONArray(allergies);
        return jsonArray.toString();
    }

    // Méthode pour définir les allergies depuis JSON
    public void setAllergiesFromJson(String json) {
        try {
            JSONArray jsonArray = new JSONArray(json);
            allergies = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); i++) {
                allergies.add(jsonArray.getString(i));
            }
        } catch (JSONException e) {
            allergies = new ArrayList<>();
        }
    }
}