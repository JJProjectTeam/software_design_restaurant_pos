package com.softwaredesign.project.controller;

import com.softwaredesign.project.kitchen.Kitchen;
import com.softwaredesign.project.kitchen.StationType;
import com.softwaredesign.project.kitchen.Station;
import com.softwaredesign.project.mediator.RestaurantViewMediator;
import com.softwaredesign.project.view.KitchenView;
import com.softwaredesign.project.view.View;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class KitchenController extends BaseController {
    private Kitchen kitchen;
    private RestaurantViewMediator mediator;
    private Map<StationType, Integer> stationIdMap;

    public KitchenController(Kitchen kitchen) {
        super("Kitchen");
        System.out.println("[KitchenController] Initializing controller...");
        this.kitchen = kitchen;
        this.mediator = RestaurantViewMediator.getInstance();
        this.stationIdMap = new HashMap<>();
        
        // Initialize station IDs
        int id = 1;
        for (StationType type : StationType.values()) {
            stationIdMap.put(type, id++);
        }
        
        // Register with mediator
        mediator.registerController("Kitchen", this);
    }

    @Override
    public void updateView() {
        System.out.println("[KitchenController] Updating all station views");
        for (StationType stationType : StationType.values()) {
            updateStationView(stationType);
        }
    }

    private void updateStationView(StationType stationType) {
        // Get station data from model
        List<Station> stations = kitchen.getStationManager().getStationsByType(stationType);
        if (stations.isEmpty()) {
            System.out.println("[KitchenController] No stations found for type: " + stationType);
            return;
        }
        
        // For simplicity, we'll just use the first station of each type
        Station station = stations.get(0);
        
        // Get station data
        int stationId = stationIdMap.get(stationType);
        String stationName = stationType.toString();
        int backlog = station.getBacklogSize();
        String chefName = station.hasChef() ? station.getAssignedChef().getName() : "None";
        char inUse = station.isBusy() ? '*' : ' ';
        
        System.out.println("[KitchenController] Updating station " + stationName + 
                         " (ID: " + stationId + 
                         ", backlog: " + backlog + 
                         ", chef: " + chefName + 
                         ", inUse: " + inUse + ")");
        
        // Update all registered views
        List<View> views = mediator.getViews("Kitchen");
        for (View view : views) {
            if (view instanceof KitchenView) {
                ((KitchenView) view).onStationUpdate(
                    stationId,
                    stationName,
                    backlog,
                    chefName,
                    inUse
                );
            } else {
                System.out.println("[KitchenController] View is not a KitchenView, skipping update");
            }
        }
    }
    
    /**
     * Notify views about a specific station update
     */
    public void notifyStationUpdate(StationType stationType) {
        System.out.println("[KitchenController] Notifying views of update for station: " + stationType);
        updateStationView(stationType);
    }
    
    /**
     * Refresh all stations in the view
     */
    public void refreshAllStations() {
        System.out.println("[KitchenController] Refreshing all stations");
        updateView();
    }

    public Kitchen getKitchen() {
        return kitchen;
    }
}
