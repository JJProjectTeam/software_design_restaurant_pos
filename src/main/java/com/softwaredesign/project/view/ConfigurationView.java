package com.softwaredesign.project.view;

import com.softwaredesign.project.controller.BaseController;
import com.softwaredesign.project.controller.ConfigurationController;
import com.softwaredesign.project.mediator.RestaurantViewMediator;

import jexer.*;

public abstract class ConfigurationView implements View, ConfigurableView {
    protected final RestaurantApplication app;
    protected TWindow window;
    protected TLabel moneyLabel;
    protected TLabel errorLabel;
    protected TLabel warningLabel;
    protected RestaurantViewMediator mediator;
    protected TTableWidget configTable;

    public ConfigurationView(RestaurantApplication app) {
        if (app == null) {
            throw new IllegalArgumentException("RestaurantApplication cannot be null");
        }
        this.app = app;
        this.mediator = RestaurantViewMediator.getInstance();
        mediator.registerView("Configuration", this);
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
        window.addLabel("Configuration Settings", 2, 2);
        window.addLabel("$", window.getWidth() - 15, 2);
        moneyLabel = window.addLabel("1000", window.getWidth() - 13, 2);
        
        // Initialize status labels with different positions
        errorLabel = window.addLabel("", 2, window.getHeight() - 6);
        warningLabel = window.addLabel("", 2, window.getHeight() - 5);
        
        clearWarning(); // Reset labels to initial state
    }

    protected abstract void setupSpecificElements();
    
    protected abstract boolean validateConfiguration();

    protected TAction nullAction = new TAction() {
        public void DO() {
        }
    };

    protected void setupNavigationButtons() {
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
    }

    protected void showError(String message) {
        if (errorLabel != null) {
            errorLabel.setLabel("Error: " + message);
            errorLabel.setColorKey("RED");
        }
    }

    protected void showWarning(String message) {
        if (warningLabel != null) {
            warningLabel.setLabel("Warning: " + message);
            warningLabel.setColorKey("YELLOW");
        }
    }

    protected void clearWarning() {
        if (errorLabel != null) {
            errorLabel.setLabel("");
        }
        if (warningLabel != null) {
            warningLabel.setLabel("");
        }
    }

    protected abstract void onNextPressed();
    protected abstract void onBackPressed();

    @Override
    public void onUpdate(BaseController controller) {

    }

}
