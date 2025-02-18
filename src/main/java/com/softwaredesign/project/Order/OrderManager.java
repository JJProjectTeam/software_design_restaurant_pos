package com.softwaredesign.project.order;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;

import com.softwaredesign.project.inventory.Ingredient;
import com.softwaredesign.project.kitchen.StationManager;

public class OrderManager {
    private Queue<Order> orders;
    private StationMapper stationMapper;
    private CollectionPoint collectionPoint;

    public OrderManager(CollectionPoint collectionPoint, StationManager stationManager) {
        orders = new LinkedList<>();
        stationMapper = new StationMapper(stationManager);
        this.collectionPoint = collectionPoint;
    }

    public String generateOrderId() {
        return UUID.randomUUID().toString();
    }

    public void addOrder(Order order) {
        collectionPoint.registerOrder(order.getOrderId(), order.getRecipes().size());
        orders.add(order);
    }

    public List<Recipe> processOrder() {
        if (orders.isEmpty()) {
            return new ArrayList<>();
        }

        // Peek instead of poll - don't remove the order yet
        Order order = orders.peek();
        List<Recipe> recipes = order.getRecipes();

        for (Recipe recipe : recipes) {
            makeAmendments(recipe, order);
            stationMapper.mapStationsToRecipe(recipe);
        }

        // Now we can remove the order
        orders.poll();
        return recipes;
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
