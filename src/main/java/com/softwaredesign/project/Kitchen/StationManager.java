package com.softwaredesign.project.kitchen;

import java.util.HashMap;
import java.util.Map;

import com.softwaredesign.project.order.Station;
import com.softwaredesign.project.order.StationType;

public class StationManager {
    private Map<StationType, Station> stations;

    public StationManager() {
        this.stations = new HashMap<>();
        initializeStations();
    }
    
    public void initializeStations() {
        stations.put(StationType.PREP, new Station(StationType.PREP));
        stations.put(StationType.GRILL, new Station(StationType.GRILL));
        stations.put(StationType.PLATE, new Station(StationType.PLATE));
    }
    
    public Station getStation(StationType type) {
        return stations.get(type);
    }
    
}
