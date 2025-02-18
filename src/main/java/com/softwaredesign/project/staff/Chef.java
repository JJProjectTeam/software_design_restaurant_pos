package com.softwaredesign.project.staff;

import java.util.ArrayList;
import java.util.List;

import com.softwaredesign.project.kitchen.StationManager;
import com.softwaredesign.project.order.Station;
import com.softwaredesign.project.order.StationType;
import com.softwaredesign.project.staff.chefstrategies.ChefStrategy;

public class Chef extends StaffMember {
    private List<Station> assignedStations;
    private ChefStrategy workStrategy;
    private StationManager stationManager;
    
        public Chef(double payPerHour, double speedMultiplier, ChefStrategy strategy, StationManager stationManager) {
            super(payPerHour, speedMultiplier);
            this.assignedStations = new ArrayList<>();
            this.workStrategy = strategy;
            this.stationManager = stationManager;
    }

    public void assignToStation(StationType stationType) {
        Station station = stationManager.getStation(stationType);
        if (!assignedStations.contains(station)) {
            assignedStations.add(station);
        }
    }

    public void setWorkStrategy(ChefStrategy strategy) {
        this.workStrategy = strategy;
    }

    public Station chooseNextStation() {
        return workStrategy.chooseNextStation(assignedStations);
    }
}
