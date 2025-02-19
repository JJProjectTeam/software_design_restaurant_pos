package com.softwaredesign.project.view;

import jexer.*;

public abstract class GeneralView implements View {
    protected final RestaurantApplication app;
    protected TWindow window;

    public GeneralView(RestaurantApplication app) {
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

    protected abstract void setupView();

    @Override
    public TWindow getWindow() {
        return window;
    }
}
