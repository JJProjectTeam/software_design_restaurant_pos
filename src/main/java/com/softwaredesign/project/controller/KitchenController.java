package com.softwaredesign.project.controller;

import com.softwaredesign.project.kitchen.Kitchen;
import com.softwaredesign.project.kitchen.StationType;
import com.softwaredesign.project.kitchen.Station;
import com.softwaredesign.project.mediator.RestaurantViewMediator;
import com.softwaredesign.project.view.KitchenView;
import com.softwaredesign.project.view.View;
import com.softwaredesign.project.view.ViewType;

import java.util.List;
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
        view = (KitchenView) view;
        System.out.println("[KitchenController] Updating view for " + 
            kitchen.getStationManager().getAllStations().size() + " stations");
            
        for (Station station : kitchen.getStationManager().getAllStations()) {
            int stationID = stationIdMap.getOrDefault(station, nextStationId++);
            if (!stationIdMap.containsKey(station)) {
                stationIdMap.put(station, stationID);
                System.out.println("[KitchenController] Assigned new ID " + stationID + 
                    " to " + station.getType() + " station");
            }
            
            String stationName = station.getType().toString();
            int backlog = station.getBacklogSize();
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
