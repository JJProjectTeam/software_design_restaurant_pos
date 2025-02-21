package com.softwaredesign.project.controller;

import com.softwaredesign.project.kitchen.Kitchen;
import com.softwaredesign.project.kitchen.StationType;
import com.softwaredesign.project.view.GeneralView;
import com.softwaredesign.project.view.KitchenView;

public class KitchenController extends BaseController {
    private Kitchen kitchen;

    public KitchenController(Kitchen kitchen) {
        super("Kitchen");
        System.out.println("[KitchenController] Initializing controller...");
        this.kitchen = kitchen;
    }

    @Override
    public void updateView() {
        System.out.println("[KitchenController] Updating all station views");
        for (StationType station : StationType.values()) {
            updateRow(station);
        }
    }

    private void updateRow(StationType station) {
        // Get station data from model
        // int backlog = kitchen.getBacklog(station);
        // String assignedChef = kitchen.getAssignedChef(station);
        // boolean isInUse = kitchen.isStationInUse(station);

        // Convert data for view
        String stationName = station.toString();
        // char inUseIndicator = isInUse ? '*' : ' ';

        // System.out.println("[KitchenController] Updating station " + stationName + 
        //                  " (backlog: " + backlog + 
        //                  ", chef: " + assignedChef + 
        //                  ", inUse: " + inUseIndicator + ")");

        // Update all registered views
        // for (GeneralView view : mediator.getViews("Kitchen")) {
        //     if (view instanceof KitchenView) {
        //         ((KitchenView) view).onStationUpdate(
        //             stationName,
        //             backlog,
        //             assignedChef,
        //             inUseIndicator
        //         );
        //     }
        //     else {
        //         System.out.println("[KitchenController] View is not a KitchenView, skipping update");
        //     }
        // }
    }

    public Kitchen getKitchen() {
        return kitchen;
    }
}
