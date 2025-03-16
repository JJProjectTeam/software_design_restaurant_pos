package com.softwaredesign.project.order;

import java.util.List;

import com.softwaredesign.project.inventory.Ingredient;
import com.softwaredesign.project.inventory.InventoryService;

public class Meal {
    private String name;
    private final List<Ingredient> ingredients;
    private final InventoryService inventory;
    private String orderId;

    public Meal(String name, List<Ingredient> ingredients, InventoryService inventory, String orderId) {
        this.name = name;
        this.ingredients = ingredients;
        this.inventory = inventory;
        this.orderId = orderId;
        useIngredients();
    }

    public void useIngredients() {
        for (Ingredient ingredient : ingredients) {
            ingredient.useIngredient();
        }
    }

    public String getOrderId() {
        return orderId;
    }

    public double getPrice() {
        double price = 0;
        for (Ingredient ingredient : ingredients) {
            // Retrieve the price from the IngredientStore
            double ingredientPrice = inventory.getIngredientStore(ingredient.getName()).getPrice();
            price += ingredientPrice;
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

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public List<Ingredient> getIngredients() {
        return ingredients;
    }
}
