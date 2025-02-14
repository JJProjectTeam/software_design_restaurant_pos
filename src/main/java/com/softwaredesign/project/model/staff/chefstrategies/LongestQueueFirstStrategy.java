package com.softwaredesign.project.model.staff.chefstrategies;

import java.util.List;

import com.softwaredesign.project.model.placeholders.Station;

public class LongestQueueFirstStrategy implements ChefStrategy {
    @Override
    public Station chooseNextStation(List<Station> assignedStations) {
        return assignedStations.stream()
            .max((s1, s2) -> Integer.compare(s1.getBacklogSize(), s2.getBacklogSize()))
            .orElse(null);
    }
}
