package com.softwaredesign.project.staff;

import java.util.ArrayList;
import java.util.List;

import com.softwaredesign.project.kitchen.Station;
import com.softwaredesign.project.kitchen.StationManager;
import com.softwaredesign.project.kitchen.StationType;
import com.softwaredesign.project.staff.chefstrategies.ChefStrategy;

public class Chef extends StaffMember {
    private List<Station> assignedStations; // Stations where the chef is assigned
    private ChefStrategy workStrategy; 
    private StationManager stationManager;
    private Station currentStation;
    
    public Chef(double payPerHour, double speedMultiplier, ChefStrategy strategy, StationManager stationManager) {
        super(payPerHour, speedMultiplier);
        this.assignedStations = new ArrayList<>();
        this.workStrategy = strategy;
        this.stationManager = stationManager;
    }

    public void assignToStation(StationType stationType) {
        Station station = stationManager.getStation(stationType);
        if (station != null && !assignedStations.contains(station)) {
            assignedStations.add(station);
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
        
        currentStation = workStrategy.chooseNextStation(assignedStations);
        return currentStation;
    }
    
    public Station getCurrentStation() {
        return currentStation;
    }
    
    public List<Station> getAssignedStations() {
        return new ArrayList<>(assignedStations);
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
