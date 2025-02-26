package com.softwaredesign.project.kitchen;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import com.softwaredesign.project.engine.Entity;
import com.softwaredesign.project.order.Meal;
import com.softwaredesign.project.order.OrderManager;
import com.softwaredesign.project.order.Recipe;
import com.softwaredesign.project.orderfulfillment.CollectionPoint;
import com.softwaredesign.project.staff.Chef;

public class Kitchen extends Entity {
    private OrderManager orderManager;
    private CollectionPoint collectionPoint;
    private StationManager stationManager;
    private List<Recipe> pendingRecipes = new ArrayList<>();
    
    public Kitchen(OrderManager orderManager, CollectionPoint collectionPoint) {
        this.orderManager = orderManager;
        this.collectionPoint = collectionPoint;
        this.stationManager = new StationManager(collectionPoint);
        
        // Register this kitchen with the game engine
        com.softwaredesign.project.engine.GameEngine.getInstance().registerEntity(this);
    }
    
    public void setOrderManager(OrderManager orderManager) {
        this.orderManager = orderManager;
    }
    
    public StationManager getStationManager() {
        return stationManager;
    }

    public void getRecipes() {
        if (orderManager == null) {
            System.out.println("OrderManager is not set");
            return;
        }
        
        List<Recipe> newRecipes = orderManager.processOrder();
        if (newRecipes != null && !newRecipes.isEmpty()) {
            pendingRecipes.addAll(newRecipes);
            System.out.println("Kitchen received " + newRecipes.size() + " new recipes");
        }
    }
    
    public void assignChefToStation(Chef chef, StationType stationType) {
        Station station = stationManager.getStation(stationType);
        if (station != null) {
            station.registerChef(chef);
            chef.assignToStation(stationType);
            System.out.println("Chef assigned to " + stationType + " station");
        }
    }
    
    public void unassignChefFromStation(Chef chef, StationType stationType) {
        Station station = stationManager.getStation(stationType);
        if (station != null && station.getAssignedChef() == chef) {
            station.unregisterChef();
            chef.removeStationAssignment(station);
            System.out.println("Chef unassigned from " + stationType + " station");
        }
    }
    
    private void assignRecipesToStations() {
        if (pendingRecipes.isEmpty()) {
            return;
        }
        
        List<Station> availableStations = new ArrayList<>();
        for (Station station : stationManager.getAllStations()) {
            if (!station.isBusy() && station.hasChef()) {
                availableStations.add(station);
            }
        }
        
        if (availableStations.isEmpty()) {
            return;
        }
        
        List<Recipe> assignedRecipes = new ArrayList<>();
        for (Recipe recipe : pendingRecipes) {
            for (Station station : availableStations) {
                if (!station.isBusy()) {
                    station.assignRecipe(recipe);
                    assignedRecipes.add(recipe);
                    availableStations.remove(station);
                    break;
                }
            }
            
            if (availableStations.isEmpty()) {
                break;
            }
        }
        
        pendingRecipes.removeAll(assignedRecipes);
    }
    
    private void provideIngredientsToStations() {
        for (Station station : stationManager.getAllStations()) {
            if (station.needsIngredients()) {
                station.provideIngredients();
            }
        }
    }

    @Override
    public void readState() {
        // Get new recipes from order manager
        getRecipes();
    }

    @Override
    public void writeState() {
        // Assign recipes to available stations with chefs
        assignRecipesToStations();
        
        // Provide ingredients to stations that need them
        provideIngredientsToStations();
    }
}
