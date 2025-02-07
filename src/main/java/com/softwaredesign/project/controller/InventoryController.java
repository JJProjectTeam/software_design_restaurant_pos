package com.softwaredesign.project.controller;

import com.softwaredesign.project.model.inventory.Inventory;

public class InventoryController {
    private final Inventory inventory;

    public InventoryController(Inventory inventory) {
        this.inventory = inventory;
    }

    public void addIngredient(String name, int quantity, double price) {
        inventory.addIngredient(name, quantity, price);
    }

    public void useIngredient(String name, int amount) {
        inventory.useIngredient(name, amount);
    }

    public int getStock(String name) {
        return inventory.getStock(name);
    }

    public double getPrice(String name) {
        return inventory.getPrice(name);
    }
}
