package com.softwaredesign.project.model.staff.chefstrategies;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.softwaredesign.project.model.kitchen.Station;
import com.softwaredesign.project.model.kitchen.StationType;
import com.softwaredesign.project.model.order.RecipeTask;

import java.util.List;

public class PriorityBasedChefStrategy implements ChefStrategy {
    private static final Logger logger = LoggerFactory.getLogger(PriorityBasedChefStrategy.class);
    
    @Override
    public Station chooseNextStation(List<Station> stationsToCheck) {
        if (stationsToCheck == null || stationsToCheck.isEmpty()) {
            return null;
        }

        // PRIORITY 1: Check PREP stations with backlog items
        for (Station station : stationsToCheck) {
            if (station.getType() == StationType.PREP && station.hasBacklogItems() && !station.hasChef()) {
                logger.info("[IMPORTANT] Prioritizing free PREP station with backlog items");
                StringBuilder logMessage = new StringBuilder();
                logMessage.append("Chef moved to PREP station (priority assignment)");
                if (station.getCurrentTask() != null) {
                    logMessage.append(" to work on: ").append(station.getCurrentTask().getName());
                } else {
                    logMessage.append(" (waiting for task assignment)");
                }
                logger.info(logMessage.toString());
                return station;
            }
        }
        
        // PRIORITY 2: Check stations with assigned tasks but no chef
        for (Station station : stationsToCheck) {
            if (station.getCurrentTask() != null && !station.hasChef()) {
                logger.info("[DEBUG] Found station with task but no chef");
                
                StringBuilder logMessage = new StringBuilder();
                logMessage.append("Chef moved to ").append(station.getType()).append(" station");
                logMessage.append(" to work on: ").append(station.getCurrentTask().getName());
                logMessage.append(" for recipe: ").append(station.getCurrentRecipe().getName());
                
                if (station.getCurrentRecipe().getOrderId() != null) {
                    logMessage.append(" (Order ID: ").append(station.getCurrentRecipe().getOrderId()).append(")");
                }
                
                logger.info(logMessage.toString());
                return station;
            }
        }
        
        // PRIORITY 3: Check for any station with backlog items
        for (Station station : stationsToCheck) {
            if (station.hasBacklogItems() && !station.hasChef()) {
                logger.info("[DEBUG] Found free station with backlog items");
                StringBuilder logMessage = new StringBuilder();
                logMessage.append("Chef moved to ").append(station.getType()).append(" station");
                logMessage.append(" (station has backlog items)");
                logger.info(logMessage.toString());
                return station;
            }
        }
        
        return null;
    }
    
    @Override
    public RecipeTask getNextTask(Station station) {
        if (station == null || station.getBacklog().isEmpty()) {
            return null;
        }
        
        // Return the first task in the backlog
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