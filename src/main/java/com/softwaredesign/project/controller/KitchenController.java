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
    private Map<StationType, Integer> stationIdMap;

    public KitchenController(Kitchen kitchen) {
        super("Kitchen");
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
        View view = mediator.getView(ViewType.KITCHEN);
        view = (KitchenView) view;

        for (Station station : kitchen.getStationManager().getAllStations()) {
            int stationID = stationIdMap.get(station.getType());
            String stationName = station.getType().toString();
            int backlog = station.getBacklogSize();
            String chefName = station.hasChef()? station.getAssignedChef().getName(): "";
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
