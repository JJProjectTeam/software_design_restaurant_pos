package com.softwaredesign.project.Order;

import com.softwaredesign.project.inventory.Ingredient;

public class BurgerRecipe extends Recipe {
    public BurgerRecipe() {
        super("Burger");
    }

    @Override
    protected void initializeBaseIngredients() {
        addIngredient(new Ingredient("Beef Patty"));
        addIngredient(new Ingredient("Bun"));
        addIngredient(new Ingredient("Lettuce"));
        addIngredient(new Ingredient("Tomato"));
        addIngredient(new Ingredient("Cheese"));
    }
}