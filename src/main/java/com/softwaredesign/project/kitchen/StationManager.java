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
    }
    

    public void addStation(Station station) {
        stations.put(station.getType(), station);
        GameEngine.getInstance().registerEntity(station);
    }
    
    public Station getStation(StationType type) {
        return stations.get(type);
    }
    
    public List<Station> getAllStations() {
        return new ArrayList<>(stations.values());
    }
    
    /**
     * Returns a list of stations of the specified type.
     * Since we currently only have one station per type, this will return a list with at most one station.
     * 
     * @param type The station type to filter by
     * @return List of stations of the specified type
     */
    public List<Station> getStationsByType(StationType type) {
        List<Station> result = new ArrayList<>();
        Station station = stations.get(type);
        if (station != null) {
            result.add(station);
        }
        return result;
    }
}
