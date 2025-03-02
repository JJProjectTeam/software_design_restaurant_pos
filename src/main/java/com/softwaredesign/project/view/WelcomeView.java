package com.softwaredesign.project.view;

import jexer.*;

public class WelcomeView implements View {
    private final RestaurantApplication app;
    private TWindow window;

    public WelcomeView(RestaurantApplication app) {
        if (app == null) {
            throw new IllegalArgumentException("RestaurantApplication cannot be null");
        }
        this.app = app;
    }

    @Override
    public void initialize(TWindow window) {
        this.window = window;
        setupView();
    }

    @Override
    public void cleanup() {
        window.close();
    }

    @Override
    public TWindow getWindow() {
        return window;
    }

    @Override
    public void setupView() {
        window.addLabel("Welcome to Restaurant Simulator!", 2, 2);
        window.addButton("Start", 2, 4, new TAction() {
            public void DO() {
                app.showView(ViewType.DINING_ROOM);
            }
        });
    }
}
