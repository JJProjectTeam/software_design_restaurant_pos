package com.softwaredesign.project.staff.chefstrategies;

import java.util.ArrayList;
import java.util.List;

import com.softwaredesign.project.kitchen.Station;
import com.softwaredesign.project.kitchen.StationManager;
import com.softwaredesign.project.kitchen.StationType;
import com.softwaredesign.project.order.Order;
import com.softwaredesign.project.order.Recipe;
import com.softwaredesign.project.order.RecipeTask;

/**
 * A dynamic chef strategy that allows chefs to move between stations as needed.
 * Chefs will prioritize stations that have tasks assigned to them.
 */
public class DynamicChefStrategy implements ChefStrategy {
    
    private StationManager stationManager;
    
    public DynamicChefStrategy(StationManager stationManager) {
        this.stationManager = stationManager;
    }
    
    @Override
    public Station chooseNextStation(List<Station> assignedStations) {
        if (assignedStations == null || assignedStations.isEmpty()) {
            return null;
        }
        
        // Get a list of all stations that have work to do
        List<Station> stationsWithWork = new ArrayList<>();
        
        // First priority: Find empty stations with ready tasks that don't already have a chef assigned
        for (Station station : stationManager.getAllStations()) {
            // Only consider stations that don't have a chef and are in the chef's assigned stations
            if (!station.hasChef() && assignedStations.contains(station)) {
                Recipe recipe = station.findRecipeWithReadyTasks();
                if (recipe != null) {
                    RecipeTask task = findTaskForStation(recipe, station.getType());
                    if (task != null && task.areDependenciesMet() && !task.isCompleted()) {
                        System.out.println("Strategy found available task at empty station: " + task.getName() + 
                                         " for recipe: " + recipe.getName() + 
                                         " at station: " + station.getType());
                        return station;
                    }
                }
            }
        }
        
        // Second priority: Check for specific station types with pending work
        // This helps separate chefs to different station types
        StationType[] stationOrder = {StationType.GRILL, StationType.PLATE, StationType.PREP};
        for (StationType type : stationOrder) {
            for (Station station : stationManager.getAllStations()) {
                // Only consider stations that don't have a chef, are of the current type, and are in the chef's assigned stations
                if (station.getType() == type && !station.hasChef() && assignedStations.contains(station)) {
                    Recipe recipe = station.findRecipeWithReadyTasks();
                    if (recipe != null) {
                        RecipeTask task = findTaskForStation(recipe, station.getType());
                        if (task != null && task.areDependenciesMet() && !task.isCompleted()) {
                            System.out.println("Strategy found task by station type priority: " + task.getName() + 
                                         " for recipe: " + recipe.getName() + 
                                         " at station: " + station.getType());
                            return station;
                        }
                    }
                }
            }
        }
        
        // Third priority: Check for any station with pending tasks
        for (Station station : stationManager.getAllStations()) {
            // Only consider stations that don't have a chef and are in the chef's assigned stations
            if (!station.hasChef() && assignedStations.contains(station)) {
                for (RecipeTask task : station.getBacklog()) {
                    // Check if the task is for this station type and has dependencies met
                    if (task.getStationType() == station.getType() && 
                        task.areDependenciesMet() && 
                        !task.isCompleted()) {
                        Recipe recipe = task.getRecipe();
                        if (recipe != null) {
                            System.out.println("Strategy found backlog task: " + task.getName() + 
                                             " for recipe: " + recipe.getName() + 
                                             " at station: " + station.getType());
                            return station;
                        }
                    }
                }
            }
        }
        
        // Last resort: return any unoccupied station to avoid chef standing idle
        for (Station station : assignedStations) {
            // Only consider stations that don't already have a chef
            if (!station.hasChef()) {
                return station;
            }
        }
        
        // If all else fails, return the first assigned station
        return assignedStations.get(0);
    }
    
    /**
     * Finds a task in the recipe that matches the given station type and has all dependencies met
     * @param recipe The recipe to check for tasks
     * @param stationType The station type to find tasks for
     * @return A task that can be done at the station, or null if none found
     */
    private RecipeTask findTaskForStation(Recipe recipe, StationType stationType) {
        if (recipe == null) {
            return null;
        }
        
        for (RecipeTask task : recipe.getUncompletedTasks()) {
            if (task.getStationType() == stationType && task.areDependenciesMet()) {
                return task;
            }
        }
        
        return null;
    }
    
    @Override
    public RecipeTask getNextTask(Station station) {
        if (station == null || station.getBacklog().isEmpty()) {
            return null;
        }
        
        // Find tasks with all dependencies met
        for (RecipeTask task : station.getBacklog()) {
            if (task.getStationType() == station.getType() && task.areDependenciesMet() && !task.isCompleted()) {
                Recipe recipe = task.getRecipe();
                if (recipe != null) {
                    System.out.println("Dynamic strategy found task " + task.getName() + 
                                   " for recipe " + recipe.getName() + " ready at " + 
                                   station.getType() + " station");
                    return task;
                }
            }
        }
        
        // Simply get the first task in the backlog if no ready tasks found
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
