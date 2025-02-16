package com.softwaredesign.project.Order;

import com.softwaredesign.project.inventory.Ingredient;
import com.softwaredesign.project.inventory.InventoryService;

public class KebabRecipe extends Recipe {
    public KebabRecipe(InventoryService inventoryService) {
        super("Kebab", inventoryService);
    }

    @Override
    protected void initializeBaseIngredients() {
        addIngredient(new Ingredient("Kebab Meat", inventoryService));
        addIngredient(new Ingredient("Pita Bread", inventoryService));
        addIngredient(new Ingredient("Onions", inventoryService));
        addIngredient(new Ingredient("Tomatoes", inventoryService));
        addIngredient(new Ingredient("Garlic Sauce", inventoryService));
    }
}