package com.softwaredesign.project.inventory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Observer that tracks current stock levels and provides functionality to check if orders are possible
 */
public class InventoryStockTracker implements IObserver {
    private final Map<String, Integer> stockLevels;
    // Map to track reserved ingredients: orderId -> (ingredientName -> reservedQuantity)
    private final Map<String, Map<String, Integer>> reservedIngredients;

    public InventoryStockTracker() {
        this.stockLevels = new HashMap<>();
        this.reservedIngredients = new ConcurrentHashMap<>();
    }

    @Override
    public void update(String ingredient, int quantity) {
        stockLevels.put(ingredient, quantity);
    }

    /**
     * Checks if an order with the given ingredients and quantities can be fulfilled
     * @param ingredients Map of ingredient names to required quantities
     * @return true if the order can be fulfilled, false otherwise
     */
    public boolean canFulfillOrder(Map<String, Integer> ingredients) {
        for (Map.Entry<String, Integer> entry : ingredients.entrySet()) {
            String ingredient = entry.getKey();
            int requiredQuantity = entry.getValue();
            
            // Check if we have the ingredient and enough stock
            Integer currentStock = stockLevels.get(ingredient);
            if (currentStock == null || currentStock < requiredQuantity) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Reserves ingredients for an order by marking them as unavailable for other orders
     * @param orderId The ID of the order for which to reserve ingredients
     * @param ingredients Map of ingredient names to required quantities
     * @return true if all ingredients could be reserved, false otherwise
     */
    public synchronized boolean reserveIngredientsForOrder(String orderId, Map<String, Integer> ingredients) {
        // Check if we can fulfill the order first
        for (Map.Entry<String, Integer> entry : ingredients.entrySet()) {
            String ingredient = entry.getKey();
            int requiredQuantity = entry.getValue();
            
            // Get the available quantity (current stock minus what's already reserved)
            int totalReserved = getTotalReservedQuantity(ingredient);
            Integer currentStock = stockLevels.get(ingredient);
            
            if (currentStock == null || (currentStock - totalReserved) < requiredQuantity) {
                // Not enough available stock after accounting for reservations
                return false;
            }
        }
        
        // If we have enough of all ingredients, reserve them
        Map<String, Integer> orderReservation = new HashMap<>();
        for (Map.Entry<String, Integer> entry : ingredients.entrySet()) {
            orderReservation.put(entry.getKey(), entry.getValue());
        }
        
        // Store the reservation and return success
        reservedIngredients.put(orderId, orderReservation);
        return true;
    }
    
    /**
     * Releases reserved ingredients for an order
     * @param orderId The ID of the order for which to release ingredients
     */
    public synchronized void releaseReservedIngredients(String orderId) {
        reservedIngredients.remove(orderId);
    }
    
    /**
     * Get the total quantity of a specific ingredient reserved across all orders
     * @param ingredient The ingredient name
     * @return The total reserved quantity
     */
    private int getTotalReservedQuantity(String ingredient) {
        int total = 0;
        for (Map<String, Integer> reservation : reservedIngredients.values()) {
            Integer amount = reservation.get(ingredient);
            if (amount != null) {
                total += amount;
            }
        }
        return total;
    }

    /**
     * Gets the current stock level of an ingredient
     * @param ingredient name of the ingredient
     * @return current stock level, or 0 if ingredient doesn't exist
     */
    public int getCurrentStock(String ingredient) {
        return stockLevels.getOrDefault(ingredient, 0);
    }

    /**
     * Gets a map of all current stock levels
     * @return unmodifiable map of ingredient names to their current stock levels
     */
    public Map<String, Integer> getAllStockLevels() {
        return Map.copyOf(stockLevels);
    }
    
    /**
     * Gets the current available stock (accounting for reservations)
     * @param ingredient name of the ingredient
     * @return available stock (current stock minus reserved), or 0 if ingredient doesn't exist
     */
    public int getAvailableStock(String ingredient) {
        int currentStock = getCurrentStock(ingredient);
        int reservedQuantity = getTotalReservedQuantity(ingredient);
        return Math.max(0, currentStock - reservedQuantity);
    }
}
