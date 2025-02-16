package com.softwaredesign.project.staff.chefstrategies;

import java.util.List;

import com.softwaredesign.project.placeholders.Station;

public class OldestOrderFirstStrategy implements ChefStrategy {
    @Override
    public Station chooseNextStation(List<Station> assignedStations) {
        return assignedStations.stream()
            .min((s1, s2) -> s1.getOldestOrderTime().compareTo(s2.getOldestOrderTime()))
            .orElse(null);
    }
}
