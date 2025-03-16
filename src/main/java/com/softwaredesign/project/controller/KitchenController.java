package com.softwaredesign.project.controller;

import com.softwaredesign.project.kitchen.Kitchen;
import com.softwaredesign.project.kitchen.Station;
import com.softwaredesign.project.mediator.RestaurantViewMediator;
import com.softwaredesign.project.model.BankBalanceSingleton;
import com.softwaredesign.project.view.KitchenView;
import com.softwaredesign.project.view.View;
import com.softwaredesign.project.view.ViewType;

import java.util.Map;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KitchenController extends BaseController {
    private Kitchen kitchen;
    private RestaurantViewMediator mediator;
    private Map<Station, Integer> stationIdMap;  // Changed from StationType to Station
    private int nextStationId = 1;  // Track next available ID
    private double bankBalance;
    private static final Logger logger = LoggerFactory.getLogger(KitchenController.class);

    public KitchenController(Kitchen kitchen) {
        super("Kitchen");
        this.kitchen = kitchen;
        this.mediator = RestaurantViewMediator.getInstance();
        this.stationIdMap = new HashMap<>();
        this.bankBalance = BankBalanceSingleton.getInstance().getBankBalance();
        
        // Initialize station IDs for each actual station instance
        for (Station station : kitchen.getStationManager().getAllStations()) {
            stationIdMap.put(station, nextStationId++);
            logger.info("[KitchenController] Assigned ID " + (nextStationId-1) + 
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

        // Get updated bank balance every time updateView is called!
        ((KitchenView) view).setBankBalance(BankBalanceSingleton.getInstance().getBankBalance());
        
        // Create a copy of the stations list to avoid ConcurrentModificationException
        java.util.List<Station> stationsCopy = new java.util.ArrayList<>(kitchen.getStationManager().getAllStations());
        
        logger.info("[KitchenController] Updating view for " + stationsCopy.size() + " stations");
            
        for (Station station : stationsCopy) {
            int stationID = stationIdMap.getOrDefault(station, nextStationId++);
            if (!stationIdMap.containsKey(station)) {
                stationIdMap.put(station, stationID);
                logger.info("[KitchenController] Assigned new ID " + stationID + " to " + station.getType() + " station");
            }
            
            String stationName = station.getType().toString();
            int backlog = station.getBacklogSize();
            logger.info("[KitchenController] Station " + stationName + " has backlog size " + backlog);
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
