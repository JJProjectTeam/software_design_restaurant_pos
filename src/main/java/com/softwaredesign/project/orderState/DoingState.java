package com.softwaredesign.project.orderState;

import com.softwaredesign.project.model.Order;

public class DoingState implements OrderState {
    private final Order order; // to enable inter-state transitions

    public DoingState(Order order) {
        this.order = order;
    }

    @Override
    public void processOrder() {
        System.out.println("Completing the order...");
        order.setState(new DoneState(order));
    }

    @Override
    public void cancelOrder() {
        System.out.println("Order cancelled while in progress");
        order.setState(new TodoState(order));
    }

    @Override
    public void pauseOrder() {
        System.out.println("Order processing paused");
    }
}
