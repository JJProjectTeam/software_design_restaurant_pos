package com.softwaredesign.project.model.engine;

import java.util.List;
import java.util.ArrayList;

/**
 * Core engine class that manages entities and updates them in a two-phase approach.
 * Implemented as a singleton since only one engine instance should exist.
 */
public class GameEngine {
    private static GameEngine instance = null;
    private final List<Entity> entities;
    private boolean isRunning;

    /**
     * Private constructor to prevent direct instantiation.
     */
    private GameEngine() {
        this.entities = new ArrayList<>();
        this.isRunning = false;
    }

    /**
     * Gets the singleton instance of the GameEngine.
     * @return The singleton GameEngine instance
     */
    public static GameEngine getInstance() {
        if (instance == null) {
            instance = new GameEngine();
        }
        return instance;
    }

    /**
     * Registers a new entity with the engine.
     * @param entity The entity to register
     */
    public void registerEntity(Entity entity) {
        entities.add(entity);
    }

    /**
     * Deregisters an entity from the engine.
     * @param entity The entity to deregister
     */
    public void deregisterEntity(Entity entity) {
        entities.remove(entity);
    }

    /**
     * Starts the engine.
     */
    public void start() {
        isRunning = true;
    }

    /**
     * Stops the engine.
     */
    public void stop() {
        isRunning = false;
    }

    /**
     * Performs a single step of the simulation in a two-phase approach:
     * 1. Read phase: All entities read state and perform calculations
     * 2. Write phase: All entities update their state
     */
    public void step() {
        if (!isRunning) return;

        // Read phase
        for (Entity entity : entities) {
            entity.readState();
        }

        // Write phase
        for (Entity entity : entities) {
            entity.writeState();
        }
    }

    /**
     * @return true if the engine is currently running
     */
    public boolean isRunning() {
        return isRunning;
    }
}
