package com.softwaredesign.project.Order;
import java.util.List;

import com.softwaredesign.project.extras.Ingredient;
public class Meal {
    private String name;
    private final List<Ingredient> ingredients;

    public Meal(String name, List<Ingredient> ingredients) {
        this.name = name;
        this.ingredients = ingredients;
    }

    public double getPrice() {
        double price = 0;
        for (Ingredient ingredient : ingredients) {
            price += ingredient.getPrice();
        }
        return price;
    }


    @Override
    public String toString() {
        StringBuilder result = new StringBuilder(name + " with ");
        for (int i = 0; i < ingredients.size(); i++) {
            if (i == ingredients.size() - 1) {
                result.append(ingredients.get(i));
            } else if (i == ingredients.size() - 2) {
                result.append(ingredients.get(i)).append(" and ");
            } else {
                result.append(ingredients.get(i)).append(", ");
            }
        }

        result.append(" Price: ").append(getPrice());
        return result.toString();
    }
}

