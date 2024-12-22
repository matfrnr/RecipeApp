package com.example.recipeapp.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.recipeapp.models.ChatMessage;
import com.example.recipeapp.models.Comment;
import com.example.recipeapp.models.Recipe;
import com.example.recipeapp.models.RecipeIngredient;
import com.example.recipeapp.models.User;
import com.example.recipeapp.models.UserPreferences;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 54; // Incrémentez la version
    public static final String DATABASE_NAME = "RecipeBook.db";

    private static final String SQL_CREATE_RECIPES =
            "CREATE TABLE " + DatabaseContract.RecipeEntry.TABLE_NAME + " (" +
                    DatabaseContract.RecipeEntry._ID + " INTEGER PRIMARY KEY," +
                    DatabaseContract.RecipeEntry.COLUMN_NAME + " TEXT," +
                    DatabaseContract.RecipeEntry.COLUMN_DESCRIPTION + " TEXT," +
                    DatabaseContract.RecipeEntry.COLUMN_STEPS + " TEXT," +
                    DatabaseContract.RecipeEntry.COLUMN_IMAGE_URL + " TEXT," +
                    DatabaseContract.RecipeEntry.COLUMN_COOKING_TIME + " INTEGER," +
                    DatabaseContract.RecipeEntry.COLUMN_DIFFICULTY + " TEXT," +
                    DatabaseContract.RecipeEntry.COLUMN_SERVINGS + " INTEGER," +
                    DatabaseContract.RecipeEntry.COLUMN_USER_ID + " INTEGER," +
                    DatabaseContract.RecipeEntry.COLUMN_AUTHOR + " TEXT," +
                    DatabaseContract.RecipeEntry.COLUMN_IS_COMMUNITY + " INTEGER DEFAULT 0," +
                    DatabaseContract.RecipeEntry.COLUMN_DIET_TYPE + " TEXT," +
                    DatabaseContract.RecipeEntry.COLUMN_ALLERGENS + " TEXT," +
                    DatabaseContract.RecipeEntry.COLUMN_CREATION_DATE + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
    private static final String SQL_CREATE_RATINGS =
            "CREATE TABLE " + DatabaseContract.RatingEntry.TABLE_NAME + " (" +
                    DatabaseContract.RatingEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    DatabaseContract.RatingEntry.COLUMN_USER_ID + " INTEGER," +
                    DatabaseContract.RatingEntry.COLUMN_RECIPE_ID + " INTEGER," +
                    DatabaseContract.RatingEntry.COLUMN_RATING + " FLOAT," +
                    DatabaseContract.RatingEntry.COLUMN_TIMESTAMP + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "UNIQUE(" + DatabaseContract.RatingEntry.COLUMN_USER_ID + ", " +
                    DatabaseContract.RatingEntry.COLUMN_RECIPE_ID + "))";

    private static final String SQL_CREATE_RECIPE_INGREDIENTS =
            "CREATE TABLE " + DatabaseContract.RecipeIngredientEntry.TABLE_NAME + " (" +
                    DatabaseContract.RecipeIngredientEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    DatabaseContract.RecipeIngredientEntry.COLUMN_RECIPE_ID + " INTEGER," +
                    DatabaseContract.RecipeIngredientEntry.COLUMN_NAME + " TEXT," +
                    DatabaseContract.RecipeIngredientEntry.COLUMN_QUANTITY + " REAL," +
                    DatabaseContract.RecipeIngredientEntry.COLUMN_UNIT + " TEXT," +
                    "FOREIGN KEY(" + DatabaseContract.RecipeIngredientEntry.COLUMN_RECIPE_ID + ") " +
                    "REFERENCES " + DatabaseContract.RecipeEntry.TABLE_NAME + "(" + DatabaseContract.RecipeEntry._ID + "))";


    private static final String SQL_CREATE_USERS =
            "CREATE TABLE users (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "username TEXT UNIQUE," +
                    "email TEXT UNIQUE," +
                    "password TEXT)";

    private static final String SQL_CREATE_FAVORITES =
            "CREATE TABLE favorites (" +
                    "user_id INTEGER," +
                    "recipe_id INTEGER," +
                    "FOREIGN KEY(user_id) REFERENCES users(id)," +
                    "FOREIGN KEY(recipe_id) REFERENCES recipes(id)," +
                    "PRIMARY KEY(user_id, recipe_id))";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_RECIPES);
        db.execSQL(SQL_CREATE_USERS);
        db.execSQL(SQL_CREATE_FAVORITES);
        db.execSQL(SQL_CREATE_RATINGS);
        db.execSQL(SQL_CREATE_USER_PREFERENCES);
        db.execSQL(SQL_CREATE_RECIPE_INGREDIENTS);
        db.execSQL(SQL_CREATE_SHOPPING_LIST);
        db.execSQL(SQL_CREATE_COMMENTS);
        db.execSQL(SQL_CREATE_CHAT_HISTORY);
        RecipeDataInitializer.insertInitialRecipes(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            // Sauvegarder les données existantes si nécessaire
            List<RecipeIngredient> shoppingList = null;
            if (tableExists(db, DatabaseContract.ShoppingListEntry.TABLE_NAME)) {
                shoppingList = getShoppingListFromDatabase(db);
            }

            // Supprimer les tables existantes
            db.execSQL("DROP TABLE IF EXISTS " + DatabaseContract.ShoppingListEntry.TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + DatabaseContract.RecipeIngredientEntry.TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS favorites");
            db.execSQL("DROP TABLE IF EXISTS " + DatabaseContract.RecipeEntry.TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS users");
            db.execSQL("DROP TABLE IF EXISTS " + DatabaseContract.RatingEntry.TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + DatabaseContract.UserPreferencesEntry.TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + DatabaseContract.CommentEntry.TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + DatabaseContract.ChatHistoryEntry.TABLE_NAME);
            onCreate(db);
            // Recréer les tables
            onCreate(db);

            // Restaurer les données si nécessaire
            if (shoppingList != null) {
                restoreShoppingList(db, shoppingList);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Ajoutez ces méthodes pour gérer l'historique
    public void saveChatMessage(int userId, String message, boolean isUser) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.ChatHistoryEntry.COLUMN_USER_ID, userId);
        values.put(DatabaseContract.ChatHistoryEntry.COLUMN_MESSAGE, message);
        values.put(DatabaseContract.ChatHistoryEntry.COLUMN_IS_USER, isUser ? 1 : 0);
        values.put(DatabaseContract.ChatHistoryEntry.COLUMN_TIMESTAMP, System.currentTimeMillis());
        db.insert(DatabaseContract.ChatHistoryEntry.TABLE_NAME, null, values);
    }

    public List<ChatMessage> getChatHistory(int userId) {
        List<ChatMessage> history = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String selection = DatabaseContract.ChatHistoryEntry.COLUMN_USER_ID + " = ?";
        String[] selectionArgs = {String.valueOf(userId)};
        String orderBy = DatabaseContract.ChatHistoryEntry.COLUMN_TIMESTAMP + " ASC";

        Cursor cursor = db.query(
                DatabaseContract.ChatHistoryEntry.TABLE_NAME,
                null,
                selection,
                selectionArgs,
                null,
                null,
                orderBy
        );

        while (cursor.moveToNext()) {
            String message = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.ChatHistoryEntry.COLUMN_MESSAGE));
            boolean isUser = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.ChatHistoryEntry.COLUMN_IS_USER)) == 1;
            history.add(new ChatMessage(message, isUser));
        }
        cursor.close();
        return history;
    }

    private boolean tableExists(SQLiteDatabase db, String tableName) {
        Cursor cursor = db.rawQuery(
                "SELECT name FROM sqlite_master WHERE type='table' AND name=?",
                new String[]{tableName}
        );
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    private static final String SQL_CREATE_COMMENTS =
            "CREATE TABLE " + DatabaseContract.CommentEntry.TABLE_NAME + " (" +
                    DatabaseContract.CommentEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    DatabaseContract.CommentEntry.COLUMN_USER_ID + " INTEGER," +
                    DatabaseContract.CommentEntry.COLUMN_RECIPE_ID + " INTEGER," +
                    DatabaseContract.CommentEntry.COLUMN_CONTENT + " TEXT," +
                    DatabaseContract.CommentEntry.COLUMN_USERNAME + " TEXT," +
                    DatabaseContract.CommentEntry.COLUMN_TIMESTAMP + " INTEGER," +
                    DatabaseContract.CommentEntry.COLUMN_IS_EDITED + " INTEGER DEFAULT 0)";

    public void addComment(long userId, long recipeId, String content, String username) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.CommentEntry.COLUMN_USER_ID, userId);
        values.put(DatabaseContract.CommentEntry.COLUMN_RECIPE_ID, recipeId);
        values.put(DatabaseContract.CommentEntry.COLUMN_CONTENT, content);
        values.put(DatabaseContract.CommentEntry.COLUMN_USERNAME, username);
        values.put(DatabaseContract.CommentEntry.COLUMN_TIMESTAMP, System.currentTimeMillis());
        db.insert(DatabaseContract.CommentEntry.TABLE_NAME, null, values);
    }

    public List<Comment> getRecipeComments(long recipeId) {
        List<Comment> comments = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(
                DatabaseContract.CommentEntry.TABLE_NAME,
                null,
                DatabaseContract.CommentEntry.COLUMN_RECIPE_ID + " = ?",
                new String[]{String.valueOf(recipeId)},
                null,
                null,
                DatabaseContract.CommentEntry.COLUMN_TIMESTAMP + " DESC"
        );

        while (cursor.moveToNext()) {
            Comment comment = new Comment(
                    cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseContract.CommentEntry._ID)),
                    cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseContract.CommentEntry.COLUMN_USER_ID)),
                    recipeId,
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.CommentEntry.COLUMN_CONTENT)),
                    cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseContract.CommentEntry.COLUMN_TIMESTAMP)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.CommentEntry.COLUMN_USERNAME))
            );
            comments.add(comment);
        }
        cursor.close();
        return comments;
    }

    public void deleteComment(long commentId, long userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        // Vérifie que le commentaire appartient bien à l'utilisateur avant de le supprimer
        db.delete(DatabaseContract.CommentEntry.TABLE_NAME,
                DatabaseContract.CommentEntry._ID + " = ? AND " +
                        DatabaseContract.CommentEntry.COLUMN_USER_ID + " = ?",
                new String[]{String.valueOf(commentId), String.valueOf(userId)});
    }

    public void updateComment(long commentId, String newContent, long userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.CommentEntry.COLUMN_CONTENT, newContent);
        values.put(DatabaseContract.CommentEntry.COLUMN_TIMESTAMP, System.currentTimeMillis());
        values.put(DatabaseContract.CommentEntry.COLUMN_IS_EDITED, 1);

        // Vérifie que le commentaire appartient bien à l'utilisateur avant de le modifier
        db.update(DatabaseContract.CommentEntry.TABLE_NAME,
                values,
                DatabaseContract.CommentEntry._ID + " = ? AND " +
                        DatabaseContract.CommentEntry.COLUMN_USER_ID + " = ?",
                new String[]{String.valueOf(commentId), String.valueOf(userId)});
    }

    private List<RecipeIngredient> getShoppingListFromDatabase(SQLiteDatabase db) {
        List<RecipeIngredient> list = new ArrayList<>();
        Cursor cursor = db.query(DatabaseContract.ShoppingListEntry.TABLE_NAME,
                null, null, null, null, null, null);

        while (cursor.moveToNext()) {
            RecipeIngredient ingredient = new RecipeIngredient(
                    cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseContract.ShoppingListEntry._ID)),
                    0,
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.ShoppingListEntry.COLUMN_INGREDIENT)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseContract.ShoppingListEntry.COLUMN_QUANTITY)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.ShoppingListEntry.COLUMN_UNIT))
            );
            if (cursor.getColumnIndex(DatabaseContract.ShoppingListEntry.COLUMN_IS_CHECKED) != -1) {
                boolean isChecked = cursor.getInt(cursor.getColumnIndexOrThrow(
                        DatabaseContract.ShoppingListEntry.COLUMN_IS_CHECKED)) == 1;
                ingredient.setChecked(isChecked);
            }
            list.add(ingredient);
        }
        cursor.close();
        return list;
    }

    private void restoreShoppingList(SQLiteDatabase db, List<RecipeIngredient> shoppingList) {
        for (RecipeIngredient ingredient : shoppingList) {
            ContentValues values = new ContentValues();
            values.put(DatabaseContract.ShoppingListEntry.COLUMN_INGREDIENT, ingredient.getName());
            values.put(DatabaseContract.ShoppingListEntry.COLUMN_QUANTITY, ingredient.getQuantity());
            values.put(DatabaseContract.ShoppingListEntry.COLUMN_UNIT, ingredient.getUnit());
            values.put(DatabaseContract.ShoppingListEntry.COLUMN_IS_CHECKED, ingredient.isChecked() ? 1 : 0);
            db.insert(DatabaseContract.ShoppingListEntry.TABLE_NAME, null, values);
        }
    }

    public float getAverageRating(int recipeId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT AVG(" + DatabaseContract.RatingEntry.COLUMN_RATING + ") as avg_rating" +
                " FROM " + DatabaseContract.RatingEntry.TABLE_NAME +
                " WHERE " + DatabaseContract.RatingEntry.COLUMN_RECIPE_ID + " = ?";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(recipeId)});
        float avgRating = 0f;
        if (cursor.moveToFirst()) {
            avgRating = cursor.getFloat(0);
        }
        cursor.close();
        return avgRating;
    }

    // Méthode pour ajouter un ingrédient à une recette
    public long addIngredientToRecipe(long recipeId, String name, double quantity, String unit) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.RecipeIngredientEntry.COLUMN_RECIPE_ID, recipeId);
        values.put(DatabaseContract.RecipeIngredientEntry.COLUMN_NAME, name);
        values.put(DatabaseContract.RecipeIngredientEntry.COLUMN_QUANTITY, quantity);
        values.put(DatabaseContract.RecipeIngredientEntry.COLUMN_UNIT, unit);
        return db.insert(DatabaseContract.RecipeIngredientEntry.TABLE_NAME, null, values);
    }

    // Méthode pour supprimer un ingrédient
    public void deleteIngredient(long ingredientId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(
                DatabaseContract.RecipeIngredientEntry.TABLE_NAME,
                DatabaseContract.RecipeIngredientEntry._ID + " = ?",
                new String[]{String.valueOf(ingredientId)}
        );
    }

    public List<RecipeIngredient> getRecipeIngredients(long recipeId) {
        List<RecipeIngredient> ingredients = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String selection = DatabaseContract.RecipeIngredientEntry.COLUMN_RECIPE_ID + " = ?";
        String[] selectionArgs = {String.valueOf(recipeId)};

        Cursor cursor = db.query(
                DatabaseContract.RecipeIngredientEntry.TABLE_NAME,
                null,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        while (cursor.moveToNext()) {
            RecipeIngredient ingredient = new RecipeIngredient(
                    cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseContract.RecipeIngredientEntry._ID)),
                    recipeId,
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.RecipeIngredientEntry.COLUMN_NAME)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseContract.RecipeIngredientEntry.COLUMN_QUANTITY)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.RecipeIngredientEntry.COLUMN_UNIT))
            );
            ingredients.add(ingredient);
        }

        cursor.close();
        return ingredients;
    }

    // Méthode pour mettre à jour un ingrédient
    public int updateIngredient(RecipeIngredient ingredient) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.RecipeIngredientEntry.COLUMN_NAME, ingredient.getName());
        values.put(DatabaseContract.RecipeIngredientEntry.COLUMN_QUANTITY, ingredient.getQuantity());
        values.put(DatabaseContract.RecipeIngredientEntry.COLUMN_UNIT, ingredient.getUnit());

        return db.update(
                DatabaseContract.RecipeIngredientEntry.TABLE_NAME,
                values,
                DatabaseContract.RecipeIngredientEntry._ID + " = ?",
                new String[]{String.valueOf(ingredient.getId())}
        );
    }


    public List<Recipe> getFilteredRecipes(UserPreferences userPrefs, String searchQuery, String selectedDifficulty, int maxTime) {
        List<Recipe> allRecipes = getAllRecipes();
        List<Recipe> filteredRecipes = new ArrayList<>();
        if (userPrefs != null) {
        }

        for (Recipe recipe : allRecipes) {
            boolean isCompatible = true;
            boolean matchesSearch = true;
            boolean matchesDifficulty = true;
            boolean matchesTime = true;

            // Filtre régime alimentaire
            if (userPrefs != null && userPrefs.getDietType() != null && !userPrefs.getDietType().equals("none")) {
                String recipeDietType = recipe.getDietType();
                // Les recettes végétaliennes sont aussi compatibles avec le régime végétarien
                if (userPrefs.getDietType().equals("vegetarian")) {
                    isCompatible = recipeDietType.equals("vegetarian") || recipeDietType.equals("vegan");
                } else {
                    isCompatible = recipeDietType.equals(userPrefs.getDietType());
                }
            }

            // Filtre allergènes
            if (userPrefs != null && userPrefs.getAllergies() != null && !userPrefs.getAllergies().isEmpty()) {
                List<String> recipeAllergens = recipe.getAllergens();
                if (recipeAllergens != null) {
                    for (String allergen : userPrefs.getAllergies()) {
                        if (recipeAllergens.contains(allergen.toLowerCase())) {
                            isCompatible = false;
                            break;
                        }
                    }
                }
            }

            // Filtre recherche
            if (searchQuery != null && !searchQuery.isEmpty()) {
                matchesSearch = recipe.getName().toLowerCase().contains(searchQuery.toLowerCase()) ||
                        recipe.getDescription().toLowerCase().contains(searchQuery.toLowerCase());
            }

            // Filtre difficulté
            if (selectedDifficulty != null && !selectedDifficulty.equals("Tous")) {
                matchesDifficulty = recipe.getDifficulty().equals(selectedDifficulty);
            }

            // Filtre temps
            if (maxTime > 0) {
                matchesTime = recipe.getCookingTime() <= maxTime;
            }

            if (isCompatible && matchesSearch && matchesDifficulty && matchesTime) {
                filteredRecipes.add(recipe);
            }
        }

        return filteredRecipes;
    }

    public float getUserRating(int userId, int recipeId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + DatabaseContract.RatingEntry.COLUMN_RATING +
                " FROM " + DatabaseContract.RatingEntry.TABLE_NAME +
                " WHERE " + DatabaseContract.RatingEntry.COLUMN_USER_ID + " = ? AND " +
                DatabaseContract.RatingEntry.COLUMN_RECIPE_ID + " = ?";

        Cursor cursor = db.rawQuery(query,
                new String[]{String.valueOf(userId), String.valueOf(recipeId)});
        float rating = 0f;
        if (cursor.moveToFirst()) {
            rating = cursor.getFloat(0);
        }
        cursor.close();
        return rating;
    }

    public int getRatingCount(int recipeId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM " + DatabaseContract.RatingEntry.TABLE_NAME +
                " WHERE " + DatabaseContract.RatingEntry.COLUMN_RECIPE_ID + " = ?";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(recipeId)});
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    public void rateRecipe(int userId, int recipeId, float rating) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.RatingEntry.COLUMN_USER_ID, userId);
        values.put(DatabaseContract.RatingEntry.COLUMN_RECIPE_ID, recipeId);
        values.put(DatabaseContract.RatingEntry.COLUMN_RATING, rating);

        // Insert ou update si la note existe déjà
        db.insertWithOnConflict(DatabaseContract.RatingEntry.TABLE_NAME, null, values,
                SQLiteDatabase.CONFLICT_REPLACE);
    }

    public Recipe getRecipe(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = DatabaseContract.RecipeEntry._ID + " = ?";
        String[] selectionArgs = {String.valueOf(id)};

        Cursor cursor = db.query(
                DatabaseContract.RecipeEntry.TABLE_NAME,
                null,
                selection,
                selectionArgs,
                null,
                null,
                null
        );
        Recipe recipe = null;
        if (cursor != null && cursor.moveToFirst()) {
            recipe = cursorToRecipe(cursor);
            cursor.close();
        }
        return recipe;
    }

    public List<Recipe> getAllRecipes() {
        List<Recipe> recipes = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Récupérer uniquement les recettes non communautaires (is_community = 0)
        String selection = DatabaseContract.RecipeEntry.COLUMN_IS_COMMUNITY + " = ?";
        String[] selectionArgs = {"0"};  // 0 pour les recettes non communautaires

        Cursor cursor = db.query(
                DatabaseContract.RecipeEntry.TABLE_NAME,
                null,
                selection,
                selectionArgs,
                null, null,
                DatabaseContract.RecipeEntry._ID + " ASC"
        );

        while (cursor.moveToNext()) {
            recipes.add(cursorToRecipe(cursor));
        }

        cursor.close();
        return recipes;
    }

    public List<Recipe> getCommunityRecipes() {
        List<Recipe> recipes = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String selection = DatabaseContract.RecipeEntry.COLUMN_IS_COMMUNITY + " = ?";
        String[] selectionArgs = {"1"};

        Cursor cursor = db.query(
                DatabaseContract.RecipeEntry.TABLE_NAME,
                null,
                selection,
                selectionArgs,
                null,
                null,
                DatabaseContract.RecipeEntry.COLUMN_CREATION_DATE + " DESC"
        );

        while (cursor.moveToNext()) {
            recipes.add(cursorToRecipe(cursor));
        }

        cursor.close();
        return recipes;
    }

    public void deleteRecipe(long recipeId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(DatabaseContract.RecipeEntry.TABLE_NAME,
                DatabaseContract.RecipeEntry._ID + " = ?",
                new String[]{String.valueOf(recipeId)});
    }


    public boolean isRecipeFavorite(int userId, int recipeId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query("favorites", null,
                "user_id = ? AND recipe_id = ?",
                new String[]{String.valueOf(userId), String.valueOf(recipeId)},
                null, null, null);

        boolean isFavorite = cursor.getCount() > 0;
        cursor.close();
        return isFavorite;
    }

    private Recipe cursorToRecipe(Cursor cursor) {
        Recipe recipe = new Recipe(
                cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseContract.RecipeEntry._ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.RecipeEntry.COLUMN_NAME)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.RecipeEntry.COLUMN_DESCRIPTION)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.RecipeEntry.COLUMN_STEPS)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.RecipeEntry.COLUMN_IMAGE_URL)),
                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.RecipeEntry.COLUMN_COOKING_TIME)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.RecipeEntry.COLUMN_DIFFICULTY)),
                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.RecipeEntry.COLUMN_SERVINGS)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.RecipeEntry.COLUMN_AUTHOR)),
                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.RecipeEntry.COLUMN_USER_ID))
        );

        String dietType = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.RecipeEntry.COLUMN_DIET_TYPE));
        String allergensJson = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.RecipeEntry.COLUMN_ALLERGENS));

        recipe.setDietType(dietType);
        if (allergensJson != null) {
            recipe.setAllergensFromJson(allergensJson);
        }
        // Définir le statut community
        int isCommunity = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.RecipeEntry.COLUMN_IS_COMMUNITY));
        recipe.setCommunity(true);

        // Charger les notes
        float avgRating = getAverageRating((int) recipe.getId());
        int ratingCount = getRatingCount((int) recipe.getId());
        recipe.setAverageRating(avgRating);
        recipe.setRatingCount(ratingCount);

        return recipe;
    }

    private static final String SQL_CREATE_USER_PREFERENCES =
            "CREATE TABLE " + DatabaseContract.UserPreferencesEntry.TABLE_NAME + " (" +
                    DatabaseContract.UserPreferencesEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    DatabaseContract.UserPreferencesEntry.COLUMN_USER_ID + " INTEGER UNIQUE," +
                    DatabaseContract.UserPreferencesEntry.COLUMN_ALLERGIES + " TEXT," +
                    DatabaseContract.UserPreferencesEntry.COLUMN_DIET_TYPE + " TEXT," +
                    DatabaseContract.UserPreferencesEntry.COLUMN_COOKING_EXPERIENCE + " TEXT," +
                    "FOREIGN KEY(" + DatabaseContract.UserPreferencesEntry.COLUMN_USER_ID + ") REFERENCES users(id))";

    // Méthodes pour gérer les préférences
    public void saveUserPreferences(int userId, UserPreferences prefs) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.UserPreferencesEntry.COLUMN_USER_ID, userId);
        values.put(DatabaseContract.UserPreferencesEntry.COLUMN_ALLERGIES, prefs.getAllergiesJson());
        values.put(DatabaseContract.UserPreferencesEntry.COLUMN_DIET_TYPE, prefs.getDietType());
        values.put(DatabaseContract.UserPreferencesEntry.COLUMN_COOKING_EXPERIENCE, prefs.getCookingExperience());

        db.insertWithOnConflict(DatabaseContract.UserPreferencesEntry.TABLE_NAME, null, values,
                SQLiteDatabase.CONFLICT_REPLACE);
    }

    public UserPreferences getUserPreferences(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        UserPreferences prefs = new UserPreferences();

        Cursor cursor = db.query(DatabaseContract.UserPreferencesEntry.TABLE_NAME,
                null,
                DatabaseContract.UserPreferencesEntry.COLUMN_USER_ID + " = ?",
                new String[]{String.valueOf(userId)},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            String allergiesJson = cursor.getString(
                    cursor.getColumnIndexOrThrow(DatabaseContract.UserPreferencesEntry.COLUMN_ALLERGIES));
            if (allergiesJson != null) {
                prefs.setAllergiesFromJson(allergiesJson);
            }
            prefs.setDietType(cursor.getString(
                    cursor.getColumnIndexOrThrow(DatabaseContract.UserPreferencesEntry.COLUMN_DIET_TYPE)));
            prefs.setCookingExperience(cursor.getString(
                    cursor.getColumnIndexOrThrow(DatabaseContract.UserPreferencesEntry.COLUMN_COOKING_EXPERIENCE)));

            cursor.close();
        }
        return prefs;
    }


    public long createCommunityRecipe(Recipe recipe, int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.RecipeEntry.COLUMN_NAME, recipe.getName());
        values.put(DatabaseContract.RecipeEntry.COLUMN_DESCRIPTION, recipe.getDescription());
        values.put(DatabaseContract.RecipeEntry.COLUMN_STEPS, recipe.getSteps());
        values.put(DatabaseContract.RecipeEntry.COLUMN_IMAGE_URL, recipe.getImageUrl());
        values.put(DatabaseContract.RecipeEntry.COLUMN_COOKING_TIME, recipe.getCookingTime());
        values.put(DatabaseContract.RecipeEntry.COLUMN_DIFFICULTY, recipe.getDifficulty());
        values.put(DatabaseContract.RecipeEntry.COLUMN_SERVINGS, recipe.getServings());
        values.put(DatabaseContract.RecipeEntry.COLUMN_USER_ID, userId);
        values.put(DatabaseContract.RecipeEntry.COLUMN_IS_COMMUNITY, 1);
        values.put(DatabaseContract.RecipeEntry.COLUMN_DIET_TYPE, recipe.getDietType());  // Ajout du régime
        values.put(DatabaseContract.RecipeEntry.COLUMN_ALLERGENS, recipe.getAllergensJson()); // Ajout des allergènes

        String username = getUserName(userId);
        values.put(DatabaseContract.RecipeEntry.COLUMN_AUTHOR, username);

        return db.insert(DatabaseContract.RecipeEntry.TABLE_NAME, null, values);
    }

    private String getUserName(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query("users", new String[]{"username"},
                "id = ?", new String[]{String.valueOf(userId)},
                null, null, null);

        String username = "";
        if (cursor.moveToFirst()) {
            username = cursor.getString(0);
        }
        cursor.close();
        return username;
    }

    public List<Recipe> getUserRecipes(int userId) {
        List<Recipe> userRecipes = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(
                DatabaseContract.RecipeEntry.TABLE_NAME,
                null,
                DatabaseContract.RecipeEntry.COLUMN_USER_ID + " = ?",
                new String[]{String.valueOf(userId)},
                null,
                null,
                DatabaseContract.RecipeEntry.COLUMN_CREATION_DATE + " DESC"
        );

        while (cursor.moveToNext()) {
            Recipe recipe = cursorToRecipe(cursor);
            userRecipes.add(recipe);
        }
        cursor.close();

        return userRecipes;
    }

    public List<Recipe> findRecipesByIngredients(List<String> searchTerms) {
        List<Recipe> recipes = getAllRecipes();
        List<Recipe> matchingRecipes = new ArrayList<>();

        for (Recipe recipe : recipes) {
            for (String term : searchTerms) {
                if (recipe.getName().toLowerCase().contains(term.toLowerCase()) ||
                        recipe.getDescription().toLowerCase().contains(term.toLowerCase()) ||
                        recipe.getSteps().toLowerCase().contains(term.toLowerCase())) {
                    matchingRecipes.add(recipe);
                    break;
                }
            }
        }

        return matchingRecipes;
    }

    public long registerUser(String username, String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("username", username);
        values.put("email", email);
        values.put("password", password);
        return db.insert("users", null, values);
    }

    public void addFavorite(int userId, int recipeId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("recipe_id", recipeId);
        db.insert("favorites", null, values);
    }

    public void removeFavorite(int userId, int recipeId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("favorites",
                "user_id = ? AND recipe_id = ?",
                new String[]{String.valueOf(userId), String.valueOf(recipeId)});
    }

    public List<Recipe> getFavorites(int userId) {
        List<Recipe> favorites = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT r.* FROM " + DatabaseContract.RecipeEntry.TABLE_NAME + " r " +
                "INNER JOIN favorites f ON r." + DatabaseContract.RecipeEntry._ID + " = f.recipe_id " +
                "WHERE f.user_id = ?";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

        while (cursor.moveToNext()) {
            favorites.add(cursorToRecipe(cursor));
        }
        cursor.close();

        return favorites;
    }

    public boolean isFavorite(int userId, int recipeId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query("favorites", null,
                "user_id = ? AND recipe_id = ?",
                new String[]{String.valueOf(userId), String.valueOf(recipeId)},
                null, null, null);

        boolean isFavorite = cursor.getCount() > 0;
        cursor.close();
        return isFavorite;
    }

    public User loginUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();

        // Utiliser des paramètres préparés pour éviter les injections SQL
        String selection = "username = ? AND password = ?";
        String[] selectionArgs = {username, password};

        try {
            Cursor cursor = db.query(
                    "users",
                    null,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    null
            );

            User user = null;
            if (cursor != null && cursor.moveToFirst()) {
                user = new User(
                        cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("username")),
                        cursor.getString(cursor.getColumnIndexOrThrow("email")),
                        cursor.getString(cursor.getColumnIndexOrThrow("password"))
                );
                cursor.close();
            }
            return user;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public void toggleFavorite(int userId, int recipeId) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.query("favorites", null,
                "user_id = ? AND recipe_id = ?",
                new String[]{String.valueOf(userId), String.valueOf(recipeId)},
                null, null, null);

        if (cursor.moveToFirst()) {
            db.delete("favorites",
                    "user_id = ? AND recipe_id = ?",
                    new String[]{String.valueOf(userId), String.valueOf(recipeId)});
        } else {
            ContentValues values = new ContentValues();
            values.put("user_id", userId);
            values.put("recipe_id", recipeId);
            db.insert("favorites", null, values);
        }
        cursor.close();
    }

    private void loadUserFavorites(User user) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query("favorites", new String[]{"recipe_id"},
                "user_id = ?", new String[]{String.valueOf(user.getId())},
                null, null, null);

        while (cursor.moveToNext()) {
            user.addFavoriteRecipe(cursor.getInt(0));
        }
        cursor.close();
    }

    private static final String SQL_CREATE_CHAT_HISTORY =
            "CREATE TABLE " + DatabaseContract.ChatHistoryEntry.TABLE_NAME + " (" +
                    DatabaseContract.ChatHistoryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    DatabaseContract.ChatHistoryEntry.COLUMN_USER_ID + " INTEGER," +
                    DatabaseContract.ChatHistoryEntry.COLUMN_MESSAGE + " TEXT," +
                    DatabaseContract.ChatHistoryEntry.COLUMN_IS_USER + " INTEGER," +
                    DatabaseContract.ChatHistoryEntry.COLUMN_TIMESTAMP + " INTEGER)";


    public void updateRecipe(Recipe recipe) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.RecipeEntry.COLUMN_NAME, recipe.getName());
        values.put(DatabaseContract.RecipeEntry.COLUMN_DESCRIPTION, recipe.getDescription());
        values.put(DatabaseContract.RecipeEntry.COLUMN_STEPS, recipe.getSteps());
        values.put(DatabaseContract.RecipeEntry.COLUMN_IMAGE_URL, recipe.getImageUrl());
        values.put(DatabaseContract.RecipeEntry.COLUMN_COOKING_TIME, recipe.getCookingTime());
        values.put(DatabaseContract.RecipeEntry.COLUMN_DIFFICULTY, recipe.getDifficulty());
        values.put(DatabaseContract.RecipeEntry.COLUMN_SERVINGS, recipe.getServings());
        values.put(DatabaseContract.RecipeEntry.COLUMN_DIET_TYPE, recipe.getDietType()); // Mise à jour du régime
        values.put(DatabaseContract.RecipeEntry.COLUMN_ALLERGENS, recipe.getAllergensJson()); // Mise à jour des allergènes

        db.update(DatabaseContract.RecipeEntry.TABLE_NAME, values,
                DatabaseContract.RecipeEntry._ID + " = ?",
                new String[]{String.valueOf(recipe.getId())});
        db.close();
    }


    public boolean updateUserProfile(int userId, String newUsername, String newEmail) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("username", newUsername);
        values.put("email", newEmail);

        int rowsAffected = db.update("users", values, "id = ?",
                new String[]{String.valueOf(userId)});
        return rowsAffected > 0;
    }

    public boolean updateUserPassword(int userId, String currentPassword, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Vérifier l'ancien mot de passe
        Cursor cursor = db.query("users", null, "id = ? AND password = ?",
                new String[]{String.valueOf(userId), currentPassword},
                null, null, null);

        if (cursor.moveToFirst()) {
            ContentValues values = new ContentValues();
            values.put("password", newPassword);

            int rowsAffected = db.update("users", values, "id = ?",
                    new String[]{String.valueOf(userId)});
            cursor.close();
            return rowsAffected > 0;
        }

        cursor.close();
        return false;
    }
    public void updateShoppingListQuantity(long id, double newQuantity, String newUnit) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put(DatabaseContract.ShoppingListEntry.COLUMN_QUANTITY, newQuantity);
            values.put(DatabaseContract.ShoppingListEntry.COLUMN_UNIT, newUnit);
            db.update(DatabaseContract.ShoppingListEntry.TABLE_NAME,
                    values,
                    DatabaseContract.ShoppingListEntry._ID + " = ?",
                    new String[]{String.valueOf(id)});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private static final String SQL_CREATE_SHOPPING_LIST =
            "CREATE TABLE " + DatabaseContract.ShoppingListEntry.TABLE_NAME + " (" +
                    DatabaseContract.ShoppingListEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    DatabaseContract.ShoppingListEntry.COLUMN_INGREDIENT + " TEXT," +
                    DatabaseContract.ShoppingListEntry.COLUMN_QUANTITY + " REAL," +
                    DatabaseContract.ShoppingListEntry.COLUMN_UNIT + " TEXT," +
                    DatabaseContract.ShoppingListEntry.COLUMN_RECIPE_NAME + " TEXT," +
                    DatabaseContract.ShoppingListEntry.COLUMN_CATEGORY + " TEXT," +
                    DatabaseContract.ShoppingListEntry.COLUMN_IS_CHECKED + " INTEGER DEFAULT 0)";
    public List<RecipeIngredient> getShoppingList() {
        List<RecipeIngredient> shoppingList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(DatabaseContract.ShoppingListEntry.TABLE_NAME, null, null, null, null, null, null);

        while (cursor.moveToNext()) {
            RecipeIngredient ingredient = new RecipeIngredient(
                    cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseContract.ShoppingListEntry._ID)),
                    0,
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.ShoppingListEntry.COLUMN_INGREDIENT)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseContract.ShoppingListEntry.COLUMN_QUANTITY)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.ShoppingListEntry.COLUMN_UNIT))
            );
            // Récupérer l'état coché
            boolean isChecked = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.ShoppingListEntry.COLUMN_IS_CHECKED)) == 1;
            if (isChecked) {
                ingredient.setChecked(true);
            }
            shoppingList.add(ingredient);
        }
        cursor.close();
        return shoppingList;
    }
    public void updateShoppingListQuantity(long id, double newQuantity) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.ShoppingListEntry.COLUMN_QUANTITY, newQuantity);
        db.update(DatabaseContract.ShoppingListEntry.TABLE_NAME,
                values,
                DatabaseContract.ShoppingListEntry._ID + " = ?",
                new String[]{String.valueOf(id)});
    }
    public void updateShoppingListCheckedStatus(long id, boolean isChecked) {
        if (isChecked) {
            // Si l'élément est coché, on le supprime
            deleteFromShoppingList(id);
        }
    }

    public void clearShoppingList() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(DatabaseContract.ShoppingListEntry.TABLE_NAME, null, null);
    }

    public void deleteFromShoppingList(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(DatabaseContract.ShoppingListEntry.TABLE_NAME,
                DatabaseContract.ShoppingListEntry._ID + " = ?",
                new String[]{String.valueOf(id)});
    }

    public void addToShoppingList(String recipeName, List<RecipeIngredient> ingredients) {
        SQLiteDatabase db = this.getWritableDatabase();

        for (RecipeIngredient ingredient : ingredients) {
            // Vérifier si l'ingrédient existe déjà avec la même unité
            Cursor cursor = db.query(DatabaseContract.ShoppingListEntry.TABLE_NAME,
                    new String[]{DatabaseContract.ShoppingListEntry._ID,
                            DatabaseContract.ShoppingListEntry.COLUMN_QUANTITY},
                    DatabaseContract.ShoppingListEntry.COLUMN_INGREDIENT + " = ? AND " +
                            DatabaseContract.ShoppingListEntry.COLUMN_UNIT + " = ?",
                    new String[]{ingredient.getName(), ingredient.getUnit()},
                    null, null, null);

            if (cursor.moveToFirst()) {
                // Si l'ingrédient existe, ajouter les quantités
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(
                        DatabaseContract.ShoppingListEntry._ID));
                double existingQuantity = cursor.getDouble(cursor.getColumnIndexOrThrow(
                        DatabaseContract.ShoppingListEntry.COLUMN_QUANTITY));
                double newQuantity = ingredient.getQuantity() + existingQuantity;

                ContentValues updateValues = new ContentValues();
                updateValues.put(DatabaseContract.ShoppingListEntry.COLUMN_QUANTITY, newQuantity);

                db.update(DatabaseContract.ShoppingListEntry.TABLE_NAME,
                        updateValues,
                        DatabaseContract.ShoppingListEntry._ID + " = ?",
                        new String[]{String.valueOf(id)});
            } else {
                // Si l'ingrédient n'existe pas, l'ajouter
                ContentValues values = new ContentValues();
                values.put(DatabaseContract.ShoppingListEntry.COLUMN_INGREDIENT,
                        ingredient.getName());
                values.put(DatabaseContract.ShoppingListEntry.COLUMN_QUANTITY,
                        ingredient.getQuantity());
                values.put(DatabaseContract.ShoppingListEntry.COLUMN_UNIT,
                        ingredient.getUnit());
                values.put(DatabaseContract.ShoppingListEntry.COLUMN_RECIPE_NAME,
                        recipeName);
                values.put(DatabaseContract.ShoppingListEntry.COLUMN_IS_CHECKED, 0);
                db.insert(DatabaseContract.ShoppingListEntry.TABLE_NAME, null, values);
            }
            cursor.close();
        }
    }
    private String generatePlaceholders(int count) {
        StringBuilder placeholders = new StringBuilder();
        for (int i = 0; i < count; i++) {
            if (i > 0) placeholders.append(",");
            placeholders.append("?");
        }
        return placeholders.toString();
    }
}