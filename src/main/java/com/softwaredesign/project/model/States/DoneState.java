package com.softwaredesign.project.model.States;

import com.softwaredesign.project.model.Order;
import com.softwaredesign.project.model.State;

public class DoneState implements State {
    @Override
    public void processOrder(Order order) {
        order.setState(new DoingState());
    }
    
    // Once again, Should not be able to complete an order that is already done? I'm not sure since it's a method in the interface.
    @Override
    public void completeOrder(Order order) {
        order.setState(new DoneState());
    }
    
    @Override
    public void cancelOrder(Order order) {
        order.setState(new CancelledState());
    }

    @Override
    public void pauseOrder(Order order) {
        order.setState(new PausedState());
    }

    @Override
    public void resumeOrder(Order order) {
        order.setState(new DoingState());
    }  
}

