package com.softwaredesign.project.order;

import com.softwaredesign.project.inventory.Ingredient;
import com.softwaredesign.project.inventory.InventoryService;

public class BurgerRecipe extends Recipe {
    public BurgerRecipe(InventoryService inventoryService) {
        super("Burger", inventoryService);

    }

    @Override
    protected void initializeBaseIngredients() {
        addIngredient(new Ingredient("Beef Patty", inventoryService));
        addIngredient(new Ingredient("Bun", inventoryService));
        addIngredient(new Ingredient("Lettuce", inventoryService));
        addIngredient(new Ingredient("Tomato", inventoryService));
        addIngredient(new Ingredient("Cheese", inventoryService));
    }
}