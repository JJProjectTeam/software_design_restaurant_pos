package com.softwaredesign.project.model.States;

import com.softwaredesign.project.model.Order;
import com.softwaredesign.project.model.State;

public class CancelledState implements State {
    @Override
    public void resumeOrder(Order order) {
        order.setState(new DoingState());
    }
}

