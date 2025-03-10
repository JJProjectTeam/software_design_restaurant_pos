package com.softwaredesign.project.kitchen;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

import com.softwaredesign.project.engine.GameEngine;
import com.softwaredesign.project.orderfulfillment.CollectionPoint;

public class StationManager {
    private Map<StationType, List<Station>> stations;  // Changed to Map<StationType, List<Station>>
    private CollectionPoint collectionPoint;

    public StationManager(CollectionPoint collectionPoint) {
        this.stations = new HashMap<>();
        this.collectionPoint = collectionPoint;
        // Initialize lists for each station type
        for (StationType type : StationType.values()) {
            stations.put(type, new ArrayList<>());
        }
    }

    public void addStation(Station station) {
        stations.get(station.getType()).add(station);
        GameEngine.getInstance().registerEntity(station);
        System.out.println("[StationManager] Added station: " + station.getType() + 
            " (Total: " + stations.get(station.getType()).size() + ")");
    }
    
    public Station getStation(StationType type) {
        List<Station> stationList = stations.get(type);
        return stationList != null && !stationList.isEmpty() ? stationList.get(0) : null;
    }
    
    public List<Station> getAllStations() {
        List<Station> allStations = new ArrayList<>();
        for (List<Station> stationList : stations.values()) {
            allStations.addAll(stationList);
        }
        return allStations;
    }
    
    public List<Station> getStationsByType(StationType type) {
        return stations.getOrDefault(type, new ArrayList<>());
    }

    public void clearStations() {
        for (List<Station> stationList : stations.values()) {
            stationList.clear();
        }
        System.out.println("[StationManager] Cleared all stations");
    }
}
