package com.softwaredesign.project.Order;

import com.softwaredesign.project.inventory.Ingredient;


public class KebabRecipe extends Recipe {
    public KebabRecipe() {
        super("Kebab");
    }

    @Override
    protected void initializeBaseIngredients() {
        addIngredient(new Ingredient("Kebab Meat"));
        addIngredient(new Ingredient("Pita Bread"));
        addIngredient(new Ingredient("Onions"));
        addIngredient(new Ingredient("Tomatoes"));
        addIngredient(new Ingredient("Garlic Sauce"));
    }
}