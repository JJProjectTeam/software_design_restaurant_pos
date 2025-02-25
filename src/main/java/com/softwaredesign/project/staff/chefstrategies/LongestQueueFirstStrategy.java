package com.softwaredesign.project.staff.chefstrategies;

import java.util.List;
import java.util.Objects;

import com.softwaredesign.project.kitchen.Station;
import com.softwaredesign.project.order.Order;

public class LongestQueueFirstStrategy implements ChefStrategy {
    @Override
    public Station chooseNextStation(List<Station> assignedStations) {
        return assignedStations.stream()
            .filter(Objects::nonNull)
            .filter(station -> station.getBacklogSize() > 0)
            .max((s1, s2) -> Integer.compare(s1.getBacklogSize(), s2.getBacklogSize()))
            .orElse(null);
    }

    @Override
    public Order getNextOrder(Station station) {
        if (station == null || station.getBacklog().isEmpty()) {
            return null;
        }
        return station.getBacklog().get(0); // assumes 
    }
}
