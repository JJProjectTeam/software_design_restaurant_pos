package com.softwaredesign.project.staff.chefstrategies;

import java.util.List;

import com.softwaredesign.project.order.Station;

public interface ChefStrategy {
    Station chooseNextStation(List<Station> assignedStations);
}
