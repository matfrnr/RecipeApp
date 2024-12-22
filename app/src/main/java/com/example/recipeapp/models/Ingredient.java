package com.example.recipeapp.models;

public class Ingredient {
    private String name;
    private String category;
    private double quantity;
    private String unit;

    public Ingredient(String name, String category, double quantity, String unit) {
        this.name = name;
        this.category = category;
        this.quantity = quantity;
        this.unit = unit;
    }

    // Constructor sans quantité et unité (pour les ingrédients de base)
    public Ingredient(String name, String category) {
        this(name, category, 0.0, "");
    }

    // Getters
    public String getName() { return name; }
    public String getCategory() { return category; }
    public double getQuantity() { return quantity; }
    public String getUnit() { return unit; }

    // Setters
    public void setName(String name) { this.name = name; }
    public void setCategory(String category) { this.category = category; }
    public void setQuantity(double quantity) { this.quantity = quantity; }
    public void setUnit(String unit) { this.unit = unit; }

    @Override
    public String toString() {
        if (quantity > 0) {
            return String.format("%.1f %s %s", quantity, unit, name);
        }
        return name;
    }

    // Pour comparaison et HashSet/HashMap
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ingredient that = (Ingredient) o;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}