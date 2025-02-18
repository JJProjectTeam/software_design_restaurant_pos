package com.softwaredesign.project.order;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;

import com.softwaredesign.project.inventory.Ingredient;

public class OrderManager {
    private Queue<Order> orders;
    private StationMapper stationMapper;
    private CollectionPoint collectionPoint;

    public OrderManager() {
        orders = new LinkedList<>();
        stationMapper = new StationMapper();
        collectionPoint = new CollectionPoint();
    }

    public String generateOrderId() {
        return UUID.randomUUID().toString();
    }

    public void addOrder(Order order) {
        collectionPoint.registerOrder(order.getOrderId(), order.getRecipes().size());
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

        return new ArrayList<>();
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
