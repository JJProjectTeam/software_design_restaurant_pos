package com.softwaredesign.project.view;

import com.softwaredesign.project.controller.BaseController;
import com.softwaredesign.project.controller.ConfigurationController;
import com.softwaredesign.project.mediator.RestaurantViewMediator;

import jexer.*;

public abstract class ConfigurationView implements View, ConfigurableView {
    protected final RestaurantApplication app;
    protected TWindow window;
    protected RestaurantViewMediator mediator;
    protected TTableWidget configTable;

    public ConfigurationView(RestaurantApplication app) {
        if (app == null) {
            throw new IllegalArgumentException("RestaurantApplication cannot be null");
        }
        System.out.println("[ConfigurationView] Constructor called");
        this.app = app;
        this.mediator = RestaurantViewMediator.getInstance();
    }

    @Override
    public void initialize(TWindow window) {
        this.window = window;
        setupView();
    }

    @Override
    public void cleanup() {
        window.close();
    }

    @Override
    public TWindow getWindow() {
        return window;
    }

    @Override
    public void setupView() {
        setupCommonElements();
        setupSpecificElements();
        setupNavigationButtons();
    }

    protected void setupCommonElements() {
        try {
            // Add the configuration title and money label
            window.addLabel("Configuration Settings", 2, 2);
            window.addLabel("$", window.getWidth() - 15, 2);
            
        } catch (Exception e) {
            System.err.println("[ConfigurationView] Error in setupCommonElements: " + e.getMessage());
            e.printStackTrace();
        }
    }

    protected abstract void setupSpecificElements();
    
    protected abstract boolean validateConfiguration();

    protected TAction nullAction = new TAction() {
        public void DO() {
        }
    };

    protected void setupNavigationButtons() {
        try {
            window.addButton("Next", window.getWidth() - 15, window.getHeight() - 4, new TAction() {
                public void DO() {
                    if (validateConfiguration()) {
                        onNextPressed();
                    }
                }
            });

            window.addButton("Back", 2, window.getHeight() - 4, new TAction() {
                public void DO() {
                    onBackPressed();
                }
            });
        } catch (Exception e) {
            System.err.println("[ConfigurationView] Error in setupNavigationButtons: " + e.getMessage());
            e.printStackTrace();
        }
    }

    protected void showError(String message) {
        try {
            // Use a message box instead of a label
            new TMessageBox(window.getApplication(), "Error", message, TMessageBox.Type.OK);
        } catch (Exception e) {
            System.err.println("[ConfigurationView] Error showing error message box: " + e.getMessage());
            e.printStackTrace();
        }
    }

    protected void showWarning(String message) {
        System.out.println("[ConfigurationView] showWarning called with message: " + message);
        try {
            // Use a message box instead of a label
            new TMessageBox(window.getApplication(), "Warning", message, TMessageBox.Type.OK);
        } catch (Exception e) {
            System.err.println("[ConfigurationView] Error showing warning message box: " + e.getMessage());
            e.printStackTrace();
        }
    }

    protected void clearWarning() {
        // No action needed since we're using message boxes that disappear when closed
    }

    protected abstract void onNextPressed();
    protected abstract void onBackPressed();

    @Override
    public void onUpdate(BaseController controller) {

    }

}
