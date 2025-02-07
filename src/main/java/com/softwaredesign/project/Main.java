package com.softwaredesign.project;

import com.softwaredesign.project.controller.InventoryController;
import com.softwaredesign.project.model.inventory.Inventory;
import com.softwaredesign.project.views.InventoryAlertView;
import com.softwaredesign.project.model.inventory.InventoryAlert;

public class Main {
    public static void main(String[] args) {
        // Create the inventory (Model)
        Inventory inventory = new Inventory();

        // Create the view first
        InventoryAlertView alertView = new InventoryAlertView();
        // Create the alert model with the view
        InventoryAlert alert = new InventoryAlert(5, alertView);  // Create alert with threshold of 5
        inventory.attach(alert);

        InventoryController controller = new InventoryController(inventory);

        // Add some ingredients
        controller.addIngredient("Tomatoes", 10, 2.50);
        controller.addIngredient("Garlic", 8, 1.00);
        controller.addIngredient("Onions", 15, 1.50);

        // Use ingredients to test stock levels
        System.out.println("\nUsing ingredients...");
        controller.useIngredient("Tomatoes", 6);  // Should trigger warning (4 left)
        controller.useIngredient("Garlic", 4);    // Should trigger warning (4 left)
        controller.useIngredient("Onions", 12);   // Should trigger warning (3 left)

        // Use more ingredients to trigger low stock warning
        System.out.println("\nUsing more ingredients...");
        controller.useIngredient("Tomatoes", 4);  // Should trigger warning
        controller.useIngredient("Garlic", 2);    // Should trigger warning
    }
}
