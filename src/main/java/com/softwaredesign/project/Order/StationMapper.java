package com.softwaredesign.project.order;

import java.util.HashSet;
import java.util.Set;

import com.softwaredesign.project.inventory.Ingredient;
import com.softwaredesign.project.kitchen.StationManager;

public class StationMapper {
    private StationManager stationManager;

    public StationMapper(StationManager stationManager) {
        this.stationManager = stationManager;
    }

    public void mapStationsToRecipe(Recipe recipe) {
        Set<StationType> requiredStationTypes = new HashSet<>();
        
        for (Ingredient ingredient : recipe.getIngredients()) {
            requiredStationTypes.addAll(ingredient.getStationTypes());
        }
        
        // Add required stations in specific order
        if (requiredStationTypes.contains(StationType.PREP)) {
            recipe.addStation(stationManager.getStation(StationType.PREP));
        }
        if (requiredStationTypes.contains(StationType.GRILL)) {
            recipe.addStation(stationManager.getStation(StationType.GRILL));
        }
        // Always add plate station last
        recipe.addStation(stationManager.getStation(StationType.PLATE));
    }
}
