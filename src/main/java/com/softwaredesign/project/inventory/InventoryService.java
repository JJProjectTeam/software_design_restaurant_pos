package com.softwaredesign.project.inventory;

import java.util.Map;

import com.softwaredesign.project.order.Station;

public interface InventoryService {
    void addIngredient(String name, int quantity, double price, Station... stations);
    void useIngredient(String name, int amount) throws IllegalArgumentException;
    void updateInventory(Map<String, Integer> ingredientUpdates) throws IllegalArgumentException;
    int getStock(String name);
    double getPrice(String name);
    IngredientStore getIngredientStore(String name);
}
