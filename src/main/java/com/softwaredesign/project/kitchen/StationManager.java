package com.softwaredesign.project.kitchen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StationManager {
    private static StationManager instance;
    private final Map<Class<? extends Station>, List<Station>> stationRegistry;

    private StationManager() {
        stationRegistry = new HashMap<>();
    }

    public static StationManager getInstance() {
        if (instance == null) {
            instance = new StationManager();
        }
        return instance;
    }

    public void registerStation(Station station) {
        Class<? extends Station> stationType = station.getClass();
        stationRegistry.computeIfAbsent(stationType, k -> new ArrayList<>()).add(station);
    }

    public void unregisterStation(Station station) {
        Class<? extends Station> stationType = station.getClass();
        if (stationRegistry.containsKey(stationType)) {
            stationRegistry.get(stationType).remove(station);
        }
    }

    public Station findAvailableStation(Class<? extends Station> stationType) {
        List<Station> stations = stationRegistry.get(stationType);
        if (stations != null) {
            return stations.stream()
                .filter(Station::canAcceptRecipe)
                .findFirst()
                .orElse(null);
        }
        return null;
    }
}
