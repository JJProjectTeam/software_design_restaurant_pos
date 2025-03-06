package com.softwaredesign.project.order;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

import com.softwaredesign.project.inventory.Ingredient;
import com.softwaredesign.project.kitchen.StationManager;
import com.softwaredesign.project.orderfulfillment.CollectionPoint;

public class OrderManager {
    private Queue<Order> orders;
    private StationMapper stationMapper;
    private CollectionPoint collectionPoint;
    private static final AtomicInteger orderCounter = new AtomicInteger(1000);

    public OrderManager(CollectionPoint collectionPoint, StationManager stationManager) {
        orders = new LinkedList<>();
        stationMapper = new StationMapper(stationManager);
        this.collectionPoint = collectionPoint;
    }

    /**
     * Generates a sequential order ID starting from 1000
     * @return A string representation of the order ID (e.g., "Order-1001")
     */
    public String generateOrderId() {
        return "Order-" + orderCounter.getAndIncrement();
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

        // Ensure the order is registered with the collection point
        // This is a safety check in case the order was not properly registered before
        String orderId = order.getOrderId();
        // Only re-register if it's not already registered
        if (collectionPoint.getTotalMealsExpected(orderId) == 0) {
            collectionPoint.registerOrder(orderId, recipes.size());
            System.out.println("[DEBUG] Re-registered order " + orderId + " with collection point");
        }

        for (Recipe recipe : recipes) {
            // Set the orderId on the recipe to ensure proper tracking
            recipe.setOrderId(orderId);
            makeAmendments(recipe, order);
            stationMapper.mapStationsToRecipe(recipe);
        }

        // Instead of removing the order, we'll keep it in the queue
        // so stations can use it for their backlogs
        // orders.poll();
        
        // Clone the recipes list to avoid modifying the original order's recipes
        return new ArrayList<>(recipes);
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
    
    /**
     * Get a copy of the pending orders queue for display purposes
     * @return a copy of the pending orders queue
     */
    public Queue<Order> getPendingOrders() {
        return new LinkedList<>(orders);
    }
}
