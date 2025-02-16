package com.softwaredesign.project.staff.chefstrategies;

import java.util.List;

import com.softwaredesign.project.placeholders.Station;

public class ShortestQueueFirst implements ChefStrategy {
    @Override
    public Station chooseNextStation(List<Station> assignedStations) {
        return assignedStations.stream()
            .min((s1, s2) -> Integer.compare(s1.getBacklogSize(), s2.getBacklogSize()))
            .orElse(null);
    }
}
