package com.Order;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import com.softwaredesign.project.model.Ingredient;

public class Order {
    private List<Recipe> recipes;
    private Map<Recipe, RecipeModification> modifications;

    public Order() {
        this.recipes = new ArrayList<>();
        this.modifications = new HashMap<>();
    }

    public void addRecipe(Recipe recipe) {
        recipes.add(recipe);
        modifications.put(recipe, new RecipeModification());
    }

    public void addModification(Recipe recipe, Ingredient ingredient, boolean isAddition) {
        RecipeModification mod = modifications.get(recipe);
        if (isAddition) {
            mod.addIngredient(ingredient);
        } else {
            mod.removeIngredient(ingredient);
        }
    }

    public List<Recipe> getRecipes() {
        return recipes;
    }

    public RecipeModification getModificationsForRecipe(Recipe recipe) {
        return modifications.get(recipe);
    }
}
