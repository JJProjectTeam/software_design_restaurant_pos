package com.softwaredesign.project.order;

import java.util.List;
import java.util.Map;

import com.softwaredesign.project.inventory.Ingredient;

import java.util.ArrayList;
import java.util.HashMap;

public class Order {
    private String orderId;
    private List<Recipe> recipes;
    private Map<Recipe, RecipeModification> modifications;

    public Order(String orderId) {
        this.orderId = orderId;
        this.recipes = new ArrayList<>();
        this.modifications = new HashMap<>();
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public void addRecipes(Recipe... recipes) {
        for (Recipe recipe : recipes) {
            recipe.setOrderId(this.orderId);
            this.recipes.add(recipe);
            modifications.put(recipe, new RecipeModification());
        }
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

    public String getOrderId() {
        return orderId;
    }

    public boolean isComplete() {
        return recipes.stream().allMatch(Recipe::isComplete);
    }
}
