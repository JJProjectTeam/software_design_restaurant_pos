package com.softwaredesign.project.model;

import java.util.List;

public class PizzaBuilder extends Recipe {
    public PizzaBuilder(List<Ingredient> baseIngredients) {
        super(baseIngredients);
        this.name = "Pizza";
    }
}
