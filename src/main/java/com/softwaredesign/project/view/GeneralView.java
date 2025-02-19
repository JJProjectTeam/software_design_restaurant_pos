package com.softwaredesign.project.view;

import jexer.TAction;
import jexer.TWindow;

public class GeneralView implements View {
    
    private RestaurantApplication app;

    public GeneralView(RestaurantApplication restaurantApplication) {
        this.app = restaurantApplication;
    }

    @Override
    public void initialize(TWindow window) {
        window.addLabel("Configuration Settings", 2, 2);
        int buttonX = window.getWidth() - "Restart Game".length() - 8;
        window.addButton("Restart Game", buttonX, 2, new TAction() {
            public void DO() {
                app.showView(new WelcomeView(app));
            }
        });
    }
}
