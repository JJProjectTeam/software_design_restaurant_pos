package com.softwaredesign.project.view;

import jexer.*;

public abstract class GamePlayView extends GeneralView {
    public GamePlayView() throws Exception {
        super();
    }
    
    @Override
    protected void initializeUI(TWindow window) {
        addSideButtons(window);
        addSpecificGameplayUI(window);
    }
    
    private void addSideButtons(TWindow window) {
        int currentY = 5;
        
        // Toggle button (Kitchen/Dining Room)
        window.addButton(getToggleButtonText(window), SIDE_BUTTON_X, currentY, new TAction() {
            public void DO() {
                handleToggleView(window);
            }
        });
        currentY += SIDE_BUTTON_HEIGHT + 1;
        
        // Inventory button
        window.addButton("Show Inventory", SIDE_BUTTON_X, currentY, new TAction() {
            public void DO() {
                showInventory();
            }
        });
        currentY += SIDE_BUTTON_HEIGHT + 1;
        
        // Menu button
        window.addButton("View Menu", SIDE_BUTTON_X, currentY, new TAction() {
            public void DO() {
                showMenu();
            }
        });
    }
    
    protected abstract String getToggleButtonText(TWindow window);
    protected abstract void handleToggleView(TWindow window);
    protected abstract void addSpecificGameplayUI(TWindow window);
    
    private void showInventory() {
        // TODO: Implement inventory view
    }
    
    private void showMenu() {
        // Create popup window for menu
        TWindow menuPopup = new TWindow(this, "Menu", 20, 2, 40, 20);
        menuPopup.addLabel("Menu Items:", 2, 2);
        // TODO: Add menu items
        menuPopup.show();
    }
}
