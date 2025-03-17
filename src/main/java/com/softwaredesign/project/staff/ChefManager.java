package com.softwaredesign.project.staff;

import java.util.ArrayList;
import java.util.List;

import com.softwaredesign.project.engine.Entity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages a collection of chefs and coordinates their work assignments.
 * This class implements Entity so it can be registered with the GameEngine.
 */
public class ChefManager extends Entity {
    private static final Logger logger = LoggerFactory.getLogger(ChefManager.class);
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
        
        // First, periodically check for and fix inconsistencies
        if (isConsistencyCheckNeeded()) {
            checkAndFixConsistencies();
        }
        
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

    // Track tick count to perform consistency checks periodically
    private int tickCount = 0;
    private static final int CONSISTENCY_CHECK_INTERVAL = 10; // Check every 10 ticks

    private boolean isConsistencyCheckNeeded() {
        tickCount++;
        return tickCount % CONSISTENCY_CHECK_INTERVAL == 0;
    }

    /**
     * Checks for and fixes inconsistencies between chefs and stations
     */
    private void checkAndFixConsistencies() {
        logger.info("\n=== CHEF MANAGER: PERFORMING CONSISTENCY CHECK ===");
        
        // Have each chef check its own consistency
        for (Chef chef : chefs) {
            chef.checkStationConsistency();
        }
        
        // Check for chefs that are in inconsistent states
        int inconsistenciesFixed = 0;
        for (Chef chef : chefs) {
            if (chef.isWorking() && chef.getCurrentStation() == null) {
                logger.info("[CONSISTENCY-ERROR] Chef " + chef.getName() + 
                    " is marked as working but has no current station");
                chef.setWorking(false);
                inconsistenciesFixed++;
            }
        }
        
        // Log the results
        if (inconsistenciesFixed > 0) {
            logger.info("[CONSISTENCY-CHECK] Fixed " + inconsistenciesFixed + " inconsistencies");
        } else {
            logger.info("[CONSISTENCY-CHECK] No inconsistencies found");
        }
    }
}
