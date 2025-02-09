package com.softwaredesign.project.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.time.LocalDateTime;

public class OrderManager {
    private final Map<String, Order> orders;
    private int orderCounter;

    public OrderManager() {
        this.orders = new HashMap<>();
        this.orderCounter = 1;
    }

    // Create and add a new order
    public Order createOrder() {
        String orderId = generateOrderId();
        Order newOrder = new Order(orderId);
        orders.put(orderId, newOrder);
        return newOrder;
    }

    // Get order by ID
    public Order getOrder(String orderId) {
        return orders.get(orderId);
    }

    // Get all orders
    public List<Order> getAllOrders() {
        return new ArrayList<>(orders.values());
    }

    // Get orders by state
    public List<Order> getOrdersByState(Class<?> stateClass) {
        return orders.values().stream()
                .filter(order -> order.getState().getClass().equals(stateClass))
                .toList();
    }

    // Process specific order
    public void processOrder(String orderId) {
        Order order = orders.get(orderId);
        if (order != null) {
            order.processOrder();
        }
    }

    // Cancel specific order
    public void cancelOrder(String orderId) {
        Order order = orders.get(orderId);
        if (order != null) {
            order.cancelOrder();
        }
    }

    // Pause specific order
    public void pauseOrder(String orderId) {
        Order order = orders.get(orderId);
        if (order != null) {
            order.pauseOrder();
        }
    }

    // Remove order from the system
    public boolean removeOrder(String orderId) {
        return orders.remove(orderId) != null;
    }

    // Check if order exists
    public boolean hasOrder(String orderId) {
        return orders.containsKey(orderId);
    }

    // Get total number of orders
    public int getOrderCount() {
        return orders.size();
    }

    // Clear all completed orders
    public void clearCompletedOrders() {
        orders.entrySet().removeIf(entry -> 
            entry.getValue().getState().getClass().getSimpleName().equals("DoneState"));
    }

    public void archiveCompletedOrders() {
        orders.entrySet().removeIf(entry -> 
            entry.getValue().getState().getClass().getSimpleName().equals("DoneState"));
    }

    // Generate a unique order ID
    private String generateOrderId() {
        LocalDateTime now = LocalDateTime.now();
        String dateStr = String.format("%d%02d%02d", 
            now.getYear(), now.getMonthValue(), now.getDayOfMonth());
        String orderId = String.format("ORD-%s-%04d", dateStr, orderCounter++);
        return orderId;
    }
}
