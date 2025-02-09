package com.softwaredesign.project.orderState;

import com.softwaredesign.project.model.Order;

public class DoneState implements OrderState {
    private final Order order; // to enable inter-state transitions

    public DoneState(Order order) {
        this.order = order; 
    }

    @Override
    public void processOrder() {
        System.out.println("Order is already completed");
    }

    @Override
    public void cancelOrder() {
        System.out.println("Cannot cancel a completed order");
    }

    @Override
    public void pauseOrder() {
        System.out.println("Cannot pause a completed order");
    }
}
