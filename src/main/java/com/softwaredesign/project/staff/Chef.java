package com.softwaredesign.project.staff;

import java.util.ArrayList;
import java.util.List;

import com.softwaredesign.project.engine.Entity;
import com.softwaredesign.project.kitchen.Station;
import com.softwaredesign.project.kitchen.StationManager;
import com.softwaredesign.project.kitchen.StationType;
import com.softwaredesign.project.staff.chefstrategies.ChefStrategy;

import com.softwaredesign.project.staff.staffspeeds.ISpeedComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Chef extends StaffMember {
    private static final Logger logger = LoggerFactory.getLogger(Chef.class);
    private List<Station> assignedStations; // Stations where the chef is assigned
    private ChefStrategy workStrategy; 
    private StationManager stationManager;
    private Station currentStation; // The station where the chef is currently working
    private String name;
    private boolean isWorking; // Flag to indicate if the chef is currently working on a task
    private static int chefCounter = 0;
    private ISpeedComponent speedDecorator;
    
    public Chef(double pay, ISpeedComponent speedDecorator, ChefStrategy strategy, StationManager stationManager) {
        super(pay);
        this.speedDecorator = speedDecorator;
        this.assignedStations = new ArrayList<>();
        this.workStrategy = strategy;
        this.stationManager = stationManager;
        this.name = "Chef " + (++chefCounter);
        this.isWorking = false;
        logger.info("[DEBUG] Created new chef: " + name + speedDecorator.getSpeedMultiplier());
    }
    
    public Chef(String name, double pay, ISpeedComponent speedDecorator, ChefStrategy strategy, StationManager stationManager) {
        super(pay);
        this.speedDecorator = speedDecorator;
        this.assignedStations = new ArrayList<>();
        this.workStrategy = strategy;
        this.stationManager = stationManager;
        this.name = name;
        this.isWorking = false;
        logger.info("[DEBUG] Created new chef: " + name + speedDecorator.getSpeedMultiplier());
    }
    
    public String getName() {
        return name;
    }
    
    // Chef periodically checks for work
    public void checkForWork() {
        logger.info("[DEBUG] " + name + " checking for work");
        if (currentStation != null) {
            logger.info("[DEBUG] Current station: " + currentStation.getType());
        } else {
            logger.info("[DEBUG] No current station");
        }
        
        // Check if any assigned station actually has tasks
        int totalBacklog = 0;
        for (Station station : assignedStations) {
            totalBacklog += station.getBacklogSize();
        }
        
        if (totalBacklog > 0) {
            // There are pending tasks, proceed to choose a new station
            logger.info("[DEBUG] " + name + " found pending tasks, choosing next station");
            Station newStation = chooseNextStation();
            if (newStation != null) {
                logger.info("[DEBUG] " + name + " chose station: " + newStation.getType());
                if (newStation.getCurrentTask() != null) {
                    isWorking = true;
                    logger.info("[DEBUG] " + name + " is now working on task: " + newStation.getCurrentTask().getName());
                }
            } else {
                logger.info("[DEBUG] " + name + " couldn't find a suitable station despite pending tasks");
            }
        } else {
            // No pending tasks, but we should still ensure the chef has a current station
            logger.info("[DEBUG] " + name + " finds no pending tasks in assigned stations");
            
            // If chef doesn't have a current station, choose one to remain visible
            if (currentStation == null) {
                logger.info("[DEBUG] " + name + " has no current station, choosing one to remain visible");
                chooseNextStation(); // This will ensure the chef gets assigned to a station
            } else {
                logger.info("[DEBUG] " + name + " staying at " + currentStation.getType());
            }
        }
    }

    public void assignToStation(StationType stationType) {
        logger.info("[DEBUG-CHEF-ASSIGN] " + name + " being assigned to " + stationType);
        
        // Check if we already have this station type in our assigned stations
        boolean alreadyAssigned = false;
        for (Station existingStation : assignedStations) {
            if (existingStation.getType() == stationType) {
                logger.info("[DEBUG-CHEF-ASSIGN] " + name + " already has a " + stationType + " station assigned");
                alreadyAssigned = true;
                break;
            }
        }
        
        if (!alreadyAssigned) {
            // Get all stations of this type
            List<Station> stationsOfType = stationManager.getStationsByType(stationType);
            
            if (stationsOfType != null && !stationsOfType.isEmpty()) {
                // Add all stations of this type to the chef's assigned stations
                for (Station station : stationsOfType) {
                    if (!assignedStations.contains(station)) {
                        assignedStations.add(station);
                        logger.info("[DEBUG-CHEF-ASSIGN] Added " + stationType + " station to " + 
                            name + "'s assigned stations list. Total stations: " + assignedStations.size());
                    }
                }
            } else {
                logger.info("[DEBUG-CHEF-ASSIGN] Could not find any stations of type " + stationType);
            }
        }
        
        // Print out all assigned stations for debugging
        logger.info("[DEBUG-CHEF-ASSIGN] " + name + " now has " + assignedStations.size() + " assigned stations:");
        for (Station station : assignedStations) {
            logger.info("  - " + station.getType());
        }
    }

    public void setWorkStrategy(ChefStrategy strategy) {
        this.workStrategy = strategy;
    }

    public ChefStrategy getWorkStrategy() {
        return workStrategy;
    }
    
    public void removeStationAssignment(Station station) {
        if (station == null) {
            logger.info("[DEBUG-CHEF-REMOVE] Attempted to remove null station from " + name);
            return;
        }
        
        logger.info("[DEBUG-CHEF-REMOVE] Removing " + station.getType() + " station from " + name);
        
        boolean removed = assignedStations.remove(station);
        
        if (removed) {
            logger.info("[DEBUG-CHEF-REMOVE] Successfully removed " + station.getType() + 
                " station from " + name + "'s assigned stations");
        } else {
            logger.info("[DEBUG-CHEF-REMOVE] Station " + station.getType() + 
                " was not in " + name + "'s assigned stations list");
        }
        
        // If this was the chef's current station, clear that reference
        if (currentStation == station) {
            logger.info("[DEBUG-CHEF-REMOVE] Clearing current station reference for " + name);
            currentStation = null;
        }
        
        // Print remaining assigned stations
        logger.info("[DEBUG-CHEF-REMOVE] " + name + " now has " + assignedStations.size() + " assigned stations:");
        for (Station s : assignedStations) {
            logger.info("  - " + s.getType());
        }
    }

    public Station chooseNextStation() {
        logger.info("[DEBUG-CHEF-CHOOSE] " + name + " choosing next station");
        logger.info("[DEBUG-CHEF-CHOOSE] Current station: " + (currentStation != null ? currentStation.getType() : "NONE"));
        logger.info("[DEBUG-CHEF-CHOOSE] Assigned stations: " + assignedStations.size());
        
        // If the chef is currently working on a task, they shouldn't be reassigned
        if (isWorking) {
            logger.info("[DEBUG-CHEF-CHOOSE] " + name + " is already working, staying at current station");
            return currentStation;
        }
        
        // Only search among assigned stations
        List<Station> stationsToCheck = new ArrayList<>();
        
        if (!assignedStations.isEmpty()) {
             logger.info("[DEBUG-CHEF-CHOOSE] " + name + " looking for work in assigned stations only");
             stationsToCheck.addAll(assignedStations);
        } else {
             logger.info("[DEBUG-CHEF-CHOOSE] " + name + " has no assigned stations, cannot check work outside assignments");
             
             // If chef has no assigned stations, this is likely an error - recover by logging
             logger.info("[DEBUG-CHEF-CHOOSE] RECOVERY: " + name + " has no assigned stations - please check initialization");
             return null;
        }
        
        // Use the strategy for station selection
        Station nextStation = workStrategy.chooseNextStation(stationsToCheck);
        
        // If we found a station, register with it
        if (nextStation != null) {
            // The registerChef method will handle all the necessary state updates
            // including unregistering from the previous station
            nextStation.registerChef(this);
            
            // Now currentStation should be updated by the registerChef method, so we can use it
            StringBuilder logMessage = new StringBuilder();
            logMessage.append(name).append(" moved to ").append(nextStation.getType()).append(" station");
                
            // Add information about the current task if available
            if (nextStation.getCurrentTask() != null) {
                logMessage.append(" to work on: ").append(nextStation.getCurrentTask().getName());
                logMessage.append(" for recipe: ").append(nextStation.getCurrentRecipe().getName());
                if (nextStation.getCurrentRecipe().getOrderId() != null) {
                    logMessage.append(" (Order ID: ").append(nextStation.getCurrentRecipe().getOrderId()).append(")");
                }
            } else {
                logMessage.append(" (no active task)");
            }
                
            logger.info(logMessage.toString());
            return nextStation;
        } else {
            // No station with pending tasks was found, but we should still ensure the chef
            // is assigned to one of their assigned stations to remain visible
            logger.info("[DEBUG-CHEF-CHOOSE] No station with pending tasks found, but ensuring chef stays visible");
            
            // If chef is already at a station, keep them there
            if (currentStation != null && assignedStations.contains(currentStation)) {
                logger.info("[DEBUG-CHEF-CHOOSE] " + name + " staying at current station: " + currentStation.getType() + " (no pending tasks)");
                return currentStation;
            }
            
            // Otherwise, assign to the first available station in their assigned list
            for (Station station : assignedStations) {
                if (!station.hasChef()) {
                    logger.info("[DEBUG-CHEF-CHOOSE] " + name + " moving to " + station.getType() + " station (no pending tasks but staying visible)");
                    station.registerChef(this);
                    return station;
                }
            }
            
            // If all stations have chefs, pick the first one anyway
            if (!assignedStations.isEmpty()) {
                Station defaultStation = assignedStations.get(0);
                logger.info("[DEBUG-CHEF-CHOOSE] " + name + " moving to " + defaultStation.getType() + " station (all stations occupied but staying visible)");
                defaultStation.registerChef(this);
                return defaultStation;
            }
            
            // Only if we really can't find any station, set current station to null
            logger.info("[DEBUG-CHEF-CHOOSE] " + name + " couldn't find any station to stay visible at");
            currentStation = null;
            return null;
        }
    }
    
    public Station getCurrentStation() {
        return currentStation;
    }
    
    public List<Station> getAssignedStations() {
        return new ArrayList<>(assignedStations);
    }
    
    /**
     * Set the chef's current station directly
     * @param station The station where the chef is now working
     */
    public void setCurrentStation(Station station) {
        this.currentStation = station;
    }
    
    public boolean isWorking() {
        return isWorking;
    }
    
    public void setWorking(boolean working) {
        this.isWorking = working;
    }
    
    public boolean isAssignedToStation(StationType stationType) {
        for (Station station : assignedStations) {
            if (station.getType() == stationType) {
                return true;
            }
        }
        return false;
    }
    public void addStationAssignment(Station station) {
        if (!assignedStations.contains(station)) {
            assignedStations.add(station);
        }
    }
    
    public void clearStationAssignments() {
        assignedStations.clear();
    }

    public double getSpeedMultiplier() {
        return speedDecorator.getSpeedMultiplier();
    }

    /**
     * Checks and resolves any inconsistencies between the chef's state and its current station.
     * This is useful for fixing duplication issues where references get out of sync.
     */
    public void checkStationConsistency() {
        logger.info("[DEBUG-CHEF-SYNC] Checking station consistency for " + name);
        
        // Case 1: Chef thinks it's at a station, but that station doesn't have this chef assigned
        if (currentStation != null) {
            if (currentStation.getAssignedChef() != this) {
                logger.info("[DEBUG-CHEF-SYNC] Inconsistency detected: " + name + 
                    " thinks it's at " + currentStation.getType() + 
                    " but that station has " + 
                    (currentStation.getAssignedChef() != null ? 
                        currentStation.getAssignedChef().getName() : "no chef") + " assigned");
                
                // Fix: Clear chef's current station reference
                logger.info("[DEBUG-CHEF-SYNC] Fixing: Setting currentStation to null for " + name);
                currentStation = null;
            }
        }
        
        // Case 2: Chef is marked as working but has no current station
        if (isWorking && currentStation == null) {
            logger.info("[DEBUG-CHEF-SYNC] Inconsistency detected: " + name + 
                " is marked as working but has no current station");
            
            // Fix: Reset working status
            logger.info("[DEBUG-CHEF-SYNC] Fixing: Setting isWorking to false for " + name);
            isWorking = false;
        }
        
        // Case 3: Chef is not working but is at a station with an active task
        if (!isWorking && currentStation != null && 
            currentStation.getCurrentTask() != null && 
            currentStation.getAssignedChef() == this) {
            
            logger.info("[DEBUG-CHEF-SYNC] Inconsistency detected: " + name + 
                " is not marked as working but is at " + currentStation.getType() + 
                " station with an active task: " + currentStation.getCurrentTask().getName());
            
            // Fix: Set working status to match the station's state
            logger.info("[DEBUG-CHEF-SYNC] Fixing: Setting isWorking to true for " + name);
            isWorking = true;
        }
    }

}
