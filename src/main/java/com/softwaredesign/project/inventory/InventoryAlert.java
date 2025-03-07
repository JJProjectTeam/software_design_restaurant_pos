package com.softwaredesign.project.inventory;

public class InventoryAlert implements IObserver {
    private final int lowStockThreshold;


    public InventoryAlert(int lowStockThreshold) {
        this.lowStockThreshold = lowStockThreshold;
    }


    public boolean isLowStock(int quantity) {
        return quantity <= lowStockThreshold;
    }


    @Override
    public void update(String ingredient, int quantity) {
        if (isLowStock(quantity)) {
            System.out.println("WARNING: Low stock alert for " + ingredient + " - only " + quantity + " remaining!");
        }
        System.out.println("Stock update: " + ingredient + " - " + quantity + " units in stock");
    }
}
