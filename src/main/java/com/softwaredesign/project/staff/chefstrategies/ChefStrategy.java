package com.softwaredesign.project.staff.chefstrategies;

import com.softwaredesign.project.order.RecipeTask;
import com.softwaredesign.project.kitchen.Station;

import java.util.List;

public interface ChefStrategy {
    /**
     * Choose the next station to work at based on the strategy
     * @param assignedStations List of stations assigned to the chef
     * @return The chosen station or null if no station is available
     */
    Station chooseNextStation(List<Station> assignedStations);
    
    /**
     * Get the next task to work on from the chosen station
     * @param station The station to get the task from
     * @return The next recipe task to work on or null if no tasks are available
     */
    RecipeTask getNextTask(Station station);
    
    /**
     * Get the order associated with a task
     * This is a helper method since we no longer have direct access to orders in the backlog
     * @param task The task to get the order for
     * @return The order ID or null if not available
     */
    String getOrderIdForTask(RecipeTask task);
}
