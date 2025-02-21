package com.softwaredesign.project.controller;

import com.softwaredesign.project.inventory.Inventory;
import com.softwaredesign.project.kitchen.StationType;
import com.softwaredesign.project.view.GeneralView;
import com.softwaredesign.project.view.InventoryView;

public class InventoryController extends BaseController {
    private Inventory inventory;

    public InventoryController(Inventory inventory) {
        super("Inventory");
        System.out.println("[InventoryController] Initializing controller...");
        this.inventory = inventory;
    }

    @Override
    public void updateView() {
        System.out.println("[InventoryController] Updating all inventory views");
        inventory.getAllIngredients().forEach(this::updateIngredientView);
    }

    public void addIngredient(String name, int quantity, double price, StationType station) {
        System.out.println("[InventoryController] Adding ingredient " + name);
        inventory.addIngredient(name, quantity, price, station);
        updateIngredientView(name);
    }

    public void updateIngredientQuantity(String name, int newQuantity) {
        System.out.println("[InventoryController] Updating quantity for " + name + " to " + newQuantity);
        inventory.updateQuantity(name, newQuantity);
        updateIngredientView(name);
    }

    private void updateIngredientView(String ingredientName) {
        double price = inventory.getPrice(ingredientName);
        int quantity = inventory.getStock(ingredientName);

        System.out.println("[InventoryController] Updating view for ingredient " + ingredientName + 
                         " (price: " + price + 
                         ", quantity: " + quantity + ")");

        // Notify all registered views
        for (GeneralView view : mediator.getViews("Inventory")) {
            if (view instanceof InventoryView) {
                ((InventoryView) view).onInventoryUpdate(ingredientName, price, quantity);
            }
            else {
                System.out.println("[InventoryController] View is not an InventoryView, skipping update");
            }
        }
    }

    public Inventory getInventory() {
        return inventory;
    }
}
