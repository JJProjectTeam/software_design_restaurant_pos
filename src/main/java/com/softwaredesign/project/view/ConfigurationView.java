package com.softwaredesign.project.view;

import com.softwaredesign.project.controller.BaseController;
import com.softwaredesign.project.mediator.RestaurantViewMediator;

import jexer.*;

public abstract class ConfigurationView implements View, ConfigurableView {
    protected final RestaurantApplication app;
    protected TWindow window;
    protected RestaurantViewMediator mediator;
    protected TTableWidget configTable;
    protected double bankBalance = 0.0;
    protected TLabel bankBalanceLabel;

    public ConfigurationView(RestaurantApplication app) {
        if (app == null) {
            throw new IllegalArgumentException("RestaurantApplication cannot be null");
        }
        logger.info("[ConfigurationView] Constructor called");
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
            // Add the configuration title
            window.addLabel("Configuration Settings", 2, 2);
            
            // Create and store reference to bank balance label
            bankBalanceLabel = window.addLabel(String.format("Bank Balance: $%.2f", bankBalance), 
                window.getWidth() - 30, 2);
            
        } catch (Exception e) {
            logger.error("[ConfigurationView] Error in setupCommonElements: " + e.getMessage());
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
            logger.error("[ConfigurationView] Error in setupNavigationButtons: " + e.getMessage());
            e.printStackTrace();
        }
    }

    protected void showError(String message) {
        try {
            // Use a message box instead of a label
            new TMessageBox(window.getApplication(), "Error", message, TMessageBox.Type.OK);
        } catch (Exception e) {
            logger.error("[ConfigurationView] Error showing error message box: " + e.getMessage());
            e.printStackTrace();
        }
    }

    protected void showWarning(String message) {
        logger.info("[ConfigurationView] showWarning called with message: " + message);
        try {
            // Use a message box instead of a label
            new TMessageBox(window.getApplication(), "Warning", message, TMessageBox.Type.OK);
        } catch (Exception e) {
            logger.error("[ConfigurationView] Error showing warning message box: " + e.getMessage());
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

    // Add getter and setter for bank balance
    protected double getBankBalance() {
        return bankBalance;
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
            } catch (Exception e) {
                logger.error("[ConfigurationView] Error updating bank balance label: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

}
