package com.softwaredesign.project.view;

import jexer.*;

public class KitchenView extends GamePlayView {
    public KitchenView() throws Exception {
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
        return "Go to Inventory";
    }

    @Override
    protected void handleToggleView(TWindow window) {
        try {
            window.close();
            new InventoryView().run();
        } catch (Exception e) {
            messageBox("Error", "Failed to switch to Inventory");
        }
    }

    @Override
    protected void addSpecificGameplayUI(TWindow window) {
        window.addLabel("Kitchen View", 25, 2);
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
