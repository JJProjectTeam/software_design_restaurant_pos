package com.softwaredesign.project.kitchen;

import com.softwaredesign.project.engine.Entity;
import com.softwaredesign.project.engine.GameEngine;
import java.util.ArrayList;
import java.util.List;

public class Kitchen extends Entity {
    private final GrillStation grillStation;
    private final PlatingStation platingStation;
    private final List<Order> completedOrders;
    private int nextOrderId;

    public Kitchen() {
        this.grillStation = new GrillStation();
        this.platingStation = new PlatingStation();
        this.completedOrders = new ArrayList<>();
        this.nextOrderId = 1;

        // Register stations with the game engine
        GameEngine.getInstance().registerEntity(grillStation);
        GameEngine.getInstance().registerEntity(platingStation);
    }

    @Override
    public void readState() {
        // Check for completed orders - this will probably pass onto the write phase in the form of waiters
        for (Order order : completedOrders) {
            if (order.isComplete()) {
                System.out.println("Order " + order.getOrderId() + " is complete!");
            }
        }
    }

    @Override
    public void writeState() {
        // No state updates needed in write phase
    }

    public void submitOrder() {
        Order order = new Order(nextOrderId++);
        grillStation.addOrder(order);
    }

    // Method to move orders between stations
    public void processOrders() {
        // Move grilled orders to plating station
        if (grillStation.currentOrder != null && grillStation.currentOrder.isGrilled()) {
            platingStation.addOrder(grillStation.currentOrder);
            grillStation.currentOrder = null;
        }

        // Move completed orders to the completed list
        if (platingStation.currentOrder != null && platingStation.currentOrder.isComplete()) {
            completedOrders.add(platingStation.currentOrder);
            platingStation.currentOrder = null;
        }
    }
}
