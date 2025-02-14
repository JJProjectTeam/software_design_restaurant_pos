package com.Order;

import com.softwaredesign.project.model.Ingredient;

public class KebabRecipe extends Recipe {
    public KebabRecipe() {
        super("Kebab");
    }

    @Override
    protected void initializeBaseIngredients() {
        addIngredient(new Ingredient("Kebab Meat", 4.99, Station.PREP, Station.GRILL));
        addIngredient(new Ingredient("Pita Bread", 1.99, Station.PREP));
        addIngredient(new Ingredient("Onions", 0.50, Station.PREP));
        addIngredient(new Ingredient("Tomatoes", 0.50, Station.PREP));
        addIngredient(new Ingredient("Garlic Sauce", 0.50));
    }
}