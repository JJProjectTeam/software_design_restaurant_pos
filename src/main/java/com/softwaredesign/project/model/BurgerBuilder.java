package com.softwaredesign.project.model;
import java.util.List;

public class BurgerBuilder extends Recipe {
    public BurgerBuilder(List<Ingredient> baseIngredients) {
        super(baseIngredients);
        this.name = "Burger";
    }

    public void addTopping(Ingredient topping) {
        addIngredient(topping);
    }

    public void removeTopping(Ingredient topping) {
        removeIngredient(topping);
    }
}
