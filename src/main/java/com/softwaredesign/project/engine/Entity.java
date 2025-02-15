package com.softwaredesign.project.engine;

/**
 * Interface for game entities that can be managed by the game engine.
 * Separates read and write operations to prevent state conflicts during updates.
 */
public interface Entity {
    /**
     * Read phase of the update cycle.
     * During this phase, entities should only read state and perform calculations.
     * No state modifications should occur here.
     */
    void readState();
    
    /**
     * Write phase of the update cycle.
     * During this phase, entities can update their state based on calculations
     * from the read phase.
     */
    void writeState();

    /** 
     * Destructor to de-register the entity from the engine. (thanks ruan!)
     */


}
