package com.softwaredesign.project.inventory;

import java.util.Map;
import java.util.Set;

import com.softwaredesign.project.kitchen.StationType;

public interface InventoryService {
    void addIngredient(String name, int quantity, double price, StationType... stationTypes);
    void useIngredient(String name, int amount) throws IllegalArgumentException;
    void updateInventory(Map<String, Integer> ingredientUpdates) throws IllegalArgumentException;
    int getStock(String name);
    double getPrice(String name);
    IngredientStore getIngredientStore(String name);
    Set<IngredientStore> getIngredientStores();
}
