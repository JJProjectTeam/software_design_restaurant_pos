package com.softwaredesign.project.kitchen;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import com.softwaredesign.project.inventory.InventoryService;
import com.softwaredesign.project.order.Meal;
import com.softwaredesign.project.order.OrderManager;
import com.softwaredesign.project.order.Recipe;
import com.softwaredesign.project.order.Station;
import com.softwaredesign.project.order.StationType;
import com.softwaredesign.project.order.CollectionPoint;

public class Kitchen {
    private OrderManager orderManager;
    private InventoryService inventoryService;
    private CollectionPoint collectionPoint;
    private List<Recipe> recipes = new ArrayList<>();
    private Map<StationType, Station> stations;

    public Kitchen(OrderManager orderManager, InventoryService inventoryService, CollectionPoint collectionPoint) {
        this.orderManager = orderManager;
        this.inventoryService = inventoryService;
        this.collectionPoint = collectionPoint;
        initializeStations();
    }

    private void initializeStations() {
        // Initialize all required stations
        Station.getInstance(StationType.PREP);
        Station.getInstance(StationType.GRILL);
        Station.getInstance(StationType.PLATE);
    }

    public void getRecipes() {
        this.recipes = orderManager.processOrder();
    }

    public void prepareRecipes() {
        getRecipes();

        if (recipes.isEmpty()) {
            System.out.println("No recipes to prepare");
            return;
        }

        for (Recipe recipe : recipes) {
            System.out.println("\nPreparing " + recipe.getName());

            // Process through stations
            Queue<Station> stations = recipe.getStationsToVisit();
            while (!stations.isEmpty()) {
                Station currentStation = stations.poll();
                System.out.println("- Processing at: " + currentStation);
            }

            // Build and send to collection point
            Meal meal = recipe.buildMeal();
            collectionPoint.addCompletedMeal(meal);
        }
    }
}
