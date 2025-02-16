package com.softwaredesign.project.Order;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;

import com.softwaredesign.project.inventory.Ingredient;

public abstract class Recipe {
    protected String name;
    protected List<Ingredient> ingredients;
    protected Queue<Station> stationsToVisit;

    protected Recipe(String name) {
        this.name = name;
        this.ingredients = new ArrayList<>();
        initializeBaseIngredients();
        stationsToVisit = new LinkedList<>();
    }

    protected abstract void initializeBaseIngredients();

    public void addIngredient(Ingredient ingredient) {
        ingredients.add(ingredient);
    }

    public void removeIngredient(Ingredient ingredient) {
        ingredients.remove(ingredient);
    }

    public List<Ingredient> getIngredients() {
        return ingredients;
    }

    public String getName() {
        return name;
    }

    public Queue<Station> getStationsToVisit() {
        return stationsToVisit;
    }

    public void addStation(Station station) {
        stationsToVisit.add(station);
    }   

    public void removeStation(Station station) {
        stationsToVisit.remove(station);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Recipe recipe = (Recipe) o;
        return name.equals(recipe.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    public Meal buildMeal() {
        return new Meal(name, ingredients);
    }
}