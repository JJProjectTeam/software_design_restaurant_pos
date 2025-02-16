package com.softwaredesign.project.inventory;

import com.softwaredesign.project.Order.Station;
import java.util.Map;

public interface InventoryService {
    void addIngredient(String name, int quantity, double price, Station... stations);
    void useIngredient(String name, int amount) throws IllegalArgumentException;
    void updateInventory(Map<String, Integer> ingredientUpdates) throws IllegalArgumentException;
    int getStock(String name);
    double getPrice(String name);
    IngredientStore getIngredientStore(String name);
}
