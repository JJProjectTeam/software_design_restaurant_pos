package com.softwaredesign.project.model;
import java.util.List;

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
