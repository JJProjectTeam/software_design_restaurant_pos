package com.softwaredesign.project.controller;

import com.softwaredesign.project.inventory.Inventory;
import com.softwaredesign.project.inventory.InventoryService;
import com.softwaredesign.project.mediator.RestaurantViewMediator;
import com.softwaredesign.project.view.ConfigurableView;
import com.softwaredesign.project.view.InventoryView;
import com.softwaredesign.project.view.View;
import com.softwaredesign.project.view.ViewType;

import java.util.*;

public class InventoryController extends BaseController {
    private Set<String> ingredients;
    private Map<String, Integer> stockLevels;
    private Map<String, Double> prices;
    private RestaurantViewMediator mediator;
    private Inventory inventory;
    
    public InventoryController(Inventory inventory) {
        super("Inventory");
        this.inventory = inventory;
        this.stockLevels = new HashMap<>();
        this.prices = new HashMap<>();
        this.mediator = RestaurantViewMediator.getInstance();
        mediator.registerController("Inventory", this);
    }
    
    @Override
    public void updateView() {
        ingredients = inventory.getAllIngredients();
        for (String ingredient : ingredients) {
            stockLevels.put(ingredient, inventory.getStock(ingredient));
            prices.put(ingredient, inventory.getPrice(ingredient));
        }
        View view = mediator.getView(ViewType.INVENTORY);
        view = (InventoryView) view;
        ((InventoryView) view).onInventoryUpdate(ingredients, stockLevels, prices);
    }
    

}
