package com.softwaredesign.project.inventory;

import java.util.HashMap;
import java.util.Map;

/**
 * Observer that tracks current stock levels and provides functionality to check if orders are possible
 */
public class InventoryStockTracker implements IObserver {
    private final Map<String, Integer> stockLevels;

    public InventoryStockTracker() {
        this.stockLevels = new HashMap<>();
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
}
