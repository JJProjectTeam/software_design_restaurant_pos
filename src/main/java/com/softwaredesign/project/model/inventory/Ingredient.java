package com.softwaredesign.project.model.inventory;

import java.util.Set;

import com.softwaredesign.project.model.kitchen.StationType;

public class Ingredient {
    private String name;
    private final InventoryService inventory ;

    public Ingredient(String name, InventoryService inventory) {
        this.name = name;
        this.inventory = inventory;
    }
    
    public String getName() {
        return name;
    }
    
    // Reduce the quantity in the IngredientStore by 1 when an Ingredient is created
    public void useIngredient() {
        inventory.useIngredient(name, 1);
    }

    public Set<StationType> getStationTypes() {
        return inventory.getIngredientStore(name).getStationTypes();
    }

    @Override
    public String toString() {
        return name;
    }

    public double getPrice() {
        return inventory.getIngredientStore(name).getPrice();
    }
}
