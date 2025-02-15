package com.softwaredesign.project.model.placeholders;

import java.util.ArrayList;
import java.util.List;

public class RecipeModification {
    private List<Ingredient> addedIngredients;
    private List<Ingredient> removedIngredients;

    public RecipeModification() {
        this.addedIngredients = new ArrayList<>();
        this.removedIngredients = new ArrayList<>();
    }

    public void addIngredient(Ingredient ingredient) {
        addedIngredients.add(ingredient);
    }

    public void removeIngredient(Ingredient ingredient) {
        removedIngredients.add(ingredient);
    }

    public List<Ingredient> getAddedIngredients() {
        return addedIngredients;
    }

    public List<Ingredient> getRemovedIngredients() {
        return removedIngredients;
    }
}
