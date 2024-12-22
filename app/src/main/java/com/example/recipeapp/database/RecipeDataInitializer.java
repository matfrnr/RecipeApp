package com.example.recipeapp.database;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import com.example.recipeapp.models.Recipe;
import com.example.recipeapp.models.RecipeIngredient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RecipeDataInitializer {

    private static void addIngredientsToRecipe(SQLiteDatabase db, long recipeId, List<RecipeIngredient> ingredients) {
        for (RecipeIngredient ingredient : ingredients) {
            ContentValues values = new ContentValues();
            values.put(DatabaseContract.RecipeIngredientEntry.COLUMN_RECIPE_ID, recipeId);
            values.put(DatabaseContract.RecipeIngredientEntry.COLUMN_NAME, ingredient.getName());
            values.put(DatabaseContract.RecipeIngredientEntry.COLUMN_QUANTITY, ingredient.getQuantity());
            values.put(DatabaseContract.RecipeIngredientEntry.COLUMN_UNIT, ingredient.getUnit());
            db.insert(DatabaseContract.RecipeIngredientEntry.TABLE_NAME, null, values);
        }
    }

    private static void insertRecipeWithIngredients(SQLiteDatabase db, Recipe recipe, List<RecipeIngredient> ingredients) {
        // Insérer la recette
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.RecipeEntry._ID, recipe.getId());
        values.put(DatabaseContract.RecipeEntry.COLUMN_NAME, recipe.getName());
        values.put(DatabaseContract.RecipeEntry.COLUMN_DESCRIPTION, recipe.getDescription());
        values.put(DatabaseContract.RecipeEntry.COLUMN_STEPS, recipe.getSteps());
        values.put(DatabaseContract.RecipeEntry.COLUMN_IMAGE_URL, recipe.getImageUrl());
        values.put(DatabaseContract.RecipeEntry.COLUMN_COOKING_TIME, recipe.getCookingTime());
        values.put(DatabaseContract.RecipeEntry.COLUMN_DIFFICULTY, recipe.getDifficulty());
        values.put(DatabaseContract.RecipeEntry.COLUMN_SERVINGS, recipe.getServings());
        values.put(DatabaseContract.RecipeEntry.COLUMN_AUTHOR, recipe.getAuthor());
        values.put(DatabaseContract.RecipeEntry.COLUMN_USER_ID, recipe.getUserId());
        values.put(DatabaseContract.RecipeEntry.COLUMN_DIET_TYPE, recipe.getDietType());
        values.put(DatabaseContract.RecipeEntry.COLUMN_ALLERGENS, recipe.getAllergensJson());

        long recipeId = db.insert(DatabaseContract.RecipeEntry.TABLE_NAME, null, values);

        // Ajouter les ingrédients
        if (recipeId != -1) {
            addIngredientsToRecipe(db, recipeId, ingredients);
        }
    }

    public static void insertInitialRecipes(SQLiteDatabase db) {
        Recipe crepes = new Recipe(
                1,
                "Crêpes classiques",
                "Recette traditionnelle de crêpes françaises",
                "1. Mélanger la farine et les œufs\n2. Ajouter le lait progressivement\n3. Cuire dans une poêle chaude",
                "@drawable/crepes",
                30,
                "Facile",
                6,
                "Admin",
                0
        );
        crepes.setDietType("vegetarian");
        List<String> allergenesCrepes = new ArrayList<>();
        allergenesCrepes.add("lactose");
        allergenesCrepes.add("gluten");
        allergenesCrepes.add("oeufs");
        crepes.setAllergens(allergenesCrepes);
        List<RecipeIngredient> crepesIngredients = Arrays.asList(
                new RecipeIngredient(0, 1, "Farine", 250, "g"),
                new RecipeIngredient(0, 1, "Œufs", 4, ""),
                new RecipeIngredient(0, 1, "Lait", 500, "ml"),
                new RecipeIngredient(0, 1, "Beurre fondu", 50, "g"),
                new RecipeIngredient(0, 1, "Sel", 1, "pincée")
        );
        insertRecipeWithIngredients(db, crepes, crepesIngredients);

        Recipe ratatouille = new Recipe(
                2,
                "Ratatouille",
                "Un plat traditionnel provençal de légumes mijotés",
                "1. Couper les légumes en rondelles\n2. Faire revenir les oignons\n3. Ajouter les autres légumes\n4. Mijoter 45 minutes",
                "@drawable/crepes",
                60,
                "Moyen",
                4,
                "Admin",
                0
        );
        ratatouille.setDietType("vegetarian");
        List<String> allergenesRatatouille = new ArrayList<>();
        allergenesRatatouille.add("céleri"); // Optionnel selon les ingrédients
        ratatouille.setAllergens(allergenesRatatouille);
        List<RecipeIngredient> ratatouilleIngredients = Arrays.asList(
                new RecipeIngredient(0, 2, "Aubergines", 2, ""),
                new RecipeIngredient(0, 2, "Courgettes", 2, ""),
                new RecipeIngredient(0, 2, "Poivrons rouges", 2, ""),
                new RecipeIngredient(0, 2, "Tomates", 4, ""),
                new RecipeIngredient(0, 2, "Oignon", 1, ""),
                new RecipeIngredient(0, 2, "Huile d'olive", 3, "c. à soupe"),
                new RecipeIngredient(0, 2, "Ail", 2, "gousses"),
                new RecipeIngredient(0, 2, "Herbes de Provence", 1, "c. à soupe")
        );
        insertRecipeWithIngredients(db, ratatouille, ratatouilleIngredients);

        Recipe quicheLorraine = new Recipe(
                3,
                "Quiche Lorraine",
                "Tarte salée aux lardons et fromage",
                "1. Préparer la pâte\n2. Faire revenir les lardons\n3. Mélanger œufs et crème\n4. Cuire 30 minutes",
                "@drawable/crepes",
                45,
                "Moyen",
                6,
                "Admin",
                0
        );
        quicheLorraine.setDietType("none");
        List<String> allergenesQuiche = new ArrayList<>();
        allergenesQuiche.add("lactose");
        allergenesQuiche.add("gluten");
        allergenesQuiche.add("oeufs");
        quicheLorraine.setAllergens(allergenesQuiche);
        List<RecipeIngredient> quicheLorraineIngredients = Arrays.asList(
                new RecipeIngredient(0, 3, "Pâte brisée", 1, "rouleau"),
                new RecipeIngredient(0, 3, "Lardons", 200, "g"),
                new RecipeIngredient(0, 3, "Crème fraîche", 200, "ml"),
                new RecipeIngredient(0, 3, "Œufs", 3, ""),
                new RecipeIngredient(0, 3, "Gruyère râpé", 100, "g"),
                new RecipeIngredient(0, 3, "Sel", 1, "pincée"),
                new RecipeIngredient(0, 3, "Poivre", 1, "pincée")
        );
        insertRecipeWithIngredients(db, quicheLorraine, quicheLorraineIngredients);

        Recipe boeufBourguignon = new Recipe(
                4,
                "Bœuf Bourguignon",
                "Un classique de la cuisine française",
                "1. Faire revenir la viande\n2. Ajouter les légumes\n3. Mouiller au vin rouge\n4. Mijoter 2h30",
                "@drawable/crepes",
                180,
                "Difficile",
                6,
                "Admin",
                0
        );
        boeufBourguignon.setDietType("none");
        List<String> allergenesBoeuf = new ArrayList<>();
        allergenesBoeuf.add("céleri"); // Optionnel selon les ingrédients
        allergenesBoeuf.add("sulfites"); // Présents dans le vin rouge
        boeufBourguignon.setAllergens(allergenesBoeuf);
        List<RecipeIngredient> boeufBourguignonIngredients = Arrays.asList(
                new RecipeIngredient(0, 4, "Bœuf", 1, "kg"),
                new RecipeIngredient(0, 4, "Carottes", 3, ""),
                new RecipeIngredient(0, 4, "Oignons", 2, ""),
                new RecipeIngredient(0, 4, "Vin rouge", 750, "ml"),
                new RecipeIngredient(0, 4, "Bouquet garni", 1, ""),
                new RecipeIngredient(0, 4, "Champignons", 200, "g"),
                new RecipeIngredient(0, 4, "Lardons", 150, "g"),
                new RecipeIngredient(0, 4, "Farine", 2, "c. à soupe"),
                new RecipeIngredient(0, 4, "Huile d'olive", 2, "c. à soupe"),
                new RecipeIngredient(0, 4, "Sel", 1, "pincée"),
                new RecipeIngredient(0, 4, "Poivre", 1, "pincée")
        );
        insertRecipeWithIngredients(db, boeufBourguignon, boeufBourguignonIngredients);

        Recipe gratinDauphinois = new Recipe(
                5,
                "Gratin Dauphinois",
                "Gratin de pommes de terre à la crème",
                "1. Couper les pommes de terre\n2. Préparer la crème\n3. Superposer les couches\n4. Cuire 1h",
                "@drawable/crepes",
                75,
                "Moyen",
                6,
                "Admin",
                0
        );
        gratinDauphinois.setDietType("vegetarian");
        List<String> allergenesGratin = new ArrayList<>();
        allergenesGratin.add("lactose");
        gratinDauphinois.setAllergens(allergenesGratin);

        Recipe lasagnes = new Recipe(
                6,
                "Lasagnes",
                "Lasagnes traditionnelles à la bolognaise",
                "1. Préparer la sauce bolognaise\n2. Faire la béchamel\n3. Monter les couches\n4. Cuire 45min",
                "@drawable/crepes",
                90,
                "Difficile",
                8,
                "Admin",
                0
        );
        lasagnes.setDietType("none");
        List<String> allergenesLasagnes = new ArrayList<>();
        allergenesLasagnes.add("lactose");
        allergenesLasagnes.add("gluten");
        allergenesLasagnes.add("soja");
        lasagnes.setAllergens(allergenesLasagnes);
        List<RecipeIngredient> lasagnesIngredients = Arrays.asList(
                new RecipeIngredient(0, 6, "Viande hachée", 500, "g"),
                new RecipeIngredient(0, 6, "Feuilles de lasagnes", 12, ""),
                new RecipeIngredient(0, 6, "Tomates pelées", 400, "g"),
                new RecipeIngredient(0, 6, "Concentré de tomates", 2, "c. à soupe"),
                new RecipeIngredient(0, 6, "Oignon", 1, ""),
                new RecipeIngredient(0, 6, "Beurre", 50, "g"),
                new RecipeIngredient(0, 6, "Farine", 50, "g"),
                new RecipeIngredient(0, 6, "Lait", 500, "ml"),
                new RecipeIngredient(0, 6, "Gruyère râpé", 100, "g"),
                new RecipeIngredient(0, 6, "Sel", 1, "pincée"),
                new RecipeIngredient(0, 6, "Poivre", 1, "pincée")
        );
        insertRecipeWithIngredients(db, lasagnes, lasagnesIngredients);

        Recipe risotto = new Recipe(
                7,
                "Risotto aux Champignons",
                "Risotto crémeux aux champignons frais",
                "1. Faire revenir les champignons\n2. Cuire le riz\n3. Ajouter le bouillon progressivement",
                "@drawable/crepes",
                40,
                "Moyen",
                4,
                "Admin",
                0
        );
        risotto.setDietType("vegetarian");
        List<String> allergenesRisotto = new ArrayList<>();
        allergenesRisotto.add("lactose");
        risotto.setAllergens(allergenesRisotto);
        List<RecipeIngredient> risottoChampignonsIngredients = Arrays.asList(
                new RecipeIngredient(0, 7, "Riz Arborio", 300, "g"),
                new RecipeIngredient(0, 7, "Champignons de Paris", 200, "g"),
                new RecipeIngredient(0, 7, "Bouillon de légumes", 1, "litre"),
                new RecipeIngredient(0, 7, "Beurre", 50, "g"),
                new RecipeIngredient(0, 7, "Parmesan râpé", 50, "g"),
                new RecipeIngredient(0, 7, "Oignon", 1, ""),
                new RecipeIngredient(0, 7, "Huile d'olive", 2, "c. à soupe"),
                new RecipeIngredient(0, 7, "Sel", 1, "pincée"),
                new RecipeIngredient(0, 7, "Poivre", 1, "pincée")
        );
        insertRecipeWithIngredients(db, risotto, risottoChampignonsIngredients);

        Recipe tartePommes = new Recipe(
                8,
                "Tarte aux Pommes",
                "Tarte sucrée aux pommes",
                "1. Préparer la pâte\n2. Couper les pommes\n3. Garnir et cuire",
                "@drawable/crepes",
                60,
                "Facile",
                8,
                "Admin",
                0
        );
        tartePommes.setDietType("vegetarian");
        List<String> allergenesTarte = new ArrayList<>();
        allergenesTarte.add("gluten");
        allergenesTarte.add("lactose");
        tartePommes.setAllergens(allergenesTarte);
        List<RecipeIngredient> tarteAuxPommesIngredients = Arrays.asList(
                new RecipeIngredient(0, 8, "Pommes", 4, ""),
                new RecipeIngredient(0, 8, "Pâte brisée", 1, ""),
                new RecipeIngredient(0, 8, "Sucre", 50, "g"),
                new RecipeIngredient(0, 8, "Beurre", 50, "g"),
                new RecipeIngredient(0, 8, "Cannelle", 1, "pincée")
        );
        insertRecipeWithIngredients(db, tartePommes, tarteAuxPommesIngredients);

        Recipe mousseChocolat = new Recipe(
                9,
                "Mousse au Chocolat",
                "Dessert léger et aérien au chocolat",
                "1. Fondre le chocolat\n2. Monter les blancs\n3. Mélanger délicatement",
                "@drawable/crepes",
                20,
                "Moyen",
                4,
                "Admin",
                0
        );
        mousseChocolat.setDietType("vegetarian");
        List<String> allergenesMousse = new ArrayList<>();
        allergenesMousse.add("oeufs");
        allergenesMousse.add("lactose");
        mousseChocolat.setAllergens(allergenesMousse);
        List<RecipeIngredient> mousseAuChocolatIngredients = Arrays.asList(
                new RecipeIngredient(0, 9, "Chocolat noir", 200, "g"),
                new RecipeIngredient(0, 9, "Œufs", 4, ""),
                new RecipeIngredient(0, 9, "Sucre", 50, "g")
        );
        insertRecipeWithIngredients(db, mousseChocolat, mousseAuChocolatIngredients);

        Recipe gateauYaourt = new Recipe(
                10,
                "Gâteau au Yaourt",
                "Le classique gâteau au yaourt",
                "1. Mélanger yaourt et sucre\n2. Ajouter farine et œufs\n3. Cuire 35min",
                "@drawable/crepes",
                45,
                "Facile",
                8,
                "Admin",
                0
        );
        gateauYaourt.setDietType("vegetarian");
        List<String> allergenesGateau = new ArrayList<>();
        allergenesGateau.add("lactose");
        allergenesGateau.add("gluten");
        allergenesGateau.add("oeufs");
        gateauYaourt.setAllergens(allergenesGateau);

        List<RecipeIngredient> gateauYaourtIngredients = Arrays.asList(
                new RecipeIngredient(0, 10, "Yaourt nature", 1, "pot"),
                new RecipeIngredient(0, 10, "Sucre", 2, "pots"),
                new RecipeIngredient(0, 10, "Farine", 3, "pots"),
                new RecipeIngredient(0, 10, "Œufs", 3, ""),
                new RecipeIngredient(0, 10, "Huile végétale", 0.5, "pot"),
                new RecipeIngredient(0, 10, "Levure chimique", 1, "sachet")
        );
        insertRecipeWithIngredients(db, gateauYaourt, gateauYaourtIngredients);

        Recipe coqAuVin = new Recipe(
                11,
                "Coq au Vin",
                "Un plat traditionnel à base de vin rouge",
                "1. Faire revenir le coq\n2. Ajouter les légumes et le vin\n3. Mijoter pendant 2h",
                "@drawable/crepes",
                120,
                "Moyen",
                4,
                "Admin",
                0
        );
        coqAuVin.setDietType("none");
        List<String> allergenesCoq = new ArrayList<>();
        allergenesCoq.add("sulfites");
        allergenesCoq.add("céleri");
        coqAuVin.setAllergens(allergenesCoq);
        List<RecipeIngredient> coqAuVinIngredients = Arrays.asList(
                new RecipeIngredient(0, 11, "Poulet", 1.5, "kg"),
                new RecipeIngredient(0, 11, "Vin rouge", 750, "ml"),
                new RecipeIngredient(0, 11, "Lardons", 200, "g"),
                new RecipeIngredient(0, 11, "Champignons", 200, "g"),
                new RecipeIngredient(0, 11, "Carottes", 3, ""),
                new RecipeIngredient(0, 11, "Oignons", 2, ""),
                new RecipeIngredient(0, 11, "Bouquet garni", 1, ""),
                new RecipeIngredient(0, 11, "Farine", 2, "c. à soupe"),
                new RecipeIngredient(0, 11, "Huile", 2, "c. à soupe"),
                new RecipeIngredient(0, 11, "Sel", 1, "pincée"),
                new RecipeIngredient(0, 11, "Poivre", 1, "pincée")
        );
        insertRecipeWithIngredients(db, coqAuVin, coqAuVinIngredients);
        Recipe soupeOignon = new Recipe(
                12,
                "Soupe à l'Oignon",
                "Une soupe réconfortante et savoureuse",
                "1. Faire caraméliser les oignons\n2. Ajouter le bouillon et les herbes\n3. Servir avec des croûtons",
                "@drawable/crepes",
                45,
                "Facile",
                6,
                "Admin",
                0
        );
        soupeOignon.setDietType("vegetarian");
        List<String> allergenesSoupe = new ArrayList<>();
        allergenesSoupe.add("gluten");
        allergenesSoupe.add("lactose");
        soupeOignon.setAllergens(allergenesSoupe);
        List<RecipeIngredient> soupeOignonIngredients = Arrays.asList(
                new RecipeIngredient(0, 12, "Oignons", 1, "kg"),
                new RecipeIngredient(0, 12, "Beurre", 50, "g"),
                new RecipeIngredient(0, 12, "Farine", 2, "c. à soupe"),
                new RecipeIngredient(0, 12, "Bouillon de bœuf", 1, "l"),
                new RecipeIngredient(0, 12, "Pain", 6, "tranches"),
                new RecipeIngredient(0, 12, "Fromage râpé", 100, "g"),
                new RecipeIngredient(0, 12, "Sel", 1, "pincée"),
                new RecipeIngredient(0, 12, "Poivre", 1, "pincée")
        );
        insertRecipeWithIngredients(db, soupeOignon, soupeOignonIngredients);

        Recipe tarteTatin = new Recipe(
                13,
                "Tarte Tatin",
                "Un délicieux dessert caramélisé",
                "1. Caraméliser les pommes\n2. Disposer la pâte\n3. Cuire 30 minutes",
                "@drawable/crepes",
                60,
                "Moyen",
                8,
                "Admin",
                0
        );
        tarteTatin.setDietType("vegetarian");
        List<String> allergenesTatin = new ArrayList<>();
        allergenesTatin.add("gluten");
        allergenesTatin.add("lactose");
        tarteTatin.setAllergens(allergenesTatin);
        List<RecipeIngredient> tarteTatinIngredients = Arrays.asList(
                new RecipeIngredient(0, 13, "Pommes", 5, ""),
                new RecipeIngredient(0, 13, "Sucre", 100, "g"),
                new RecipeIngredient(0, 13, "Beurre", 80, "g"),
                new RecipeIngredient(0, 13, "Pâte feuilletée", 1, "rouleau"),
                new RecipeIngredient(0, 13, "Cannelle", 1, "pincée")
        );
        insertRecipeWithIngredients(db, tarteTatin, tarteTatinIngredients);

        Recipe blanquetteVeau = new Recipe(
                14,
                "Blanquette de Veau",
                "Un plat réconfortant et savoureux",
                "1. Faire revenir le veau\n2. Préparer la sauce blanche\n3. Mijoter pendant 1h30",
                "@drawable/crepes",
                90,
                "Difficile",
                6,
                "Admin",
                0
        );
        blanquetteVeau.setDietType("none");
        List<String> allergenesBlanquette = new ArrayList<>();
        allergenesBlanquette.add("lactose");
        allergenesBlanquette.add("céleri");
        blanquetteVeau.setAllergens(allergenesBlanquette);
        List<RecipeIngredient> blanquetteVeauIngredients = Arrays.asList(
                new RecipeIngredient(0, 14, "Veau", 1, "kg"),
                new RecipeIngredient(0, 14, "Carottes", 4, ""),
                new RecipeIngredient(0, 14, "Oignons", 2, ""),
                new RecipeIngredient(0, 14, "Bouquet garni", 1, ""),
                new RecipeIngredient(0, 14, "Beurre", 50, "g"),
                new RecipeIngredient(0, 14, "Farine", 2, "c. à soupe"),
                new RecipeIngredient(0, 14, "Crème fraîche", 200, "ml"),
                new RecipeIngredient(0, 14, "Sel", 1, "pincée"),
                new RecipeIngredient(0, 14, "Poivre", 1, "pincée")
        );
        insertRecipeWithIngredients(db, blanquetteVeau, blanquetteVeauIngredients);

        Recipe rotiPorc = new Recipe(
                15,
                "Rôti de Porc aux Herbes",
                "Un plat familial et savoureux",
                "1. Assaisonner le rôti\n2. Faire saisir la viande\n3. Cuire au four 1h",
                "@drawable/crepes",
                75,
                "Moyen",
                6,
                "Admin",
                0
        );
        rotiPorc.setDietType("none");
        List<String> allergenesRoti = new ArrayList<>();
        allergenesRoti.add("céleri");
        rotiPorc.setAllergens(allergenesRoti);
        List<RecipeIngredient> rotiPorcHerbesIngredients = Arrays.asList(
                new RecipeIngredient(0, 15, "Rôti de porc", 1, "kg"),
                new RecipeIngredient(0, 15, "Ail", 2, "gousses"),
                new RecipeIngredient(0, 15, "Herbes de Provence", 1, "c. à soupe"),
                new RecipeIngredient(0, 15, "Huile d'olive", 2, "c. à soupe"),
                new RecipeIngredient(0, 15, "Sel", 1, "pincée"),
                new RecipeIngredient(0, 15, "Poivre", 1, "pincée")
        );
        insertRecipeWithIngredients(db, rotiPorc, rotiPorcHerbesIngredients);

        Recipe bouillabaisse = new Recipe(
                16,
                "Bouillabaisse",
                "Plat de poissons et fruits de mer typique de Marseille",
                "1. Préparer le bouillon de poisson\n2. Ajouter les fruits de mer et les poissons\n3. Servir avec des croûtons",
                "@drawable/crepes",
                90,
                "Difficile",
                6,
                "Admin",
                0
        );
        bouillabaisse.setDietType("none");
        List<String> allergenesBouillabaisse = new ArrayList<>();
        allergenesBouillabaisse.add("poisson");
        allergenesBouillabaisse.add("crustacés");
        allergenesBouillabaisse.add("gluten");
        allergenesBouillabaisse.add("céleri");
        bouillabaisse.setAllergens(allergenesBouillabaisse);
        List<RecipeIngredient> bouillabaisseIngredients = Arrays.asList(
                new RecipeIngredient(0, 16, "Poissons variés", 1, "kg"),
                new RecipeIngredient(0, 16, "Fruits de mer", 500, "g"),
                new RecipeIngredient(0, 16, "Tomates", 4, ""),
                new RecipeIngredient(0, 16, "Oignons", 2, ""),
                new RecipeIngredient(0, 16, "Bouillon de poisson", 1.5, "l"),
                new RecipeIngredient(0, 16, "Safran", 1, "pincée"),
                new RecipeIngredient(0, 16, "Ail", 2, "gousses"),
                new RecipeIngredient(0, 16, "Pain", 6, "tranches"),
                new RecipeIngredient(0, 16, "Huile d'olive", 4, "c. à soupe"),
                new RecipeIngredient(0, 16, "Sel", 1, "pincée"),
                new RecipeIngredient(0, 16, "Poivre", 1, "pincée")
        );
        insertRecipeWithIngredients(db, bouillabaisse, bouillabaisseIngredients);

        Recipe boeufCarottes = new Recipe(
                17,
                "Bœuf Bourguignon aux Carottes",
                "Une version plus riche du bœuf bourguignon",
                "1. Faire revenir le bœuf\n2. Ajouter les carottes et l'oignon\n3. Cuire pendant 2h30",
                "@drawable/crepes",
                150,
                "Difficile",
                6,
                "Admin",
                0
        );
        boeufCarottes.setDietType("none");
        List<String> allergenesBoeufCarottes = new ArrayList<>();
        allergenesBoeufCarottes.add("sulfites");
        boeufCarottes.setAllergens(allergenesBoeufCarottes);
        List<RecipeIngredient> boeufBourguignonCarottesIngredients = Arrays.asList(
                new RecipeIngredient(0, 17, "Bœuf", 1, "kg"),
                new RecipeIngredient(0, 17, "Carottes", 6, ""),
                new RecipeIngredient(0, 17, "Oignons", 2, ""),
                new RecipeIngredient(0, 17, "Vin rouge", 750, "ml"),
                new RecipeIngredient(0, 17, "Bouquet garni", 1, ""),
                new RecipeIngredient(0, 17, "Farine", 2, "c. à soupe"),
                new RecipeIngredient(0, 17, "Beurre", 50, "g"),
                new RecipeIngredient(0, 17, "Sel", 1, "pincée"),
                new RecipeIngredient(0, 17, "Poivre", 1, "pincée")
        );
        insertRecipeWithIngredients(db, boeufCarottes, boeufBourguignonCarottesIngredients);

        Recipe tartiflette = new Recipe(
                18,
                "Tartiflette",
                "Plat savoureux à base de pommes de terre, reblochon et lardons",
                "1. Faire cuire les pommes de terre\n2. Préparer le mélange avec le reblochon et les lardons\n3. Cuire 30 minutes",
                "@drawable/crepes",
                60,
                "Moyen",
                4,
                "Admin",
                0
        );
        tartiflette.setDietType("none");
        List<String> allergenesTartiflette = new ArrayList<>();
        allergenesTartiflette.add("lactose");
        tartiflette.setAllergens(allergenesTartiflette);
        List<RecipeIngredient> tartifletteIngredients = Arrays.asList(
                new RecipeIngredient(0, 18, "Pommes de terre", 1, "kg"),
                new RecipeIngredient(0, 18, "Reblochon", 500, "g"),
                new RecipeIngredient(0, 18, "Lardons", 200, "g"),
                new RecipeIngredient(0, 18, "Oignons", 2, ""),
                new RecipeIngredient(0, 18, "Crème fraîche", 200, "ml"),
                new RecipeIngredient(0, 18, "Sel", 1, "pincée"),
                new RecipeIngredient(0, 18, "Poivre", 1, "pincée")
        );
        insertRecipeWithIngredients(db, tartiflette, tartifletteIngredients);

        Recipe tarteTomates = new Recipe(
                19,
                "Tarte Fine aux Tomates et Basilic",
                "Une tarte légère et parfumée",
                "1. Préparer la pâte brisée\n2. Disposer les tomates et le basilic\n3. Cuire 25 minutes",
                "@drawable/crepes",
                40,
                "Facile",
                6,
                "Admin",
                0
        );
        tarteTomates.setDietType("vegetarian");
        List<String> allergenesTarteTomates = new ArrayList<>();
        allergenesTarteTomates.add("gluten");
        tarteTomates.setAllergens(allergenesTarteTomates);
        List<RecipeIngredient> tarteFineTomatesIngredients = Arrays.asList(
                new RecipeIngredient(0, 19, "Pâte brisée", 1, "rouleau"),
                new RecipeIngredient(0, 19, "Tomates", 4, ""),
                new RecipeIngredient(0, 19, "Mozzarella", 200, "g"),
                new RecipeIngredient(0, 19, "Basilic frais", 1, "poignée"),
                new RecipeIngredient(0, 19, "Huile d'olive", 2, "c. à soupe"),
                new RecipeIngredient(0, 19, "Sel", 1, "pincée"),
                new RecipeIngredient(0, 19, "Poivre", 1, "pincée")
        );
        insertRecipeWithIngredients(db, tarteTomates, tarteFineTomatesIngredients);

        Recipe couscousRoyal = new Recipe(
                20,
                "Couscous Royal",
                "Le couscous traditionnel avec viandes et légumes",
                "1. Préparer le bouillon de viande\n2. Cuire le couscous et les légumes\n3. Servir avec les viandes",
                "@drawable/crepes",
                120,
                "Difficile",
                8,
                "Admin",
                0
        );
        couscousRoyal.setDietType("none");
        List<String> allergenesCouscous = new ArrayList<>();
        allergenesCouscous.add("gluten");
        allergenesCouscous.add("céleri");
        couscousRoyal.setAllergens(allergenesCouscous);
        List<RecipeIngredient> couscousRoyalIngredients = Arrays.asList(
                new RecipeIngredient(0, 20, "Semoule de blé", 500, "g"),
                new RecipeIngredient(0, 20, "Poulet", 500, "g"),
                new RecipeIngredient(0, 20, "Merguez", 300, "g"),
                new RecipeIngredient(0, 20, "Carottes", 4, ""),
                new RecipeIngredient(0, 20, "Navets", 2, ""),
                new RecipeIngredient(0, 20, "Courgettes", 2, ""),
                new RecipeIngredient(0, 20, "Pois chiches", 200, "g"),
                new RecipeIngredient(0, 20, "Bouillon de légumes", 1.5, "l"),
                new RecipeIngredient(0, 20, "Épices à couscous", 2, "c. à soupe"),
                new RecipeIngredient(0, 20, "Huile d'olive", 2, "c. à soupe"),
                new RecipeIngredient(0, 20, "Sel", 1, "pincée"),
                new RecipeIngredient(0, 20, "Poivre", 1, "pincée")
        );
        insertRecipeWithIngredients(db, couscousRoyal, couscousRoyalIngredients);

        Recipe pouletTikkaMasala = new Recipe(
                21,
                "Poulet Tikka Masala",
                "Un plat indien épicé et crémeux",
                "1. Préparer la marinade pour le poulet\n2. Faire griller le poulet\n3. Cuire dans une sauce épicée au lait de coco",
                "@drawable/crepes",
                60,
                "Moyen",
                4,
                "Admin",
                0
        );
        pouletTikkaMasala.setDietType("none");
        List<String> allergenesPouletTikka = new ArrayList<>();
        allergenesPouletTikka.add("lactose");
        allergenesPouletTikka.add("soja");
        pouletTikkaMasala.setAllergens(allergenesPouletTikka);
        List<RecipeIngredient> pouletTikkaMasalaIngredients = Arrays.asList(
                new RecipeIngredient(0, 21, "Poulet", 500, "g"),
                new RecipeIngredient(0, 21, "Yaourt nature", 200, "g"),
                new RecipeIngredient(0, 21, "Ail", 2, "gousses"),
                new RecipeIngredient(0, 21, "Gingembre frais", 1, "c. à soupe"),
                new RecipeIngredient(0, 21, "Épices Tikka Masala", 2, "c. à soupe"),
                new RecipeIngredient(0, 21, "Tomates concassées", 400, "g"),
                new RecipeIngredient(0, 21, "Lait de coco", 200, "ml"),
                new RecipeIngredient(0, 21, "Huile", 2, "c. à soupe"),
                new RecipeIngredient(0, 21, "Sel", 1, "pincée"),
                new RecipeIngredient(0, 21, "Poivre", 1, "pincée")
        );
        insertRecipeWithIngredients(db, pouletTikkaMasala, pouletTikkaMasalaIngredients);

        Recipe padThai = new Recipe(
                22,
                "Pad Thaï",
                "Un plat thaïlandais aux nouilles et crevettes",
                "1. Cuire les nouilles de riz\n2. Préparer la sauce Pad Thaï\n3. Mélanger avec les crevettes et les légumes sautés",
                "@drawable/crepes",
                40,
                "Moyen",
                4,
                "Admin",
                0
        );
        padThai.setDietType("none");
        List<String> allergenesPadThai = new ArrayList<>();
        allergenesPadThai.add("crustacés");
        allergenesPadThai.add("soja");
        padThai.setAllergens(allergenesPadThai);
        List<RecipeIngredient> padThaiIngredients = Arrays.asList(
                new RecipeIngredient(0, 22, "Nouilles de riz", 250, "g"),
                new RecipeIngredient(0, 22, "Crevettes", 200, "g"),
                new RecipeIngredient(0, 22, "Œufs", 2, ""),
                new RecipeIngredient(0, 22, "Cacahuètes concassées", 50, "g"),
                new RecipeIngredient(0, 22, "Sauce soja", 3, "c. à soupe"),
                new RecipeIngredient(0, 22, "Tamarin", 2, "c. à soupe"),
                new RecipeIngredient(0, 22, "Oignons verts", 2, ""),
                new RecipeIngredient(0, 22, "Huile", 2, "c. à soupe"),
                new RecipeIngredient(0, 22, "Sel", 1, "pincée"),
                new RecipeIngredient(0, 22, "Poivre", 1, "pincée")
        );
        insertRecipeWithIngredients(db, padThai, padThaiIngredients);

        Recipe clafoutisCerises = new Recipe(
                23,
                "Clafoutis aux Cerises",
                "Un dessert français aux cerises et à la pâte sucrée",
                "1. Disposer les cerises dans un plat\n2. Verser la pâte à clafoutis\n3. Cuire au four 30 minutes",
                "@drawable/crepes",
                45,
                "Facile",
                6,
                "Admin",
                0
        );
        clafoutisCerises.setDietType("vegetarian");
        List<String> allergenesClafoutis = new ArrayList<>();
        allergenesClafoutis.add("gluten");
        allergenesClafoutis.add("oeufs");
        allergenesClafoutis.add("lactose");
        clafoutisCerises.setAllergens(allergenesClafoutis);
        List<RecipeIngredient> clafoutisCerisesIngredients = Arrays.asList(
                new RecipeIngredient(0, 23, "Cerises", 500, "g"),
                new RecipeIngredient(0, 23, "Farine", 100, "g"),
                new RecipeIngredient(0, 23, "Sucre", 100, "g"),
                new RecipeIngredient(0, 23, "Lait", 300, "ml"),
                new RecipeIngredient(0, 23, "Œufs", 3, ""),
                new RecipeIngredient(0, 23, "Beurre fondu", 20, "g"),
                new RecipeIngredient(0, 23, "Extrait de vanille", 1, "c. à café")
        );
        insertRecipeWithIngredients(db, clafoutisCerises, clafoutisCerisesIngredients);

        Recipe curryLegumes = new Recipe(
                24,
                "Curry de Légumes",
                "Un plat végétarien épicé et savoureux",
                "1. Faire revenir les légumes\n2. Ajouter les épices et le lait de coco\n3. Laisser mijoter 20 minutes",
                "@drawable/crepes",
                35,
                "Facile",
                4,
                "Admin",
                0
        );
        curryLegumes.setDietType("vegetarian");
        List<String> allergenesCurryLegumes = new ArrayList<>();
        allergenesCurryLegumes.add("soja");
        curryLegumes.setAllergens(allergenesCurryLegumes);
        List<RecipeIngredient> curryLegumesIngredients = Arrays.asList(
                new RecipeIngredient(0, 24, "Légumes mélangés", 500, "g"),
                new RecipeIngredient(0, 24, "Lait de coco", 200, "ml"),
                new RecipeIngredient(0, 24, "Pâte de curry", 2, "c. à soupe"),
                new RecipeIngredient(0, 24, "Ail", 2, "gousses"),
                new RecipeIngredient(0, 24, "Gingembre", 1, "c. à café"),
                new RecipeIngredient(0, 24, "Huile", 2, "c. à soupe"),
                new RecipeIngredient(0, 24, "Sel", 1, "pincée"),
                new RecipeIngredient(0, 24, "Poivre", 1, "pincée")
        );
        insertRecipeWithIngredients(db, curryLegumes, curryLegumesIngredients);

        Recipe cheesecakeCitron = new Recipe(
                25,
                "Cheesecake au Citron",
                "Un dessert frais et crémeux",
                "1. Préparer la base biscuitée\n2. Mélanger la crème au citron\n3. Réfrigérer pendant 4 heures",
                "@drawable/crepes",
                240,
                "Moyen",
                8,
                "Admin",
                0
        );
        cheesecakeCitron.setDietType("vegetarian");
        List<String> allergenesCheesecake = new ArrayList<>();
        allergenesCheesecake.add("gluten");
        allergenesCheesecake.add("lactose");
        allergenesCheesecake.add("oeufs");
        cheesecakeCitron.setAllergens(allergenesCheesecake);
        List<RecipeIngredient> cheesecakeCitronIngredients = Arrays.asList(
                new RecipeIngredient(0, 25, "Biscuit émietté", 200, "g"),
                new RecipeIngredient(0, 25, "Beurre fondu", 100, "g"),
                new RecipeIngredient(0, 25, "Fromage frais", 400, "g"),
                new RecipeIngredient(0, 25, "Crème fraîche", 200, "ml"),
                new RecipeIngredient(0, 25, "Sucre", 100, "g"),
                new RecipeIngredient(0, 25, "Jus de citron", 2, "c. à soupe"),
                new RecipeIngredient(0, 25, "Zeste de citron", 1, "c. à soupe"),
                new RecipeIngredient(0, 25, "Œufs", 3, "")
        );
        insertRecipeWithIngredients(db, cheesecakeCitron, cheesecakeCitronIngredients);

        Recipe curryLegumesVegetarien = new Recipe(
                26,
                "Curry de Légumes Végétarien",
                "Un délicieux curry végétarien riche en saveurs et en légumes.",
                "1. Couper tous les légumes en morceaux\n2. Faire revenir les oignons et l'ail\n3. Ajouter les épices curry\n4. Ajouter les légumes et le lait de coco\n5. Laisser mijoter 20 minutes",
                "@drawable/crepes",
                30,
                "Facile",
                4,
                "Admin",
                0
        );
        curryLegumesVegetarien.setDietType("vegetarian");
        List<String> allergenesCurryVegetarien = new ArrayList<>();
        allergenesCurryVegetarien.add("soja");
        curryLegumesVegetarien.setAllergens(allergenesCurryVegetarien);
        List<RecipeIngredient> curryLegumesIngredients1 = Arrays.asList(
                new RecipeIngredient(0, 26, "Carottes", 200, "g"),
                new RecipeIngredient(0, 26, "Courgettes", 200, "g"),
                new RecipeIngredient(0, 26, "Oignons", 2, ""),
                new RecipeIngredient(0, 26, "Ail", 2, "gousses"),
                new RecipeIngredient(0, 26, "Lait de coco", 200, "ml"),
                new RecipeIngredient(0, 26, "Curry en poudre", 1, "c. à soupe"),
                new RecipeIngredient(0, 26, "Huile végétale", 2, "c. à soupe"),
                new RecipeIngredient(0, 26, "Sel", 1, "pincée"),
                new RecipeIngredient(0, 26, "Poivre", 1, "pincée")
        );
        insertRecipeWithIngredients(db, curryLegumesVegetarien, curryLegumesIngredients1);

        Recipe bowlBuddhaVegan = new Recipe(
                27,
                "Bowl Buddha Vegan",
                "Un bowl équilibré et coloré 100% végétal.",
                "1. Cuire le quinoa\n" +
                        "2. Préparer les légumes\n" +
                        "3. Faire griller le tofu\n" +
                        "4. Préparer la sauce tahini\n" +
                        "5. Assembler le bowl",
                "@drawable/crepes",
                25,
                "Moyen",
                2,
                "Admin",
                0
        );
        bowlBuddhaVegan.setDietType("vegan");
        List<String> allergenesBowlBuddha = new ArrayList<>();
        allergenesBowlBuddha.add("soja");
        allergenesBowlBuddha.add("sésame");
        bowlBuddhaVegan.setAllergens(allergenesBowlBuddha);
        List<RecipeIngredient> bowlBuddhaIngredients = Arrays.asList(
                new RecipeIngredient(0, 27, "Quinoa", 200, "g"),
                new RecipeIngredient(0, 27, "Tofu", 150, "g"),
                new RecipeIngredient(0, 27, "Carottes", 100, "g"),
                new RecipeIngredient(0, 27, "Avocat", 1, ""),
                new RecipeIngredient(0, 27, "Chou rouge", 100, "g"),
                new RecipeIngredient(0, 27, "Tahini", 2, "c. à soupe"),
                new RecipeIngredient(0, 27, "Jus de citron", 1, "c. à soupe"),
                new RecipeIngredient(0, 27, "Sel", 1, "pincée"),
                new RecipeIngredient(0, 27, "Poivre", 1, "pincée")
        );
        insertRecipeWithIngredients(db, bowlBuddhaVegan, bowlBuddhaIngredients);

        Recipe curryPoisChiches = new Recipe(
                28,
                "Curry Vegan aux Pois Chiches",
                "Un curry savoureux et épicé 100% végétal.",
                "1. Faire revenir un oignon avec de l'ail\n" +
                        "2. Ajouter les épices (curry, curcuma)\n" +
                        "3. Incorporer des pois chiches cuits, des tomates concassées et du lait de coco\n" +
                        "4. Laisser mijoter 20 minutes\n" +
                        "5. Servir avec du riz basmati",
                "@drawable/crepes",
                35,
                "Facile",
                4,
                "Admin",
                0
        );
        curryPoisChiches.setDietType("vegan");
        List<String> allergenesCurryPoisChiches = new ArrayList<>();
        allergenesCurryPoisChiches.add("soja");
        curryPoisChiches.setAllergens(allergenesCurryPoisChiches);
        List<RecipeIngredient> curryPoisChichesIngredients = Arrays.asList(
                new RecipeIngredient(0, 28, "Pois chiches cuits", 400, "g"),
                new RecipeIngredient(0, 28, "Tomates concassées", 400, "g"),
                new RecipeIngredient(0, 28, "Lait de coco", 200, "ml"),
                new RecipeIngredient(0, 28, "Oignon", 1, ""),
                new RecipeIngredient(0, 28, "Ail", 2, "gousses"),
                new RecipeIngredient(0, 28, "Curry en poudre", 2, "c. à soupe"),
                new RecipeIngredient(0, 28, "Curcuma", 1, "c. à café"),
                new RecipeIngredient(0, 28, "Huile", 2, "c. à soupe"),
                new RecipeIngredient(0, 28, "Sel", 1, "pincée"),
                new RecipeIngredient(0, 28, "Poivre", 1, "pincée")
        );
        insertRecipeWithIngredients(db, curryPoisChiches, curryPoisChichesIngredients);

        Recipe painBananeSansGluten = new Recipe(
                29,
                "Pain aux Bananes",
                "Un pain moelleux et savoureux à base de bananes, parfait pour les personnes sensibles au gluten.",
                "1. Écraser les bananes mûres dans un bol\n" +
                        "2. Ajouter les œufs, l'huile végétale et le sucre\n" +
                        "3. Incorporer la farine sans gluten et la levure chimique\n" +
                        "4. Verser dans un moule et cuire au four pendant 50 minutes à 180°C",
                "@drawable/crepes",
                50,
                "Facile",
                8,
                "Admin",
                0
        );
        painBananeSansGluten.setDietType("gluten_free");
        List<String> painAllergens = new ArrayList<>();
        painAllergens.add("Œufs");
        painBananeSansGluten.setAllergens(painAllergens);
        List<RecipeIngredient> painBananeIngredients = Arrays.asList(
                new RecipeIngredient(0, 29, "Bananes", 3, ""),
                new RecipeIngredient(0, 29, "Œufs", 2, ""),
                new RecipeIngredient(0, 29, "Huile végétale", 100, "ml"),
                new RecipeIngredient(0, 29, "Sucre", 100, "g"),
                new RecipeIngredient(0, 29, "Farine sans gluten", 200, "g"),
                new RecipeIngredient(0, 29, "Levure chimique", 1, "c. à café"),
                new RecipeIngredient(0, 29, "Sel", 1, "pincée")
        );
        insertRecipeWithIngredients(db, painBananeSansGluten, painBananeIngredients);

    }


    private static void insertRecipe(SQLiteDatabase db, Recipe recipe) {
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.RecipeEntry._ID, recipe.getId());
        values.put(DatabaseContract.RecipeEntry.COLUMN_NAME, recipe.getName());
        values.put(DatabaseContract.RecipeEntry.COLUMN_DESCRIPTION, recipe.getDescription());
        values.put(DatabaseContract.RecipeEntry.COLUMN_STEPS, recipe.getSteps());
        values.put(DatabaseContract.RecipeEntry.COLUMN_IMAGE_URL, recipe.getImageUrl());
        values.put(DatabaseContract.RecipeEntry.COLUMN_COOKING_TIME, recipe.getCookingTime());
        values.put(DatabaseContract.RecipeEntry.COLUMN_DIFFICULTY, recipe.getDifficulty());
        values.put(DatabaseContract.RecipeEntry.COLUMN_SERVINGS, recipe.getServings());
        values.put(DatabaseContract.RecipeEntry.COLUMN_AUTHOR, recipe.getAuthor());
        values.put(DatabaseContract.RecipeEntry.COLUMN_USER_ID, recipe.getUserId());
        values.put(DatabaseContract.RecipeEntry.COLUMN_DIET_TYPE, recipe.getDietType());
        values.put(DatabaseContract.RecipeEntry.COLUMN_ALLERGENS, recipe.getAllergensJson());

        db.insert(DatabaseContract.RecipeEntry.TABLE_NAME, null, values);
    }


}