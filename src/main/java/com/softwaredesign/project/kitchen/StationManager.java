package com.softwaredesign.project.kitchen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.softwaredesign.project.engine.GameEngine;
import com.softwaredesign.project.orderfulfillment.CollectionPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StationManager{
    private static final Logger logger = LoggerFactory.getLogger(StationManager.class);
    private Map<StationType, List<Station>> stations;
    private CollectionPoint collectionPoint;
    
    public StationManager(CollectionPoint collectionPoint) {
        this.stations = new HashMap<>();
        this.collectionPoint = collectionPoint;
        
        // Initialize empty lists for each station type
        for (StationType type : StationType.values()) {
            stations.put(type, new ArrayList<>());
        }
    }
    
    /**
     * Adds a station to the manager
     * @param station The station to add
     */
    public void addStation(Station station) {
        StationType type = station.getType();
        if (!stations.containsKey(type)) {
            stations.put(type, new ArrayList<>());
        }
        stations.get(type).add(station);
        
        // Ensure the station uses the same CollectionPoint instance
        station.setCollectionPoint(collectionPoint);
        GameEngine.getInstance().registerEntity(station);
        logger.info("[StationManager] Added station: " + type + 
            " (Total: " + stations.get(type).size() + ")");
    }
    
    /**
     * Gets a list of all stations of a specific type
     * @param type The station type to filter by
     * @return List of stations of the specified type
     */
    public List<Station> getStationsByType(StationType type) {
        return new ArrayList<>(stations.getOrDefault(type, new ArrayList<>()));
    }
    
    /**
     * Gets a station of the specified type
     * @param type The type of station to get
     * @return A station of the specified type, or null if none exists
     */
    public Station getStation(StationType type) {
        List<Station> stationsOfType = stations.get(type);
        if (stationsOfType != null && !stationsOfType.isEmpty()) {
            return stationsOfType.get(0);
        }
        return null;
    }
    
    /**
     * Gets a list of all stations
     * @return List of all stations
     */
    public List<Station> getAllStations() {
        List<Station> allStations = new ArrayList<>();
        for (List<Station> stationList : stations.values()) {
            allStations.addAll(stationList);
        }
        return allStations;
    }
    
    /**
     * Gets the CollectionPoint used by this StationManager
     * @return The CollectionPoint instance
     */
    public CollectionPoint getCollectionPoint() {
        return collectionPoint;
    }

    public void clearStations() {
        for (List<Station> stationList : stations.values()) {
            stationList.clear();
        }
        logger.info("[StationManager] Cleared all stations");
    }
}
