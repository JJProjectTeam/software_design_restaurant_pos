package com.softwaredesign.project.model.States;

import com.softwaredesign.project.model.Order;
import com.softwaredesign.project.model.State;

public class TodoState implements State {
    @Override
    public void processOrder(Order order) {
        order.setState(new DoingState());
    }

    @Override
    public void cancelOrder(Order order) {
        order.setState(new CancelledState());
    }
}
