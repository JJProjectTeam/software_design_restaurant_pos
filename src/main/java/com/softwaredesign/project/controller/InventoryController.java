package com.softwaredesign.project.controller;

import com.softwaredesign.project.mediator.RestaurantViewMediator;
import com.softwaredesign.project.view.ConfigurableView;
import com.softwaredesign.project.view.View;
import java.util.*;

public class InventoryController extends BaseController {
    private Map<String, Integer> ingredients;
    
    public InventoryController() {
        super("Inventory");
        this.ingredients = new HashMap<>();
        mediator.registerController("Inventory", this);
    }
    
    public void addIngredient(String name, int quantity) {
        ingredients.put(name, quantity);
        updateView();
    }
    
    public int getIngredientQuantity(String name) {
        return ingredients.getOrDefault(name, 0);
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
        
        // Notify inventory views
        for (View view : mediator.getViews("Inventory")) {
            if (view instanceof ConfigurableView) {
                ((ConfigurableView) view).onUpdate(this);
            }
        }
    }
    
    @Override
    public void updateView() {
        mediator.notifyViewUpdate("Inventory");
    }
}
