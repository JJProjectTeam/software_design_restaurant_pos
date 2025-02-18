package com.softwaredesign.project.order;

import java.util.*;

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
}