package com.softwaredesign.project.model;
import java.util.List;
public class Meal {
    private String name;
    private final List<Ingredient> ingredients;

    public Meal(String name, List<Ingredient> ingredients) {
        this.name = name;
        this.ingredients = ingredients;
    }


    @Override
    public String toString() {
        return name + "with" + ingredients;
    }
}

