package com.softwaredesign.project.staff.chefstrategies;

import java.util.List;
import java.util.Objects;

import com.softwaredesign.project.kitchen.Station;

public class LongestQueueFirstStrategy implements ChefStrategy {
    @Override
    public Station chooseNextStation(List<Station> assignedStations) {
        return assignedStations.stream()
            // Filter out stations with no backlog
            .filter(Objects::nonNull)
            .max((s1, s2) -> Integer.compare(s1.getBacklogSize(), s2.getBacklogSize()))
            .orElse(null);
    }
}
