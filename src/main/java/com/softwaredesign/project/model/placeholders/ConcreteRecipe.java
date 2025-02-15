package com.softwaredesign.project.model.placeholders;

public class ConcreteRecipe extends Recipe {

    public ConcreteRecipe() {
        super("Sample Recipe");  // Provide a name for the recipe
    }

    @Override
    protected void initializeBaseIngredients() {
        // Add some sample ingredients
        addIngredient(new Ingredient("Base Ingredient 1"));
        addIngredient(new Ingredient("Base Ingredient 2"));
    }
    
    @Override
    public String toString() {
        return "Concrete Recipe";
    }
}
