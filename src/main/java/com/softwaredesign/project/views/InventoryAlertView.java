package com.softwaredesign.project.views;

import com.softwaredesign.project.model.inventory.IObserver;

public class InventoryAlertView implements IObserver {

    public InventoryAlertView() {
    }

    @Override
    public void update(String ingredient, int quantity) {
        showStockUpdate(ingredient, quantity);
    }

    public void showStockUpdate(String ingredient, int quantity) {
        StringBuilder message = new StringBuilder();
        message.append("\nInventory Update for ").append(ingredient).append(":\n");
        message.append("Current stock: ").append(quantity).append("\n");
        System.out.print(message.toString());
    }

    public void showLowStockWarning(String ingredient, int quantity) {
        String warning = String.format("WARNING: Low stock alert for %s! Only %d units remaining!\n",
                ingredient, quantity);
        System.out.print(warning);
    }
}
