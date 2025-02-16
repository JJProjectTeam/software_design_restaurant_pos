package com.softwaredesign.project.inventory;

import com.softwaredesign.project.Order.Station;
import java.util.Set;

public class Ingredient {
    private String name;
    private final InventoryService inventory;

    public Ingredient(String name, InventoryService inventory) {
        this.name = name;
        this.inventory = inventory;
        // Reduce the quantity in the IngredientStore by 1 when an Ingredient is created
        inventory.useIngredient(name, 1);
    }

    public String getName() {
        return name;
    }

    // This is so so dodge TODO find a better solution for this
    public Set<Station> getStations() {
        return inventory.getIngredientStore(name).getStations();
    }

    @Override
    public String toString() {
        return name;
    }
}
