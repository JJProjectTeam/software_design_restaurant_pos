package com.softwaredesign.project.controller;

import com.softwaredesign.project.inventory.Inventory;
import com.softwaredesign.project.mediator.RestaurantViewMediator;
import com.softwaredesign.project.model.BankBalanceSingleton;
import com.softwaredesign.project.view.InventoryView;
import com.softwaredesign.project.view.View;
import com.softwaredesign.project.view.ViewType;

import java.util.*;

public class InventoryController extends BaseController {
    private Set<String> ingredients;
    private RestaurantViewMediator mediator;
    private Inventory inventory;
    private double bankBalance;
    
    public InventoryController(Inventory inventory) {
        super("Inventory");
        this.inventory = inventory;
        this.mediator = RestaurantViewMediator.getInstance();
        mediator.registerController("Inventory", this);
        this.bankBalance = BankBalanceSingleton.getInstance().getBankBalance();
    }
    
    @Override
    public void updateView() {
        View view = mediator.getView(ViewType.INVENTORY);
        if (!(view instanceof InventoryView)) {
            return;
        }
        view = (InventoryView) view;
        ((InventoryView) view).setBankBalance(BankBalanceSingleton.getInstance().getBankBalance());
        ingredients = inventory.getAllIngredients();
        for (String ingredient : ingredients) {
            int stock = inventory.getStock(ingredient);
            double price = inventory.getPrice(ingredient);
            ((InventoryView) view).onIngredientUpdate(ingredient, stock, price);
        }
    }
    

}
