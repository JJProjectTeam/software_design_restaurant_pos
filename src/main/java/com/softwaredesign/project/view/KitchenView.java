package com.softwaredesign.project.view;

import jexer.*;

public class KitchenView extends GamePlayView {
    
    public KitchenView(RestaurantApplication app) {
        super(app);
    }

    @Override
    protected void setupView() {
        super.setupView();
    }

    @Override
    protected void addViewContent() {
        window.addLabel("Kitchen", 2, 6);
        window.addLabel("Order 1", 2, 8);
        window.addLabel("Order 2", 2, 10);
        window.addLabel("Order 3", 2, 12);
        
        window.addButton("Complete Order 1", 15, 8, new TAction() {
            public void DO() {
                // TODO: Implement order completion
            }
        });
    }
}
