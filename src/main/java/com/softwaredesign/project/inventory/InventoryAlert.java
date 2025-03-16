package com.softwaredesign.project.inventory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InventoryAlert implements IObserver {
    private final int lowStockThreshold;
    private static final Logger logger = LoggerFactory.getLogger(InventoryAlert.class);


    public InventoryAlert(int lowStockThreshold) {
        this.lowStockThreshold = lowStockThreshold;
    }


    public boolean isLowStock(int quantity) {
        return quantity <= lowStockThreshold;
    }


    @Override
    public void update(String ingredient, int quantity) {
        if (isLowStock(quantity)) {
            logger.info("WARNING: Low stock alert for " + ingredient + " - only " + quantity + " remaining!");
        }
        logger.info("Stock update: " + ingredient + " - " + quantity + " units in stock");
    }
}
