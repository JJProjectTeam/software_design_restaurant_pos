package com.softwaredesign.project.orderfulfillment;

import java.util.*;
import java.util.stream.Collectors;

import com.softwaredesign.project.order.Meal;

public class CollectionPoint {
    private Map<String, List<Meal>> completedMeals;
    private Map<String, Integer> mealsPerOrder;
    private Queue<String> readyOrders; // FIFO for completed orders

    public CollectionPoint() {
        this.completedMeals = new HashMap<>();
        this.mealsPerOrder = new HashMap<>();
        this.readyOrders = new LinkedList<>();
    }

    public void registerOrder(String orderId, int totalMeals) {
        mealsPerOrder.put(orderId, totalMeals);
        completedMeals.put(orderId, new ArrayList<>());
    }

    public void addCompletedMeal(Meal meal) {
        String orderId = meal.getOrderId();
        if (!completedMeals.containsKey(orderId)) {
            throw new IllegalStateException("Order " + orderId + " not registered");
        }
        completedMeals.get(orderId).add(meal);

        // Check if order is complete
        if (isOrderComplete(orderId)) {
            readyOrders.add(orderId);
        }
    }

    public boolean isOrderComplete(String orderId) {
        return completedMeals.containsKey(orderId) &&
                completedMeals.get(orderId).size() == mealsPerOrder.get(orderId);
    }

    public List<Meal> collectNextOrder() {
        if (readyOrders.isEmpty()) {
            return null;
        }
        String orderId = readyOrders.poll();
        List<Meal> meals = completedMeals.remove(orderId);
        mealsPerOrder.remove(orderId);
        return meals;
    }

    public boolean hasReadyOrders() {
        return !readyOrders.isEmpty();
    }
    
    /**
     * Returns a set of order IDs that have at least one completed meal but are not fully complete
     * @return List of order IDs that are partially completed
     */
    public List<String> getPartiallyCompletedOrderIds() {
        List<String> partiallyCompleted = new ArrayList<>();
        
        for (Map.Entry<String, List<Meal>> entry : completedMeals.entrySet()) {
            String orderId = entry.getKey();
            if (!entry.getValue().isEmpty() && !readyOrders.contains(orderId)) {
                partiallyCompleted.add(orderId);
            }
        }
        
        return partiallyCompleted;
    }
    
    /**
     * Gets the number of completed meals for a specific order
     * @param orderId The order ID to check
     * @return The number of completed meals for the order
     */
    public int getCompletedMealsCount(String orderId) {
        if (!completedMeals.containsKey(orderId)) {
            return 0;
        }
        return completedMeals.get(orderId).size();
    }
    
    /**
     * Gets the total number of meals expected for a specific order
     * @param orderId The order ID to check
     * @return The total number of meals expected for the order
     */
    public int getTotalMealsExpected(String orderId) {
        if (!mealsPerOrder.containsKey(orderId)) {
            return 0;
        }
        return mealsPerOrder.get(orderId);
    }
}