package com.softwaredesign.project.staff.chefstrategies;

import java.util.List;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.softwaredesign.project.kitchen.Station;
import com.softwaredesign.project.order.Order;
import com.softwaredesign.project.order.RecipeTask;
import com.softwaredesign.project.order.Recipe;

public class OldestOrderFirstStrategy implements ChefStrategy {
    @Override
    public Station chooseNextStation(List<Station> assignedStations) {
        // Find stations with the oldest tasks that aren't busy and don't have a chef
        Station bestStation = assignedStations.stream()
            .filter(station -> station != null && station.getBacklogSize() > 0)
            .filter(station -> !station.isBusy() && !station.hasChef())
            .min((s1, s2) -> s1.getOldestTaskTime().compareTo(s2.getOldestTaskTime()))
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
        
        // Find the oldest order based on tasks in the station's backlog
        RecipeTask oldestTask = station.getBacklog().stream()
            .filter(task -> task.getRecipe() != null)
            .min(Comparator.comparing(task -> {
                // Get the order ID from the task's recipe
                Recipe recipe = task.getRecipe();
                return recipe != null ? recipe.getOrderId() : ""; 
            }))
            .orElse(null);
        
        if (oldestTask == null || oldestTask.getRecipe() == null) {
            return null;
        }
        
        // Since we no longer have direct access to the Order objects,
        // we'll need to return null and let the caller handle getting the order
        // through other means like the OrderManager
        return null; // This strategy now only helps prioritize stations, not orders directly
    }
    @Override
    public String getOrderIdForTask(RecipeTask task) {
        if (task == null || task.getRecipe() == null) {
            return null;
        }
        return task.getRecipe().getOrderId();
    }
}
