package com.softwaredesign.project.view;

import jexer.*;

public abstract class GamePlayView implements View {
    
    protected static final int TAB_Y = 1;  
    protected static final int TAB_WIDTH = 15;  
    protected static final int TAB_SPACING = 2;  

    protected final RestaurantApplication app;
    protected TWindow window;

    public GamePlayView(RestaurantApplication app) {
        if (app == null) {
            throw new IllegalArgumentException("RestaurantApplication cannot be null");
        }
        this.app = app;
    }

    @Override
    public void initialize(TWindow window) {
        if (window == null) {
            throw new IllegalArgumentException("Window cannot be null");
        }
        this.window = window;
        setupView();
    }

    @Override
    public void cleanup() {
        if (window != null) {
            window.close();
        }
    }

    @Override
    public TWindow getWindow() {
        return window;
    }
    
    @Override
    public void setupView() {
        System.out.println("[GamePlayView] Setting up view with navigation tabs");
        createNavigationTabs();
        addViewContent();
    }
    
    protected void createNavigationTabs() {
        System.out.println("[GamePlayView] Creating navigation tabs for " + this.getClass().getSimpleName());
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
        if (window == null) {
            System.err.println("[GamePlayView] Cannot add tab - window is null");
            return;
        }
        System.out.println("[GamePlayView] Adding tab: " + label + " at position " + position);
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
