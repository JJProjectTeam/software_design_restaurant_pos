package com.softwaredesign.project.order;

import java.util.HashSet;
import java.util.Set;

import com.softwaredesign.project.inventory.Ingredient;

public class StationMapper {

    public void mapStationsToRecipe(Recipe recipe) {
        Set<Station> requiredStations = new HashSet<>();
        
        // Check each ingredient's required stations
        for (Ingredient ingredient : recipe.getIngredients()) {
            requiredStations.addAll(ingredient.getStations());
        }
        
        // Add stations in specific order (PREP -> GRILL -> PLATE)
        if (requiredStations.contains(Station.PREP)) {
            recipe.addStation(Station.PREP);
        }
        if (requiredStations.contains(Station.GRILL)) {
            recipe.addStation(Station.GRILL);
        }
        // Always add PLATE station last
        recipe.addStation(Station.PLATE);
    }
    
}
