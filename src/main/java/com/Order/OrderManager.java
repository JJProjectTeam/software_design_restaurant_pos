package com.Order;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import com.softwaredesign.project.model.Ingredient;

public class OrderManager {
    Queue<Order> orders;
    StationMapper stationMapper;

    public OrderManager() {
        orders = new LinkedList<>();
        stationMapper = new StationMapper();
    }

    public void addOrder(Order order) {
        orders.add(order);
    }

    public List<Recipe> processOrder() {
        while (!orders.isEmpty()) {
            Order order = orders.poll();
            List<Recipe> recipes = order.getRecipes();

            for (Recipe recipe : recipes) {
                makeAmendments(recipe, order);
                stationMapper.mapStationsToRecipe(recipe);
            }

            return recipes;
        }

        return null;
    }

    private void makeAmendments(Recipe recipe, Order order) {
        RecipeModification modifications = order.getModificationsForRecipe(recipe);

        // Remove ingredients
        for (Ingredient ingredient : modifications.getRemovedIngredients()) {
            recipe.removeIngredient(ingredient);
        }

        // Add ingredients
        for (Ingredient ingredient : modifications.getAddedIngredients()) {
            recipe.addIngredient(ingredient);
        }
    }
}

