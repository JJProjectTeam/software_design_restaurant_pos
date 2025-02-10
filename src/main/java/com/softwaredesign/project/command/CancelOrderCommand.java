package com.softwaredesign.project.command;

import com.softwaredesign.project.model.Order;
import com.softwaredesign.project.orderState.OrderState;

public class CancelOrderCommand implements OrderCommand {
    private final Order order;
    private OrderState previousState;
    
    public CancelOrderCommand(Order order) {
        this.order = order;
    }
    
    @Override
    public void execute() {
        // Store the actual state object before cancelling
        this.previousState = order.getState();
        order.cancelOrder();
    }
    
    @Override
    public void undo() {
        // Directly restore the previous state instead of trying to transition
        if (previousState != null) {
            order.restoreState(previousState);
        }
    }
}
