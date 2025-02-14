package com.softwaredesign.project.model.placeholders;

import java.util.ArrayList;

public abstract class Recipe {
    private ArrayList  <Ingredient> ingredients;
    public void addIngredient(Ingredient ingredient) {
        ingredients.add(ingredient);
    }

    public void removeIngredient(Ingredient ingredient) {
        ingredients.remove(ingredient);
    }

    public ArrayList<Ingredient> getIngredients() {
        return ingredients;
    }

    
}
