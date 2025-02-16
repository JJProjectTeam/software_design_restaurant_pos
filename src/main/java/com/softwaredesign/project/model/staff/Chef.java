package com.softwaredesign.project.model.staff;

import java.util.ArrayList;
import java.util.List;

import com.softwaredesign.project.model.placeholders.Station;
import com.softwaredesign.project.model.staff.chefstrategies.ChefStrategy;



public class Chef extends StaffMember {
    private List<Station> assignedStations;
    private ChefStrategy workStrategy;

    public Chef(double payPerHour, double speedMultiplier, ChefStrategy strategy) {
        super(payPerHour, speedMultiplier);
        this.assignedStations = new ArrayList<>();
        this.workStrategy = strategy;
    }

    public List<Station> getAssignedStations() {
        return assignedStations;
    }

    public void setAssignedStations(List<Station> assignedStations) {
        this.assignedStations = assignedStations;
    }

    public void setWorkStrategy(ChefStrategy strategy) {
        this.workStrategy = strategy;
    }

    public Station chooseNextStation() {
        return workStrategy.chooseNextStation(assignedStations);
    }
}
