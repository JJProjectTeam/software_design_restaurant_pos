package com.softwaredesign.project.command;

import com.softwaredesign.project.model.Order;
import com.softwaredesign.project.orderState.OrderState;

public class PauseOrderCommand implements OrderCommand {
    private final Order order;
    private OrderState previousState;
    
    public PauseOrderCommand(Order order) {
        this.order = order;
    }
    
    @Override
    public void execute() {
        this.previousState = order.getState();
        order.pauseOrder();
    }
    
    @Override
    public void undo() {
        if (previousState != null) {
            order.restoreState(previousState);
        }
    }
}
