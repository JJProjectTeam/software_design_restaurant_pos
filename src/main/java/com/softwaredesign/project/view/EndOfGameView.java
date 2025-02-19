package com.softwaredesign.project.view;


public class EndOfGameView extends GeneralView{
    public EndOfGameView(RestaurantApplication app) {
        super(app);
    }

    @Override
    protected void setupView() {
        window.addLabel("Game Over", 2, 6);
    }
}
