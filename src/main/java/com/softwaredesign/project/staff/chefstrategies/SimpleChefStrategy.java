package com.softwaredesign.project.staff.chefstrategies;

import java.util.List;

import com.softwaredesign.project.kitchen.Station;
import com.softwaredesign.project.order.Order;
import com.softwaredesign.project.order.Recipe;
import com.softwaredesign.project.order.RecipeTask;

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
            // Check if the station has a recipe, is not busy, and has no chef assigned yet
            if (station.getCurrentRecipe() != null && !station.isBusy() && !station.hasChef()) {
                return station;
            }
        }
        
        // Next, look for any station that isn't busy and has no chef
        for (Station station : assignedStations) {
            if (!station.isBusy() && !station.hasChef()) {
                return station;
            }
        }
        
        // If no suitable station found, return the first one that has no chef
        for (Station station : assignedStations) {
            if (!station.hasChef()) {
                return station;
            }
        }
        
        // If all else fails, just return the first one
        return assignedStations.get(0);
    }
    
    @Override
    public RecipeTask getNextTask(Station station) {
        if (station == null || station.getBacklog().isEmpty()) {
            return null;
        }
        
        // Simply get the first task in the backlog
        return station.getBacklog().get(0);
    }
    
    @Override
    public String getOrderIdForTask(RecipeTask task) {
        if (task == null || task.getRecipe() == null) {
            return null;
        }
        
        return task.getRecipe().getOrderId();
    }
}
