package com.softwaredesign.project.order;

import com.softwaredesign.project.inventory.Ingredient;
import com.softwaredesign.project.inventory.InventoryService;

public class BurgerRecipe extends Recipe {
    public BurgerRecipe(InventoryService inventoryService) {
        super("Burger", inventoryService);
    }

    @Override
    protected void initializeBaseIngredients() {
        // Make sure we use the inventoryService passed from the constructor
        ingredients.add(new Ingredient("Beef Patty", inventoryService));
        ingredients.add(new Ingredient("Bun", inventoryService));
        ingredients.add(new Ingredient("Lettuce", inventoryService));
        ingredients.add(new Ingredient("Tomato", inventoryService));
        ingredients.add(new Ingredient("Cheese", inventoryService));
    }
}