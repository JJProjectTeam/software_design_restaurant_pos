package com.softwaredesign.project;

import com.softwaredesign.project.inventory.Inventory;
import com.softwaredesign.project.inventory.InventoryAlert;

public class Main {
    public static void main(String[] args) {
        // Create the inventory
        Inventory inventory = new Inventory();

        // Create and attach the alert with threshold of 5
        InventoryAlert alert = new InventoryAlert(5);
        inventory.attach(alert);

        // Add some ingredients
        inventory.addIngredient("Tomatoes", 10, 2.50);
        inventory.addIngredient("Garlic", 8, 1.00);
        inventory.addIngredient("Onions", 15, 1.50);

        // Use ingredients to test stock levels
        System.out.println("\nUsing ingredients...");
        inventory.useIngredient("Tomatoes", 6);  // Should trigger warning (4 left)
        inventory.useIngredient("Garlic", 4);    // Should trigger warning (4 left)
        inventory.useIngredient("Onions", 12);   // Should trigger warning (3 left)

        // Use more ingredients to trigger low stock warning
        System.out.println("\nUsing more ingredients...");
        inventory.useIngredient("Tomatoes", 4);  // Should trigger warning
        inventory.useIngredient("Garlic", 2);    // Should trigger warning
    }
}
