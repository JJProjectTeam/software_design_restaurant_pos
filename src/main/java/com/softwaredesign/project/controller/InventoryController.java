package com.softwaredesign.project.controller;

import com.softwaredesign.project.mediator.RestaurantViewMediator;
import com.softwaredesign.project.view.ConfigurableView;
import com.softwaredesign.project.view.InventoryView;
import com.softwaredesign.project.view.View;
import java.util.*;

public class InventoryController extends BaseController {
    private Map<String, Integer> ingredients;
    private Map<String, Double> prices;
    private RestaurantViewMediator mediator;
    
    public InventoryController() {
        super("Inventory");
        this.ingredients = new HashMap<>();
        this.prices = new HashMap<>();
        this.mediator = RestaurantViewMediator.getInstance();
        mediator.registerController("Inventory", this);
        
        // Initialize with some default ingredients
        initializeDefaultIngredients();
    }
    
    private void initializeDefaultIngredients() {
        // Add some default ingredients with prices
        addIngredient("Beef Patty", 2.50, 20);
        addIngredient("Bun", 0.50, 30);
        addIngredient("Lettuce", 0.25, 40);
        addIngredient("Tomato", 0.30, 25);
        addIngredient("Cheese", 0.75, 30);
        addIngredient("Lamb", 3.00, 15);
        addIngredient("Pita Bread", 0.60, 25);
        addIngredient("Onion", 0.20, 35);
        addIngredient("Tzatziki", 1.00, 20);
    }
    
    public void addIngredient(String name, double price, int quantity) {
        ingredients.put(name, quantity);
        prices.put(name, price);
        notifyIngredientUpdate(name);
    }
    
    public int getIngredientQuantity(String name) {
        return ingredients.getOrDefault(name, 0);
    }
    
    public double getIngredientPrice(String name) {
        return prices.getOrDefault(name, 0.0);
    }
    
    public Map<String, Integer> getIngredients() {
        return new HashMap<>(ingredients);
    }
    
    public void updateIngredientQuantity(String name, int delta) {
        int currentQuantity = ingredients.getOrDefault(name, 0);
        int newQuantity = currentQuantity + delta;
        
        if (newQuantity < 0) {
            System.out.println("[InventoryController] Warning: Ingredient " + name + " quantity would go negative");
            return;
        }
        
        ingredients.put(name, newQuantity);
        System.out.println("[InventoryController] Updated " + name + " quantity to " + newQuantity);
        
        notifyIngredientUpdate(name);
    }
    
    private void notifyIngredientUpdate(String ingredientName) {
        System.out.println("[InventoryController] Notifying views of update for ingredient: " + ingredientName);
        
        int quantity = ingredients.getOrDefault(ingredientName, 0);
        double price = prices.getOrDefault(ingredientName, 0.0);
        
        // Update all registered views
        List<View> views = mediator.getViews("Inventory");
        for (View view : views) {
            if (view instanceof InventoryView) {
                ((InventoryView) view).onInventoryUpdate(
                    ingredientName,
                    price,
                    quantity
                );
            }
        }
    }
    
    @Override
    public void updateView() {
        System.out.println("[InventoryController] Updating all inventory views");
        // Update all ingredients
        for (String ingredient : ingredients.keySet()) {
            notifyIngredientUpdate(ingredient);
        }
    }
    
    /**
     * Refresh all ingredients in the view
     */
    public void refreshAllIngredients() {
        System.out.println("[InventoryController] Refreshing all ingredients");
        updateView();
    }
}
