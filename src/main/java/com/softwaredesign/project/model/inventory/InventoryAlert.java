package com.softwaredesign.project.model.inventory;

import com.softwaredesign.project.views.InventoryAlertView;

public class InventoryAlert implements IObserver {
    private final int lowStockThreshold;
    private final InventoryAlertView view;

    public InventoryAlert(int lowStockThreshold, InventoryAlertView view) {
        this.lowStockThreshold = lowStockThreshold;
        this.view = view;
    }

    public boolean isLowStock(int quantity) {
        return quantity <= lowStockThreshold;
    }

    @Override
    public void update(String ingredient, int quantity) {
        // Only notify view if stock is low
        if (isLowStock(quantity)) {
            view.showLowStockWarning(ingredient, quantity);
        }
        // Show the stock update - TODO remove when view is implemented
        view.showStockUpdate(ingredient, quantity);
    }
}
