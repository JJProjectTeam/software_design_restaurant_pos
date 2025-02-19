package com.softwaredesign.project.view;

import jexer.*;

public abstract class GamePlayView extends GeneralView {
    
    protected static final int TAB_Y = 1;  
    protected static final int TAB_WIDTH = 15;  
    protected static final int TAB_SPACING = 2;  

    public GamePlayView(RestaurantApplication app) {
        super(app);
    }

    @Override
    protected void setupView() {
        createNavigationTabs();
        addViewContent();
    }
    
    protected void createNavigationTabs() {
        // Add navigation tabs at the top
        if (this instanceof KitchenView) {
            addTab("Dining Room", ViewType.DINING_ROOM, 1);
            addTab("Inventory", ViewType.INVENTORY, 2);
        } else if (this instanceof DiningRoomView) {
            addTab("Kitchen", ViewType.KITCHEN, 0);
            addTab("Inventory", ViewType.INVENTORY, 2);
        } else if (this instanceof InventoryView) {
            addTab("Kitchen", ViewType.KITCHEN, 0);
            addTab("Dining Room", ViewType.DINING_ROOM, 1);
        }
    }
    
    protected void addTab(String label, ViewType destination, int position) {
        int x = position * (TAB_WIDTH + TAB_SPACING);
        TButton tab = window.addButton(label, x, TAB_Y, new TAction() {
            public void DO() {
                app.showView(destination);
            }
        });
        tab.setWidth(TAB_WIDTH);
    }
    
    // Abstract method for view-specific content
    protected abstract void addViewContent();
}
