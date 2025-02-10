package com.softwaredesign.project.orderState;

import com.softwaredesign.project.model.Order;

public class InactiveState implements OrderState{
    private final Order order;

    public InactiveState(Order order) {
        this.order = order;
    }

    @Override
    public void processOrder() {
        System.out.println("Can't process an inactive order");
    }

    @Override
    public void cancelOrder() {
        System.out.println("Can't cancel an inactive order");
    }

    @Override
    public void pauseOrder() {
        System.out.println("Can't pause an inactive order");
    }
    
    
}
