package com.softwaredesign.project.staff.chefstrategies;

import java.util.List;

import com.softwaredesign.project.kitchen.Station;
import com.softwaredesign.project.order.Order;
import com.softwaredesign.project.order.Recipe;
import com.softwaredesign.project.order.RecipeTask;

public class ShortestQueueFirst implements ChefStrategy {
    @Override
    public Station chooseNextStation(List<Station> assignedStations) {
        // Find station with the smallest backlog that isn't busy and doesn't have a chef
        Station bestStation = assignedStations.stream()
            .filter(station -> station != null && station.getBacklogSize() > 0)
            .filter(station -> !station.hasChef())
            .min((s1, s2) -> Integer.compare(s1.getBacklogSize(), s2.getBacklogSize()))
            .orElse(null);
        
        // If no station with backlog is available, find any unoccupied station
        if (bestStation == null) {
            bestStation = assignedStations.stream()
                .filter(station -> station != null)
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
        // In shortest queue first, we just take the first task from the queue
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
