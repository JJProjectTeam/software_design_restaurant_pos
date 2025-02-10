package com.softwaredesign.project.model.States;

import com.softwaredesign.project.model.Order;
import com.softwaredesign.project.model.State;

public class DoneState implements State {
    @Override
    public void cancelOrder(Order order) {
        order.setState(new CancelledState());
    }
}

