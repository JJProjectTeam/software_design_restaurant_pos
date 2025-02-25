package com.softwaredesign.project.view;

import jexer.*;

public abstract class ConfigurationView extends GeneralView {
    protected TLabel moneyLabel;
    protected TLabel errorLabel;
    protected TLabel warningLabel;

    public ConfigurationView(RestaurantApplication app) {
        super(app);
    }

    @Override
    protected void setupView() {
        setupCommonElements();
        
        setupSpecificElements();
        
        setupNavigationButtons();
    }

    protected void setupCommonElements() {
        window.addLabel("Configuration Settings", 2, 2);
        window.addLabel("$", window.getWidth() - 15, 2);
        moneyLabel = window.addLabel("1000", window.getWidth() - 13, 2);
        
        warningLabel = window.addLabel("", 2, 18);
        errorLabel = window.addLabel("", 2, 19);
    }

    protected abstract void setupSpecificElements();
    
    protected abstract boolean validateConfiguration();

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

    protected abstract void onNextPressed();
    protected abstract void onBackPressed();

    protected void showError(String message) {
        errorLabel.setLabel(message);
        System.out.println("[ConfigurationView] Error: " + message);
    }

    protected void showWarning(String message) {
        warningLabel.setLabel(message);
    }

    protected void clearError() {
        errorLabel.setLabel("");
    }

    protected void clearWarning() {
        warningLabel.setLabel("");
    }

    protected void updateMoney(int amount) {
        moneyLabel.setLabel(String.valueOf(amount));
    }
    protected TAction nullAction = new TAction() {
        public void DO() {
        }
    };
}
