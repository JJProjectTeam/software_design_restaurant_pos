package com.softwaredesign.project.model;
import java.util.List;
import java.util.ArrayList;

import com.softwaredesign.project.model.States.TodoState;	


public class Order {
    private List<Recipe> items = new ArrayList<>();
    private State state;


    public Order(List<Recipe> items) {
        this.items = items;
        this.state = new TodoState();
    }

    public void setState(State state) {
        this.state = state;
    }

    public void processOrder() {
        state.processOrder(this);
    }

    public void cancelOrder() {
        state.cancelOrder(this);
    }
    
    public void pauseOrder() {
        state.pauseOrder(this);
    }

    public void resumeOrder() {
        state.resumeOrder(this);
    }

    public void completeOrder() {
        state.completeOrder(this);
    }

    public List<Recipe> getItems() {
        return items;
    }
}

