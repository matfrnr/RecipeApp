package com.example.recipeapp.database;

import android.provider.BaseColumns;

public final class DatabaseContract {
    private DatabaseContract() {
    }

    public static class RecipeEntry implements BaseColumns {
        public static final String TABLE_NAME = "recipes";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_STEPS = "steps";
        public static final String COLUMN_IMAGE_URL = "image_url";
        public static final String COLUMN_COOKING_TIME = "cooking_time";
        public static final String COLUMN_DIFFICULTY = "difficulty";
        public static final String COLUMN_SERVINGS = "servings";
        public static final String COLUMN_CREATION_DATE = "creation_date";
        public static final String COLUMN_USER_ID = "user_id";
        public static final String COLUMN_IS_COMMUNITY = "is_community";
        public static final String COLUMN_AUTHOR = "author";
        public static final String COLUMN_DIET_TYPE = "diet_type";
        public static final String COLUMN_ALLERGENS = "allergens";
    }

    private static final String SQL_CREATE_SHOPPING_LIST =
            "CREATE TABLE " + DatabaseContract.ShoppingListEntry.TABLE_NAME + " (" +
                    DatabaseContract.ShoppingListEntry._ID + " INTEGER PRIMARY KEY," +
                    DatabaseContract.ShoppingListEntry.COLUMN_INGREDIENT + " TEXT," +
                    DatabaseContract.ShoppingListEntry.COLUMN_QUANTITY + " INTEGER," +
                    DatabaseContract.ShoppingListEntry.COLUMN_UNIT + " TEXT," +
                    DatabaseContract.ShoppingListEntry.COLUMN_RECIPE_NAME + " TEXT," +
                    DatabaseContract.ShoppingListEntry.COLUMN_IS_CHECKED + " INTEGER DEFAULT 0)";

    public static class UserPreferencesEntry implements BaseColumns {
        public static final String TABLE_NAME = "user_preferences";
        public static final String COLUMN_USER_ID = "user_id";
        public static final String COLUMN_ALLERGIES = "allergies";
        public static final String COLUMN_DIET_TYPE = "diet_type";
        public static final String COLUMN_COOKING_EXPERIENCE = "cooking_experience";
    }


    public static class IngredientEntry implements BaseColumns {
        public static final String TABLE_NAME = "ingredients";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_CATEGORY = "category";
    }

    public static class RecipeIngredientEntry implements BaseColumns {
        public static final String TABLE_NAME = "recipe_ingredients";
        public static final String COLUMN_RECIPE_ID = "recipe_id";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_QUANTITY = "quantity";
        public static final String COLUMN_UNIT = "unit";
    }

    public static class ChatHistoryEntry implements BaseColumns {
        public static final String TABLE_NAME = "chat_history";
        public static final String COLUMN_USER_ID = "user_id";
        public static final String COLUMN_MESSAGE = "message";
        public static final String COLUMN_IS_USER = "is_user";
        public static final String COLUMN_TIMESTAMP = "timestamp";
    }

    public static class RatingEntry implements BaseColumns {
        public static final String TABLE_NAME = "ratings";
        public static final String COLUMN_USER_ID = "user_id";
        public static final String COLUMN_RECIPE_ID = "recipe_id";
        public static final String COLUMN_RATING = "rating";
        public static final String COLUMN_TIMESTAMP = "timestamp";
    }

    public static class ShoppingListEntry implements BaseColumns {
        public static final String TABLE_NAME = "shopping_list";
        public static final String COLUMN_INGREDIENT = "ingredient";
        public static final String COLUMN_QUANTITY = "quantity";
        public static final String COLUMN_UNIT = "unit";
        public static final String COLUMN_RECIPE_NAME = "recipe_name";
        public static final String COLUMN_CATEGORY = "category";
        public static final String COLUMN_IS_CHECKED = "is_checked";  // S'assurer que cette ligne existe
    }
    public static class CategoryEntry implements BaseColumns {
        public static final String TABLE_NAME = "categories";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_DESCRIPTION = "description";
    }

    public static class CommentEntry implements BaseColumns {
        public static final String TABLE_NAME = "comments";
        public static final String COLUMN_USER_ID = "user_id";
        public static final String COLUMN_RECIPE_ID = "recipe_id";
        public static final String COLUMN_CONTENT = "content";
        public static final String COLUMN_TIMESTAMP = "timestamp";
        public static final String COLUMN_USERNAME = "username";
        public static final String COLUMN_IS_EDITED = "is_edited";

    }
}