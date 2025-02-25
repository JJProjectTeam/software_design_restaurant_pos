package com.softwaredesign.project.staff.chefstrategies;

import java.util.List;

import com.softwaredesign.project.kitchen.Station;
import com.softwaredesign.project.order.Order;

public class ShortestQueueFirst implements ChefStrategy {
    @Override
    public Station chooseNextStation(List<Station> assignedStations) {
        return assignedStations.stream()
            .filter(station -> station != null && station.getBacklogSize() > 0)
            .min((s1, s2) -> Integer.compare(s1.getBacklogSize(), s2.getBacklogSize()))
            .orElse(null);
    }

    @Override
    public Order getNextOrder(Station station) {
        if (station == null || station.getBacklog().isEmpty()) {
            return null;
        }
        // In shortest queue first, we just take the first order from the queue
        return station.getBacklog().get(0);
    }
}
