package com.softwaredesign.project.staff;

import java.util.ArrayList;
import java.util.List;

import com.softwaredesign.project.engine.Entity;

/**
 * Manages a collection of chefs and coordinates their work assignments.
 * This class implements Entity so it can be registered with the GameEngine.
 */
public class ChefManager extends Entity {
    private List<Chef> chefs;
    
    public ChefManager() {
        chefs = new ArrayList<>();
    }
    
    /**
     * Adds a chef to be managed by this manager
     * @param chef The chef to add
     */
    public void addChef(Chef chef) {
        chefs.add(chef);
    }
    
    /**
     * Removes a chef from this manager
     * @param chef The chef to remove
     */
    public void removeChef(Chef chef) {
        chefs.remove(chef);
    }
    
    /**
     * Get a list of all chefs managed by this manager
     * @return A list of all chefs
     */
    public List<Chef> getAllChefs() {
        return new ArrayList<>(chefs);
    }
    
    @Override
    public void readState() {
        // Nothing to read in the first phase
    }
    
    @Override
    public void writeState() {
        logger.info("\n=== CHEF MANAGER: CHECKING FOR WORK FOR " + chefs.size() + " CHEFS ===");
        
        // In the write phase, have each chef check for work
        for (Chef chef : chefs) {
            logger.info("\nCHEF " + chef.getName() + " STATUS:");
            logger.info("  - Current station: " + 
                (chef.getCurrentStation() != null ? chef.getCurrentStation().getType() : "NONE"));
            logger.info("  - Is working: " + chef.isWorking());
            logger.info("  - Assigned stations: " + chef.getAssignedStations().size());
            
            // Check for work
            chef.checkForWork();
            
            // Log status after checking
            logger.info("AFTER CHECK: " + chef.getName() + " at " + 
                (chef.getCurrentStation() != null ? chef.getCurrentStation().getType() : "NONE") + 
                ", working: " + chef.isWorking());
        }
    }
}
