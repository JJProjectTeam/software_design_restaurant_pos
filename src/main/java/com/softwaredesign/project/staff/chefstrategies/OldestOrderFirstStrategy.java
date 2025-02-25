package com.softwaredesign.project.staff.chefstrategies;

import java.util.List;
import java.util.Comparator;

import com.softwaredesign.project.kitchen.Station;
import com.softwaredesign.project.order.Order;

public class OldestOrderFirstStrategy implements ChefStrategy {
    @Override
    public Station chooseNextStation(List<Station> assignedStations) {
        return assignedStations.stream()
            .filter(station -> station != null && station.getBacklogSize() > 0)
            .min((s1, s2) -> s1.getOldestOrderTime().compareTo(s2.getOldestOrderTime()))
            .orElse(null);
    }

    @Override
    public Order getNextOrder(Station station) {
        if (station == null || station.getBacklog().isEmpty()) {
            return null;
        }
        
        // Find the oldest order in the station's backlog
        return station.getBacklog().stream()
            .min(Comparator.comparing(Order::getOrderId)) // alphabetical order
            .orElse(null);
    }
}
