package com.softwaredesign.project.view;

import jexer.*;

public class InventoryView extends GamePlayView {
    
    public InventoryView(RestaurantApplication app) {
        super(app);
    }

    @Override
    protected void setupView() {
        super.setupView();
    }

    @Override
    protected void addViewContent() {
        window.addLabel("Inventory", 2, 6);
        window.addLabel("Items:", 2, 8);
    }
}
