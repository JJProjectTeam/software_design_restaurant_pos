package com.softwaredesign.project.staff.chefstrategies;

import com.softwaredesign.project.order.Order;
import com.softwaredesign.project.kitchen.Station;

import java.util.List;

public interface ChefStrategy {
    /**
     * Choose the next station to work at based on the strategy
     * @param assignedStations List of stations assigned to the chef
     * @return The chosen station or null if no station is available
     */
    Station chooseNextStation(List<Station> assignedStations);
    
    /**
     * Get the next order to work on from the chosen station
     * @param station The station to get the order from
     * @return The next order to work on or null if no orders are available
     */
    Order getNextOrder(Station station);
}
