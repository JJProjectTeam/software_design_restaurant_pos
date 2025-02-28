package com.softwaredesign.project.inventory;

import java.util.Dictionary;
import java.util.Hashtable;


public class ConcreteIngredientFactory implements IIngredientFactory {
    private final InventoryService inventoryService;

    public ConcreteIngredientFactory(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @Override
    public Ingredient makeIngredient(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Ingredient name cannot be null or empty");
        }
        
        // Check if the ingredient exists in inventory and has stock
        if (inventoryService.getStock(name) <= 0) {
            throw new IllegalStateException("Ingredient " + name + " is out of stock");
        }
        
        return new Ingredient(name, inventoryService);
    }

    @Override
    public Ingredient[] listIngredients() {
        // We'll create a list of all ingredients that have any stock
        return inventoryService.getIngredientStores().stream()
            .filter(store -> inventoryService.getStock(store.getName()) > 0)
            .map(store -> new Ingredient(store.getName(), inventoryService))
            .toArray(Ingredient[]::new);
    }

    @Override
    public Dictionary<String, Integer> listStock() {
        Dictionary<String, Integer> stock = new Hashtable<>();
        inventoryService.getIngredientStores().forEach(store -> 
            stock.put(store.getName(), inventoryService.getStock(store.getName()))
        );
        return stock;
    }
}
