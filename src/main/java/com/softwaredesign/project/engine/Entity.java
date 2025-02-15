package com.softwaredesign.project.engine;

/**
 * Abstract base class for game entities that can be managed by the game engine.
 * Separates read and write operations to prevent state conflicts during updates.
 */
public abstract class Entity {
    /**
     * Read phase of the update cycle.
     * During this phase, entities should only read state and perform calculations.
     * No state modifications should occur here.
     */
    public abstract void readState();
    
    /**
     * Write phase of the update cycle.
     * During this phase, entities can update their state based on calculations
     * from the read phase.
     */
    public abstract void writeState();

    /**
     * Deregisters this entity from the game engine.
     * Call this method when the entity needs to be removed from the game.
     */
    public void destroy() {
        GameEngine.getInstance().deregisterEntity(this);
    }
}
