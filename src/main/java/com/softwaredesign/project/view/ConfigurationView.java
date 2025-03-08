package com.softwaredesign.project.view;

import com.softwaredesign.project.controller.BaseController;
import com.softwaredesign.project.controller.ConfigurationController;
import com.softwaredesign.project.mediator.RestaurantViewMediator;

import jexer.*;

public abstract class ConfigurationView implements View, ConfigurableView {
    protected final RestaurantApplication app;
    protected TWindow window;
    // protected TLabel moneyLabel;
    // protected TLabel errorLabel;
    // protected TLabel warningLabel;
    protected RestaurantViewMediator mediator;
    protected TTableWidget configTable;

    public ConfigurationView(RestaurantApplication app) {
        if (app == null) {
            throw new IllegalArgumentException("RestaurantApplication cannot be null");
        }
        System.out.println("[ConfigurationView] Constructor called");
        this.app = app;
        this.mediator = RestaurantViewMediator.getInstance();
        mediator.registerView("Configuration", this);
        System.out.println("[ConfigurationView] Constructor completed");
    }

    @Override
    public void initialize(TWindow window) {
        System.out.println("[ConfigurationView] initialize called with window: " + window);
        this.window = window;
        setupView();
    }

    @Override
    public void cleanup() {
        System.out.println("[ConfigurationView] cleanup called");
        window.close();
    }

    @Override
    public TWindow getWindow() {
        return window;
    }

    @Override
    public void setupView() {
        System.out.println("[ConfigurationView] setupView called");
        setupCommonElements();
        setupSpecificElements();
        setupNavigationButtons();
        System.out.println("[ConfigurationView] setupView completed");
    }

    protected void setupCommonElements() {
        System.out.println("[ConfigurationView] setupCommonElements started");
        try {
            // Initialize labels with empty strings first to ensure they have valid attributes
            // errorLabel = window.addLabel("", 2, window.getHeight() - 6);
            // warningLabel = window.addLabel("", 2, window.getHeight() - 5);
            
            // Now add the other labels
            window.addLabel("Configuration Settings", 2, 2);
            window.addLabel("$", window.getWidth() - 15, 2);
            // moneyLabel = window.addLabel("1000", window.getWidth() - 13, 2);
            
            System.out.println("[ConfigurationView] Labels initialized");
            
            // Clear warnings after labels are fully initialized
            clearWarning();
            System.out.println("[ConfigurationView] setupCommonElements completed");
        } catch (Exception e) {
            System.err.println("[ConfigurationView] Error in setupCommonElements: " + e.getMessage());
            e.printStackTrace();
        }
    }

    protected abstract void setupSpecificElements();
    
    protected abstract boolean validateConfiguration();

    protected TAction nullAction = new TAction() {
        public void DO() {
            System.out.println("[ConfigurationView] nullAction DO method called");
        }
    };

    protected void setupNavigationButtons() {
        System.out.println("[ConfigurationView] setupNavigationButtons started");
        try {
            window.addButton("Next", window.getWidth() - 15, window.getHeight() - 4, new TAction() {
                public void DO() {
                    System.out.println("[ConfigurationView] Next button pressed");
                    if (validateConfiguration()) {
                        onNextPressed();
                    }
                }
            });

            window.addButton("Back", 2, window.getHeight() - 4, new TAction() {
                public void DO() {
                    System.out.println("[ConfigurationView] Back button pressed");
                    onBackPressed();
                }
            });
            System.out.println("[ConfigurationView] setupNavigationButtons completed");
        } catch (Exception e) {
            System.err.println("[ConfigurationView] Error in setupNavigationButtons: " + e.getMessage());
            e.printStackTrace();
        }
    }

    protected void showError(String message) {
        System.out.println("[ConfigurationView] showError called with message: " + message);
        // if (errorLabel != null) {
        //     try {
        //         errorLabel.setLabel("Error: " + message);
        //         errorLabel.setColorKey("RED");
        //     } catch (Exception e) {
        //         System.err.println("[ConfigurationView] Error setting error label: " + e.getMessage());
        //         e.printStackTrace();
        //     }
        // } else {
        //     System.err.println("[ConfigurationView] ERROR: errorLabel is null when showing error: " + message);
        // }
    }

    protected void showWarning(String message) {
        System.out.println("[ConfigurationView] showWarning called with message: " + message);
        // if (warningLabel != null) {
        //     try {
        //         // warningLabel.setLabel("Warning: " + message);
        //         // warningLabel.setColorKey("YELLOW");
        //     } catch (Exception e) {
        //         System.err.println("[ConfigurationView] Error setting warning label: " + e.getMessage());
        //         e.printStackTrace();
        //     }
        // } else {
        //     System.err.println("[ConfigurationView] ERROR: warningLabel is null when showing warning: " + message);
        // }
    }

    protected void clearWarning() {
        System.out.println("[ConfigurationView] clearWarning called");
        try {
            // if (errorLabel != null) {
            //     errorLabel.setLabel("");
            // } else {
            //     System.err.println("[ConfigurationView] ERROR: errorLabel is null when clearing warnings");
            // }
            // if (warningLabel != null) {
            //     warningLabel.setLabel("");
            // } else {
            //     System.err.println("[ConfigurationView] ERROR: warningLabel is null when clearing warnings");
            // }
        } catch (Exception e) {
            System.err.println("[ConfigurationView] Error clearing warnings: " + e.getMessage());
            e.printStackTrace();
        }
    }

    protected abstract void onNextPressed();
    protected abstract void onBackPressed();

    @Override
    public void onUpdate(BaseController controller) {

    }

}
