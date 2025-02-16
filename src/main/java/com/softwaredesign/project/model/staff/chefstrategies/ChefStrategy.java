package com.softwaredesign.project.model.staff.chefstrategies;

import java.util.List;

import com.softwaredesign.project.model.placeholders.Station;

public interface ChefStrategy {
    Station chooseNextStation(List<Station> assignedStations);
}
