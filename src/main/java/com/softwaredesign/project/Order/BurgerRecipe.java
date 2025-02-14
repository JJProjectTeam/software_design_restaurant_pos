package com.softwaredesign.project.Order;

import com.softwaredesign.project.extras.Ingredient;

public class BurgerRecipe extends Recipe {
    public BurgerRecipe() {
        super("Burger");
    }

    @Override
    protected void initializeBaseIngredients() {
        addIngredient(new Ingredient("Beef Patty", 3.99, Station.PREP, Station.GRILL));
        addIngredient(new Ingredient("Bun", 0.99, Station.PREP));
        addIngredient(new Ingredient("Lettuce", 0.99, Station.PREP));
        addIngredient(new Ingredient("Tomato", 0.50, Station.PREP));
        addIngredient(new Ingredient("Cheese", 0.75));
    }
}