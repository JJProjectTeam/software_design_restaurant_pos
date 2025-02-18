package com.softwaredesign.project.inventory;

import java.util.Set;

import com.softwaredesign.project.order.StationType;

public class Ingredient {
    private String name;
    private final InventoryService inventory ;

    public Ingredient(String name, InventoryService inventory) {
        this.name = name;
        this.inventory = inventory;
        // Reduce the quantity in the IngredientStore by 1 when an Ingredient is created
        inventory.useIngredient(name, 1);
    }

    public String getName() {
        return name;
    }

    public Set<StationType> getStationTypes() {
        return inventory.getIngredientStore(name).getStationTypes();
    }

    @Override
    public String toString() {
        return name;
    }
}
