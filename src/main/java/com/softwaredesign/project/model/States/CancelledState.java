package com.softwaredesign.project.model.States;

import com.softwaredesign.project.model.Order;
import com.softwaredesign.project.model.State;

public class CancelledState implements State {
    @Override
    public void processOrder(Order order) {
        order.setState(new DoingState());
    }
    
    @Override
    public void completeOrder(Order order) {
        order.setState(new DoneState());
    }

    // Should not be able to cancel an order that is already cancelled? I'm not sure since it's a method in the interface.
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

