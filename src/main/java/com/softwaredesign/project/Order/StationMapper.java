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
        
        // Add stations in specific order based on backlog size
        Station prepStation = new Station();
        Station grillStation = new Station();
        Station plateStation = new Station();

        if (requiredStations.contains(prepStation)) {
            recipe.addStation(prepStation);
        }
        if (requiredStations.contains(grillStation)) {
            recipe.addStation(grillStation); 
        }
        // Always add plate station last
        recipe.addStation(plateStation);
    }
}
