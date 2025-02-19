package com.softwaredesign.project.view;

import jexer.*;

public class ConfigurationView extends GeneralView {

    public ConfigurationView(RestaurantApplication app) {
        super(app);
    }

    @Override
    protected void setupView() {
        window.addLabel("Configuration Settings", 2, 6);
        window.addButton("Start Game", 2, 8, new TAction() {
            public void DO() {
                app.showView(ViewType.DINING_ROOM);
            }
        });
    }
}
