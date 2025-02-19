package com.softwaredesign.project.order;

import java.util.ArrayList;
import java.util.List;

import com.softwaredesign.project.inventory.Ingredient;

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