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
        // First try to get modifications directly by reference
        RecipeModification directResult = modifications.get(recipe);
        if (directResult != null) {
            return directResult;
        }
        
        // If no direct match, try to find by name, which helps with cloned recipes
        for (Map.Entry<Recipe, RecipeModification> entry : modifications.entrySet()) {
            Recipe existingRecipe = entry.getKey();
            
            // If we find a recipe with the same name, use its modifications
            if (existingRecipe.getName().equals(recipe.getName())) {
                System.out.println("[DEBUG-ORDER] Found modifications for cloned recipe " + 
                                  recipe.getName() + " using name-based lookup");
                return entry.getValue();
            }
        }
        
        // No modifications found for this recipe
        return null;
    }

    public String getOrderId() {
        return orderId;
    }

    public boolean isComplete() {
        return recipes.stream().allMatch(Recipe::isComplete);
    }

    public Map<String, Integer> getIngredients() {
        Map<String, Integer> ingredients = new HashMap<>();
        List<Ingredient> allIngredients = new ArrayList<>();

        for (Recipe recipe : recipes) {
            allIngredients.addAll(recipe.getIngredients());
            for (Ingredient ingredient : recipe.getIngredients()) {
                ingredients.merge(ingredient.getName(), 1, Integer::sum);
            }
        }

        return ingredients;
    }
}
