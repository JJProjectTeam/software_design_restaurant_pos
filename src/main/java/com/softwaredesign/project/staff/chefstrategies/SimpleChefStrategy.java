package com.softwaredesign.project.staff.chefstrategies;

import java.util.List;

import com.softwaredesign.project.kitchen.Station;
import com.softwaredesign.project.order.Order;

/**
 * A simple chef strategy that chooses the first available station.
 */
public class SimpleChefStrategy implements ChefStrategy {
    @Override
    public Station chooseNextStation(List<Station> assignedStations) {
        if (assignedStations == null || assignedStations.isEmpty()) {
            return null;
        }
        
        // First, look for a station that needs a chef and has a recipe assigned
        for (Station station : assignedStations) {
            if (station.getCurrentRecipe() != null) {
                return station;
            }
        }
        
        // If no station has a recipe, just return the first one
        return assignedStations.get(0);
    }
    
    @Override
    public Order getNextOrder(Station station) {
        if (station == null || station.getBacklog().isEmpty()) {
            return null;
        }
        
        // Simply get the first order in the backlog
        return station.getBacklog().get(0);
    }
}
