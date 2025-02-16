package com.softwaredesign.project.kitchen;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import com.softwaredesign.project.inventory.InventoryService;
import com.softwaredesign.project.order.Meal;
import com.softwaredesign.project.order.OrderManager;
import com.softwaredesign.project.order.Recipe;
import com.softwaredesign.project.order.Station;

public class Kitchen {
    private OrderManager orderManager;
    private InventoryService inventoryService;
    private List<Recipe> recipes = new ArrayList<>();

    public Kitchen(OrderManager orderManager, InventoryService inventoryService) {
        this.orderManager = orderManager;
        this.inventoryService = inventoryService;
    }

    public void getRecipes() {
        this.recipes = orderManager.processOrder();
    }

    public List<Meal> prepareRecipes() {
        getRecipes();

        if (recipes.isEmpty()) {
            System.out.println("No recipes to prepare");
            return null;
        }

        List<Meal> meals = new ArrayList<>();
        for (Recipe recipe : recipes) {
            System.out.println("\nPreparing " + recipe.getName());
            System.out.println("Required stations in order:");

            Queue<Station> stations = recipe.getStationsToVisit();
            if (stations.isEmpty()) {
                System.out.println("No stations required - recipe ready for building");
            }

            while (!stations.isEmpty()) {
                Station currentStation = stations.poll();
                System.out.println("- Processing at: " + currentStation);
            }

            // After all stations are processed, build the meal
            System.out.println("All stations completed - building meal");
            Meal meal = recipe.buildMeal();
            meals.add(meal);
        }

        return meals;
    }
}
