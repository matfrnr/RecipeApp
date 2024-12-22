package com.example.recipeapp.models;

public class RecipeIngredient {
    private long id;
    private long recipeId;
    private String name;
    private double quantity;
    private String unit;
    private boolean isChecked;
    private String recipeName;

    public RecipeIngredient(long id, long recipeId, String name, double quantity, String unit) {
        this.id = id;
        this.recipeId = recipeId;
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
    }

    // Getters et Setters
    public long getId() { return id; }
    public void setUnit(String unit) {
        this.unit = unit;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }
    public long getRecipeId() { return recipeId; }
    public String getName() { return name; }
    public double getQuantity() { return quantity; }
    public String getUnit() { return unit; }
    // Ajouter au constructeur ou créer un nouveau constructeur
    public RecipeIngredient(long id, long recipeId, String name, double quantity, String unit, String recipeName) {
        this(id, recipeId, name, quantity, unit);
        this.recipeName = recipeName;
    }

    // Ajouter le getter
    public String getRecipeName() {
        return recipeName;
    }
    public void setQuantity(double quantity) { this.quantity = quantity; }
}