package com.softwaredesign.project.staff;

import java.util.ArrayList;
import java.util.List;

import com.softwaredesign.project.engine.Entity;
import com.softwaredesign.project.kitchen.Station;
import com.softwaredesign.project.kitchen.StationManager;
import com.softwaredesign.project.kitchen.StationType;
import com.softwaredesign.project.staff.chefstrategies.ChefStrategy;

public class Chef extends StaffMember {
    private List<Station> assignedStations; // Stations where the chef is assigned
    private ChefStrategy workStrategy; 
    private StationManager stationManager;
    private Station currentStation; // The station where the chef is currently working
    private String name;
    private boolean isWorking; // Flag to indicate if the chef is currently working on a task
    private static int chefCounter = 0;
    
    public Chef(double payPerHour, double speedMultiplier, ChefStrategy strategy, StationManager stationManager) {
        super(payPerHour, speedMultiplier);
        this.assignedStations = new ArrayList<>();
        this.workStrategy = strategy;
        this.stationManager = stationManager;
        this.name = "Chef " + (++chefCounter);
        this.isWorking = false;
    }
    
    public Chef(String name, double payPerHour, double speedMultiplier, ChefStrategy strategy, StationManager stationManager) {
        super(payPerHour, speedMultiplier);
        this.assignedStations = new ArrayList<>();
        this.workStrategy = strategy;
        this.stationManager = stationManager;
        this.name = name;
        this.isWorking = false;
    }
    
    public String getName() {
        return name;
    }
    
    // Chef periodically checks for work
    public void checkForWork() {
        System.out.println("[DEBUG] " + name + " checking for work");
        if (currentStation != null) {
            System.out.println("[DEBUG] Current station: " + currentStation.getType());
        } else {
            System.out.println("[DEBUG] No current station");
        }
        
        // Instead of always choosing a new station if not busy, check if any assigned station actually has tasks
        int totalBacklog = 0;
        for (Station station : assignedStations) {
            totalBacklog += station.getBacklogSize();
        }
        
        if (totalBacklog == 0) {
            System.out.println("[DEBUG] " + name + " finds no pending tasks in assigned stations, staying at " + (currentStation != null ? currentStation.getType() : "no station"));
            return;
        }
        
        // There are pending tasks, proceed to choose a new station
        System.out.println("[DEBUG] " + name + " found pending tasks, choosing next station");
        Station newStation = chooseNextStation();
        if (newStation != null) {
            System.out.println("[DEBUG] " + name + " chose station: " + newStation.getType());
            if (newStation.getCurrentTask() != null) {
                isWorking = true;
                System.out.println("[DEBUG] " + name + " is now working on task: " + newStation.getCurrentTask().getName());
            }
        } else {
            System.out.println("[DEBUG] " + name + " couldn't find a suitable station despite pending tasks");
        }
    }

    public void assignToStation(StationType stationType) {
        System.out.println("[DEBUG-CHEF-ASSIGN] " + name + " being assigned to " + stationType);
        
        // Check if we already have this station type in our assigned stations
        boolean alreadyAssigned = false;
        for (Station existingStation : assignedStations) {
            if (existingStation.getType() == stationType) {
                System.out.println("[DEBUG-CHEF-ASSIGN] " + name + " already has a " + stationType + " station assigned");
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
                        System.out.println("[DEBUG-CHEF-ASSIGN] Added " + stationType + " station to " + 
                            name + "'s assigned stations list. Total stations: " + assignedStations.size());
                    }
                }
            } else {
                System.out.println("[DEBUG-CHEF-ASSIGN] Could not find any stations of type " + stationType);
            }
        }
        
        // Print out all assigned stations for debugging
        System.out.println("[DEBUG-CHEF-ASSIGN] " + name + " now has " + assignedStations.size() + " assigned stations:");
        for (Station station : assignedStations) {
            System.out.println("  - " + station.getType());
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
            System.out.println("[DEBUG-CHEF-REMOVE] Attempted to remove null station from " + name);
            return;
        }
        
        System.out.println("[DEBUG-CHEF-REMOVE] Removing " + station.getType() + " station from " + name);
        
        boolean removed = assignedStations.remove(station);
        
        if (removed) {
            System.out.println("[DEBUG-CHEF-REMOVE] Successfully removed " + station.getType() + 
                " station from " + name + "'s assigned stations");
        } else {
            System.out.println("[DEBUG-CHEF-REMOVE] Station " + station.getType() + 
                " was not in " + name + "'s assigned stations list");
        }
        
        // If this was the chef's current station, clear that reference
        if (currentStation == station) {
            System.out.println("[DEBUG-CHEF-REMOVE] Clearing current station reference for " + name);
            currentStation = null;
        }
        
        // Print remaining assigned stations
        System.out.println("[DEBUG-CHEF-REMOVE] " + name + " now has " + assignedStations.size() + " assigned stations:");
        for (Station s : assignedStations) {
            System.out.println("  - " + s.getType());
        }
    }

    public Station chooseNextStation() {
        System.out.println("[DEBUG-CHEF-CHOOSE] " + name + " choosing next station");
        System.out.println("[DEBUG-CHEF-CHOOSE] Current station: " + (currentStation != null ? currentStation.getType() : "NONE"));
        System.out.println("[DEBUG-CHEF-CHOOSE] Assigned stations: " + assignedStations.size());
        
        // If the chef is currently working on a task, they shouldn't be reassigned
        if (isWorking) {
            System.out.println("[DEBUG-CHEF-CHOOSE] " + name + " is already working, staying at current station");
            return currentStation;
        }
        
        // Revised logic: Only search among assigned stations.
        List<Station> stationsToCheck = new ArrayList<>();
        
        if (!assignedStations.isEmpty()) {
             System.out.println("[DEBUG-CHEF-CHOOSE] " + name + " looking for work in assigned stations only");
             stationsToCheck.addAll(assignedStations);
        } else {
             System.out.println("[DEBUG-CHEF-CHOOSE] " + name + " has no assigned stations, cannot check work outside assignments");
             return null;
        }
        
        // PRIORITY 1: First check if there are any PREP stations with pending tasks
        // This ensures we prioritize the start of the recipe pipeline
        for (Station station : stationsToCheck) {
            if (station.getType() == StationType.PREP && station.hasBacklogItems() && !station.hasChef()) {
                System.out.println("[IMPORTANT] Chef " + name + " prioritizing free PREP station with backlog items");
                station.registerChef(this);
                StringBuilder logMessage = new StringBuilder();
                logMessage.append(name).append(" moved to PREP station (priority assignment)");
                if (station.getCurrentTask() != null) {
                    logMessage.append(" to work on: ").append(station.getCurrentTask().getName());
                } else {
                    logMessage.append(" (waiting for task assignment)");
                }
                System.out.println(logMessage.toString());
                return station;
            }
        }
        
        // PRIORITY 2: Check if there's a station with a task already assigned but no chef
        for (Station station : stationsToCheck) {
            if (station.getCurrentTask() != null && !station.hasChef()) {
                System.out.println("[DEBUG] Chef " + name + " found station with task but no chef");
                station.registerChef(this);
                
                StringBuilder logMessage = new StringBuilder();
                logMessage.append(name).append(" moved to ").append(station.getType()).append(" station");
                logMessage.append(" to work on: ").append(station.getCurrentTask().getName());
                logMessage.append(" for recipe: ").append(station.getCurrentRecipe().getName());
                
                if (station.getCurrentRecipe().getOrderId() != null) {
                    logMessage.append(" (Order ID: ").append(station.getCurrentRecipe().getOrderId()).append(")");
                }
                
                System.out.println(logMessage.toString());
                return station;
            }
        }
        
        // PRIORITY 3: Check for any station with backlog items
        for (Station station : stationsToCheck) {
            if (station.hasBacklogItems() && !station.hasChef()) {
                System.out.println("[DEBUG] Chef " + name + " found free station with backlog items");
                station.registerChef(this);
                StringBuilder logMessage = new StringBuilder();
                logMessage.append(name).append(" moved to ").append(station.getType()).append(" station");
                logMessage.append(" (station has backlog items)");
                System.out.println(logMessage.toString());
                return station;
            }
        }
        
        // PRIORITY 4: Use the strategy for normal station selection
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
                
            System.out.println(logMessage.toString());
            return nextStation;
        } else {
            // No station available, clear the current station
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
}
