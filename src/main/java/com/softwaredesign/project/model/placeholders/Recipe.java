package com.softwaredesign.project.model.placeholders;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public abstract class Recipe {
    protected String name;
    protected ArrayList<Ingredient> ingredients;
    protected Queue<Station> stationsToVisit;

    protected Recipe(String name) {
        this.name = name;
        this.ingredients = new ArrayList<>();  // Initialize the list
        this.stationsToVisit = new LinkedList<>();
    }

    protected abstract void initializeBaseIngredients();
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
