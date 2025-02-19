package com.softwaredesign.project.view;

import jexer.*;

public class DiningRoomView extends GamePlayView {
    
    public DiningRoomView(RestaurantApplication app) {
        super(app); 
    }

    @Override
    protected void setupView() {
        super.setupView();
    }

    @Override
    protected void addViewContent() {
        window.addLabel("Dining Room", 2, 6);
        window.addLabel("Tables", 2, 8);
    }
}
