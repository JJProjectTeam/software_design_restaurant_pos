package com.softwaredesign.project.model;

import com.softwaredesign.project.orderState.OrderState;
import com.softwaredesign.project.orderState.TodoState;

public class Order {
    private OrderState state;
    private String orderId;
    
    public Order(String orderId) {
        this.orderId = orderId;
        // Initial state is TodoState
        this.state = new TodoState(this);
    }

    public void setState(OrderState state) {
        this.state = state;
    }

    public OrderState getState() {
        return state;
    }

    public void processOrder() {
        state.processOrder();
    }

    public void cancelOrder() {
        state.cancelOrder();
    }

    public void pauseOrder() {
        state.pauseOrder();
    }

    public String getOrderId() {
        return orderId;
    }

    @Override
    public String toString() {
        return "Order{" +
                "orderId='" + orderId + '\'' +
                ", state=" + state.getClass().getSimpleName() +
                '}';
    }
}
