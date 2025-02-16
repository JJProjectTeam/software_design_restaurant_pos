package com.softwaredesign.project.extras;
import java.util.List;

import com.softwaredesign.project.inventory.Ingredient;
import com.softwaredesign.project.Order.Meal;

public abstract class Recipe {
    protected String name;
    protected List<Ingredient> ingredients;

    public Recipe(List<Ingredient> baseIngredients) {
        this.ingredients = baseIngredients;
    }

    public void addIngredient(Ingredient ingredient) {
        ingredients.add(ingredient);
    }

    public void removeIngredient(Ingredient ingredient) {
        ingredients.remove(ingredient);
    }

    public List<Ingredient> getIngredients() {
        return ingredients;
    }
    
    public Meal buildMeal() {
        return new Meal(name, ingredients);
    }
}
