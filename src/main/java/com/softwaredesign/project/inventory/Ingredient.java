package com.softwaredesign.project.inventory;

import com.softwaredesign.project.Order.Station;
import java.util.Collections;
import java.util.Set;

public class Ingredient {
    private String name;

    public Ingredient(String name) {
        this.name = name;
        // Reduce the quantity in the IngredientStore by 1 when an Ingredient is created
        Inventory.getInstance().useIngredient(name, 1);
    }

    public String getName() {
        return name;
    }

    // This is so so dodge TODO find a better solution for this
    public Set<Station> getStations() {
        return Inventory.getInstance().getIngredientStore(name).getStations();
    }

    @Override
    public String toString() {
        return name;
    }
}
