package com.softwaredesign.project.model.order;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

import com.softwaredesign.project.model.inventory.Ingredient;
import com.softwaredesign.project.model.inventory.InventoryStockTracker;
import com.softwaredesign.project.model.kitchen.StationManager;
import com.softwaredesign.project.model.orderfulfillment.CollectionPoint;
import com.softwaredesign.project.model.singletons.StatisticsSingleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrderManager {
    private static final Logger logger = LoggerFactory.getLogger(OrderManager.class);
    private Queue<Order> orders;
    private StationMapper stationMapper;
    private CollectionPoint collectionPoint;
    private InventoryStockTracker inventoryStockTracker;
    private static final AtomicInteger orderCounter = new AtomicInteger(1000);

    public OrderManager(CollectionPoint collectionPoint, StationManager stationManager, InventoryStockTracker inventoryStockTracker) {
        orders = new LinkedList<>();
        stationMapper = new StationMapper(stationManager);
        this.collectionPoint = collectionPoint;
        this.inventoryStockTracker = inventoryStockTracker;
    }
    
    // Constructor for backward compatibility
    public OrderManager(CollectionPoint collectionPoint, StationManager stationManager) {
        this(collectionPoint, stationManager, null);
    }

    /**
     * Generates a sequential order ID starting from 1000
     * 
     * @return A string representation of the order ID (e.g., "Order-1001")
     */
    public String generateOrderId() {
        return "Order-" + orderCounter.getAndIncrement();
    }

    public void addOrder(Order order) {
        collectionPoint.registerOrder(order.getOrderId(), order.getRecipes().size());
        orders.add(order);

        // Track statistics
        StatisticsSingleton.getInstance().incrementStat("ordersReceived");
        StatisticsSingleton.getInstance().incrementStat("totalRecipesOrdered", order.getRecipes().size());
    }

    /**
     * Processes the next order in the queue
     * 
     * @return A list of recipes from the order
     */
    public List<Recipe> processOrder() {
        if (orders.isEmpty()) {
            return new ArrayList<>();
        }

        // Get the next order and remove it from the queue
        Order order = orders.poll();
        List<Recipe> recipes = order.getRecipes();
        String orderId = order.getOrderId();

        // Track statistics
        StatisticsSingleton.getInstance().incrementStat("ordersProcessed");
        
        // Release the reserved ingredients since we're now going to actually use them
        if (inventoryStockTracker != null) {
            inventoryStockTracker.releaseReservedIngredients(orderId);
            logger.info("[DEBUG] Released ingredient reservations for order " + orderId);
        }

        // Ensure the order is registered with the collection point
        // This is a safety check in case the order was not properly registered before
        // Only re-register if it's not already registered
        if (collectionPoint.getTotalMealsExpected(orderId) == 0) {
            collectionPoint.registerOrder(orderId, recipes.size());
            logger.info("[DEBUG] Re-registered order " + orderId + " with collection point");
        }

        // Create a list to hold cloned recipes
        List<Recipe> clonedRecipes = new ArrayList<>();
        
        for (Recipe originalRecipe : recipes) {
            // Create a fresh copy of the recipe
            Recipe clonedRecipe = originalRecipe.copy();
            
            // Set the orderId on the cloned recipe
            clonedRecipe.setOrderId(orderId);
            
            // Apply any modifications to the cloned recipe
            makeAmendments(clonedRecipe, order);
            
            // Add the cloned recipe to our result list
            clonedRecipes.add(clonedRecipe);
            
            // Add debug logging to track recipe cloning
            System.out.println("[DEBUG-CLONE] Created copy of recipe " + clonedRecipe.getName() + 
                               " for order " + orderId);
        }
        
        // Return the list of cloned recipes
        return clonedRecipes;
    }

    private void makeAmendments(Recipe recipe, Order order) {
        RecipeModification modifications = order.getModificationsForRecipe(recipe);
        
        // Add null check to prevent NullPointerException
        if (modifications == null) {
            // Log warning and return early if no modifications are found
            System.out.println("[DEBUG-AMENDMENTS] No modifications found for recipe " + 
                              recipe.getName() + " in order " + order.getOrderId());
            return;
        }

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
     * 
     * @return a copy of the pending orders queue
     */
    public Queue<Order> getPendingOrders() {
        return new LinkedList<>(orders);
    }

    public List<Order> getOrders() {
        return new ArrayList<>(orders);
    }
    
    /**
     * Sets the inventory stock tracker for this order manager.
     * This is used to release reservations when orders are processed.
     * @param inventoryStockTracker The inventory stock tracker to use
     */
    public void setInventoryStockTracker(InventoryStockTracker inventoryStockTracker) {
        this.inventoryStockTracker = inventoryStockTracker;
    }
}
