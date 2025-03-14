package com.softwaredesign.project.controller;

import com.softwaredesign.project.kitchen.Kitchen;
import com.softwaredesign.project.kitchen.Station;
import com.softwaredesign.project.mediator.RestaurantViewMediator;
import com.softwaredesign.project.view.KitchenView;
import com.softwaredesign.project.view.View;
import com.softwaredesign.project.view.ViewType;

import java.util.Map;
import java.util.HashMap;

public class KitchenController extends BaseController {
    private Kitchen kitchen;
    private RestaurantViewMediator mediator;
    private Map<Station, Integer> stationIdMap;  // Changed from StationType to Station
    private int nextStationId = 1;  // Track next available ID

    public KitchenController(Kitchen kitchen) {
        super("Kitchen");
        this.kitchen = kitchen;
        this.mediator = RestaurantViewMediator.getInstance();
        this.stationIdMap = new HashMap<>();
        
        // Initialize station IDs for each actual station instance
        for (Station station : kitchen.getStationManager().getAllStations()) {
            stationIdMap.put(station, nextStationId++);
            System.out.println("[KitchenController] Assigned ID " + (nextStationId-1) + 
                " to " + station.getType() + " station");
        }
        
        mediator.registerController("Kitchen", this);
    }

    @Override
    public void updateView() {

        View view = mediator.getView(ViewType.KITCHEN);
        if (!(view instanceof KitchenView)) {
            return;
        }
        view = (KitchenView) view;
        
        // Create a copy of the stations list to avoid ConcurrentModificationException
        java.util.List<Station> stationsCopy = new java.util.ArrayList<>(kitchen.getStationManager().getAllStations());
        
        System.out.println("[KitchenController] Updating view for " + stationsCopy.size() + " stations");
            
        for (Station station : stationsCopy) {
            int stationID = stationIdMap.getOrDefault(station, nextStationId++);
            if (!stationIdMap.containsKey(station)) {
                stationIdMap.put(station, stationID);
                System.out.println("[KitchenController] Assigned new ID " + stationID + " to " + station.getType() + " station");
            }
            
            String stationName = station.getType().toString();
            int backlog = station.getBacklogSize();
            System.out.println("[KitchenController] Station " + stationName + " has backlog size " + backlog);
            String chefName = station.hasChef() ? station.getAssignedChef().getName() : "";
            char inUse = station.hasChef() ? 'X' : ' ';
            
            ((KitchenView) view).onStationUpdate(
                stationID,
                stationName,
                backlog,
                chefName,
                inUse
            );        
        }
    }

    public Kitchen getKitchen() {
        return kitchen;
    }
}
