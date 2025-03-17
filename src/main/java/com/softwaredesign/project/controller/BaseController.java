package com.softwaredesign.project.controller;

import com.softwaredesign.project.mediator.RestaurantViewMediator;

public abstract class BaseController {
    protected final RestaurantViewMediator mediator;
    protected final String type;
    protected BaseController(String type) {
        this.mediator = RestaurantViewMediator.getInstance();
        this.type = type;
        mediator.registerController(type, this);
    }

    public String getType() {
        return type;
    }

    /**
     * Update all registered views with the current state
     */
    public abstract void updateView();
    public void onUserInput(){
    }
}
