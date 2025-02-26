package com.softwaredesign.project.kitchen;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

import com.softwaredesign.project.engine.GameEngine;
import com.softwaredesign.project.orderfulfillment.CollectionPoint;

public class StationManager {
    private Map<StationType, Station> stations;
    private CollectionPoint collectionPoint;

    public StationManager(CollectionPoint collectionPoint) {
        this.stations = new HashMap<>();
        this.collectionPoint = collectionPoint;
        initializeStations();
    }
    
    public void initializeStations() {
        for (StationType type : StationType.values()) {
            Station station = new Station(type, collectionPoint);
            stations.put(type, station);
            GameEngine.getInstance().registerEntity(station);
        }
    }
    
    public Station getStation(StationType type) {
        return stations.get(type);
    }
    
    public List<Station> getAllStations() {
        return new ArrayList<>(stations.values());
    }
}
