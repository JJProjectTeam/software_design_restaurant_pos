package com.softwaredesign.project.controller;

import java.util.List;
import java.util.Queue;
import java.util.LinkedList;
import com.softwaredesign.project.model.Recipe;
import com.softwaredesign.project.model.Order;


public class OrderManager {
    private Queue<Order> orders;
    private Order currentOrder;

    public OrderManager() {
        orders = new LinkedList<>();
    }


    public void addOrder(Order order) {
        orders.add(order);
    }

    public void removeOrder(Order order) {
        orders.remove(order);
    }

    public Queue<Order> getAllOrders() {
        return orders;
    }



    public List<Recipe> getRecipes() {
        if (orders.isEmpty()) {
            throw new IllegalStateException("No orders to process");
        }
        else {
            // return the current order
            this.currentOrder = orders.poll();
            return currentOrder.getItems();
        }
    }


    public void setCurrentOrder(Order order) {
        currentOrder = order;
    }

    public void stepForwardOrder() {
        // I want to skip this method for now 
        // because we don't have a way to step forward an order
        // so we will just return
        return;
    }

    public void stepBackwardOrder() {
        // I want to skip this method for now 
        // because we don't have a way to step backward an order
        // so we will just return
        return;
    }

    
}

