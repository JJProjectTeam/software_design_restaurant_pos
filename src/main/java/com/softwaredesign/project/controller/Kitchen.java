package com.softwaredesign.project.controller;

import java.util.ArrayList;
import java.util.List;
import com.softwaredesign.project.model.Recipe;
import com.softwaredesign.project.model.Meal;

public class Kitchen {
    private OrderManager orderManager;
    private List<Recipe> recipes = new ArrayList<>();

    public Kitchen(OrderManager orderManager) {
        this.orderManager = orderManager;
    }

    // Get the recipes from the order manager, one option. 
    public void getRecipes() {
        this.recipes = orderManager.getRecipes();
    }

    // Add a recipe to the kitchen, another option. 
    public void addRecipe(Recipe recipe) {
        recipes.add(recipe);
    }





    // Create a recipe from the recipes list, this assumes that the recipes list is not empty, maybe we should add a check for that
    public List<Meal> prepareRecipes() {
        // hoving a list for meal so we can return it later
        // I want to check if the recipes list is empty
        if (recipes.isEmpty()) {
            System.out.println("No recipes to prepare");
            return null;
        }

        List<Meal> meals = new ArrayList<>();
        for (Recipe recipe : recipes) {
            Meal meal = recipe.buildMeal();
            meals.add(meal);
        }

        // return the list of meals
        return meals;
    }
}
