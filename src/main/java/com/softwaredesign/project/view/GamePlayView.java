package com.softwaredesign.project.view;

import jexer.*;
import com.softwaredesign.project.model.BankBalanceSingleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class GamePlayView implements View {
    private static final Logger logger = LoggerFactory.getLogger(GamePlayView.class);
    protected static final int TAB_Y = 1;  
    protected static final int TAB_WIDTH = 15;  
    protected static final int TAB_SPACING = 2;  

    protected final RestaurantApplication app;
    protected TWindow window;
    protected double bankBalance = 0.0;
    protected TLabel bankBalanceLabel;

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
        logger.info("[GamePlayView] Setting up view with navigation tabs");
        // Create and store reference to bank balance label
        bankBalanceLabel = window.addLabel(String.format("Bank Balance: $%.2f", bankBalance), 
        window.getWidth() - 30, 2);
        createNavigationTabs();
        addViewContent();
    }
    
    protected void createNavigationTabs() {
        logger.info("[GamePlayView] Creating navigation tabs for " + this.getClass().getSimpleName());
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
            logger.error("[GamePlayView] Cannot add tab - window is null");
            return;
        }
        logger.info("[GamePlayView] Adding tab: " + label + " at position " + position);
        int x = position * (TAB_WIDTH + TAB_SPACING);
        TButton tab = window.addButton(label, x, TAB_Y, new TAction() {
            public void DO() {
                app.showView(destination);
            }
        });
        tab.setWidth(TAB_WIDTH);
    }
    

    protected void setBankBalance(double newBalance) {
        this.bankBalance = newBalance;
        updateBankBalanceLabel();
    }

    // Add method to update the label
    private void updateBankBalanceLabel() {
        if (bankBalanceLabel != null) {
            try {
                bankBalanceLabel.setLabel(String.format("Bank Balance: $%.2f", bankBalance));
                logger.info("[GamePlayView] Updated bank balance label to: $" + String.format("%.2f", bankBalance));
            } catch (Exception e) {
                logger.error("[GamePlayView] Error updating bank balance label: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    // Abstract method for view-specific content
    protected abstract void addViewContent();


}
