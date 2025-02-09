package com.softwaredesign.project.orderState;

import com.softwaredesign.project.model.Order;

public class TodoState implements OrderState {
    private final Order order; // to enable inter-state transitions

    public TodoState(Order order) {
        this.order = order;
    }

    @Override
    public void processOrder() {
        System.out.println("Starting to process the order...");
        order.setState(new DoingState(order));
    }

    @Override
    public void cancelOrder() {
        System.out.println("Order cancelled before processing");
        // TBD how to handle this interaction
    }

    @Override
    public void pauseOrder() {
        System.out.println("Cannot pause an order that hasn't started");
    }
}
