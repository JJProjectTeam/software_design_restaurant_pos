package com.softwaredesign.project;

import com.softwaredesign.project.Order.Station;
import com.softwaredesign.project.inventory.Ingredient;
import com.softwaredesign.project.inventory.Inventory;
import com.softwaredesign.project.inventory.InventoryAlert;

public class Main {
    public static void main(String[] args) {
        // Get the inventory instance
        Inventory inventory = Inventory.getInstance();

        // Create and attach the alert with threshold of 5
        InventoryAlert alert = new InventoryAlert(5);
        inventory.attach(alert);

        // Add some ingredients to inventory
        inventory.addIngredient("Tomatoes", 10, 2.50);
        inventory.addIngredient("Garlic", 8, 1.00);
        inventory.addIngredient("Onions", 15, 1.50);

        System.out.println("\nCreating ingredients (this will reduce inventory)...");
        // Create ingredients - this will automatically reduce inventory
        Ingredient tomatoes = new Ingredient("Tomatoes");  // Reduces by 1
        Ingredient garlic = new Ingredient("Garlic");      // Reduces by 1
        Ingredient onions = new Ingredient("Onions");      // Reduces by 1

        System.out.println("\nUsing more ingredients directly from inventory...");
        inventory.useIngredient("Tomatoes", 6);  // Should trigger warning (3 left)
        inventory.useIngredient("Garlic", 4);    // Should trigger warning (3 left)
        inventory.useIngredient("Onions", 12);   // Should trigger warning (2 left)
    }
}
