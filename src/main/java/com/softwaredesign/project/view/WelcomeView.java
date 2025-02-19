package com.softwaredesign.project.view;

import jexer.*;

public class WelcomeView implements View {
    private RestaurantApplication app;

    public WelcomeView(RestaurantApplication app) {
        this.app = app;
    }

    @Override
    public void initialize(TWindow window) {
        window.addLabel("Welcome to OOPsies Bistro", 2, 2);
        window.addButton("Configure Game", 2, 4, new TAction() {
            public void DO() {
                app.showView(new ConfigurationView(app));
            }
        });
    }
}
