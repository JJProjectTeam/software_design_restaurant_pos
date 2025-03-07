package com.softwaredesign.project.staff.chefstrategies;

import java.util.List;
import java.util.Objects;

import com.softwaredesign.project.kitchen.Station;
import com.softwaredesign.project.order.Order;
import com.softwaredesign.project.order.Recipe;
import com.softwaredesign.project.order.RecipeTask;

public class LongestQueueFirstStrategy implements ChefStrategy {
    @Override
    public Station chooseNextStation(List<Station> assignedStations) {
        // Find stations with the largest backlog that aren't busy and don't have a chef
        Station bestStation = assignedStations.stream()
            .filter(Objects::nonNull)
            .filter(station -> station.getBacklogSize() > 0)
            .filter(station -> !station.isBusy() && !station.hasChef())
            .max((s1, s2) -> Integer.compare(s1.getBacklogSize(), s2.getBacklogSize()))
            .orElse(null);
            
        // If no station with backlog is available, find any unoccupied station
        if (bestStation == null) {
            bestStation = assignedStations.stream()
                .filter(Objects::nonNull)
                .filter(station -> !station.hasChef())
                .findFirst()
                .orElse(null);
        }
        
        return bestStation;
    }

    @Override
    public RecipeTask getNextTask(Station station) {
        if (station == null || station.getBacklog().isEmpty()) {
            return null;
        }
        // Just get the first task from the queue
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
