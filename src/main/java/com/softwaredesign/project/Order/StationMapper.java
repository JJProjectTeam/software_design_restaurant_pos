package com.softwaredesign.project.order;

import java.util.HashSet;
import java.util.Set;

import com.softwaredesign.project.inventory.Ingredient;

public class StationMapper {
    public void mapStationsToRecipe(Recipe recipe) {
        Set<StationType> requiredStationTypes = new HashSet<>();
        
        for (Ingredient ingredient : recipe.getIngredients()) {
            requiredStationTypes.addAll(ingredient.getStationTypes());
        }
        
        // Add required stations in specific order
        if (requiredStationTypes.contains(StationType.PREP)) {
            recipe.addStation(Station.getInstance(StationType.PREP));
        }
        if (requiredStationTypes.contains(StationType.GRILL)) {
            recipe.addStation(Station.getInstance(StationType.GRILL));
        }
        // Always add plate station last
        recipe.addStation(Station.getInstance(StationType.PLATE));
    }
}
