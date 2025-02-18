package com.softwaredesign.project.view;

import jexer.*;

public class InventoryView extends GamePlayView {
    public InventoryView() throws Exception {
        super();
    }
    @Override
    protected void initializeUI(TWindow window) {
        // Add a title label
        window.addLabel("Configuration Settings", 2, 2);
        
        window.addLabel("Select ingredient to configure:", 2, 3);
    }

    @Override
    protected String getToggleButtonText(TWindow window) {
        return "Go to Kitchen";
    }

    @Override
    protected void handleToggleView(TWindow window) {
        try {
            window.close();
            new KitchenView().run();
        } catch (Exception e) {
            messageBox("Error", "Failed to switch to Kitchen");
        }
    }

    @Override
    protected void addSpecificGameplayUI(TWindow window) {
        window.addLabel("Inventory View", 25, 2);
        window.addButton("End Game", window.getWidth() - 15, 
            window.getHeight() - 4, new TAction() {
                public void DO() {
                    try {
                        window.close();
                        new EndOfGameView().run();
                    } catch (Exception e) {
                        messageBox("Error", "Failed to end game");
                    }
                }
            });
    }
}
