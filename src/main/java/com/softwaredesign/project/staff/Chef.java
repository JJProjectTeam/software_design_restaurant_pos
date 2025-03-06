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
        System.out.println("[DEBUG] Current station: " + (currentStation != null ? currentStation.getType() : "NONE"));
        System.out.println("[DEBUG] Is working: " + isWorking);
        
        if (currentStation == null) {
            System.out.println("[DEBUG] " + name + " has no station, choosing next station");
            Station newStation = chooseNextStation();
            System.out.println("[DEBUG] " + name + " chose station: " + (newStation != null ? newStation.getType() : "NONE"));
        } else if (!currentStation.isBusy()) {
            System.out.println("[DEBUG] " + name + " at " + currentStation.getType() + " which is not busy, choosing next station");
            Station newStation = chooseNextStation();
            System.out.println("[DEBUG] " + name + " chose station: " + (newStation != null ? newStation.getType() : "NONE"));
        } else {
            System.out.println("[DEBUG] " + name + " is at busy station " + currentStation.getType() + ", staying put");
        }
    }

    public void assignToStation(StationType stationType) {
        Station station = stationManager.getStation(stationType);
        if (station != null) {
            if (!assignedStations.contains(station)) {
                assignedStations.add(station);
                System.out.println("[DEBUG-CHEF] Added " + stationType + " to " + name + "'s assigned stations list. Total: " + assignedStations.size());
            } else {
                System.out.println("[DEBUG-CHEF] " + name + " already has " + stationType + " in assigned stations list");
            }
        } else {
            System.out.println("[DEBUG-CHEF] Could not find station of type " + stationType);
        }
    }

    public void setWorkStrategy(ChefStrategy strategy) {
        this.workStrategy = strategy;
    }

    public ChefStrategy getWorkStrategy() {
        return workStrategy;
    }
    
    public void removeStationAssignment(Station station) {
        assignedStations.remove(station);
        if (currentStation == station) {
            currentStation = null;
        }
    }

    public Station chooseNextStation() {
        if (assignedStations.isEmpty()) {
            return null;
        }
        
        // If the chef is currently working on a task, they shouldn't be reassigned
        if (isWorking) {
            return currentStation;
        }
        
        System.out.println("Chef " + name + " looking for work at " + assignedStations.size() + " assigned stations");
        
        // PRIORITY 1: First check if there are any PREP stations with pending tasks
        // This ensures we prioritize the start of the recipe pipeline
        for (Station station : assignedStations) {
            if (station.getType() == StationType.PREP && !station.isBusy() && station.hasBacklogItems()) {
                System.out.println("[IMPORTANT] Chef " + name + " prioritizing PREP station with backlog items");
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
        for (Station station : assignedStations) {
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
        
        // PRIORITY 3: Use the strategy for normal station selection
        Station nextStation = workStrategy.chooseNextStation(assignedStations);
        
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
}
