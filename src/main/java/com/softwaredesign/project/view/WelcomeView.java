package com.softwaredesign.project.view;

import jexer.*;

public class WelcomeView extends GeneralView {

    public WelcomeView(RestaurantApplication app) {
        super(app);
    }

    @Override
    protected void setupView() {
        window.addLabel("Welcome to OOPsies Bistro", 2, 2);
        window.addButton("Configure Game", 2, 4, new TAction() {
            public void DO() {
                app.showView(ViewType.CONFIGURATION);
            }
        });
    }
}
