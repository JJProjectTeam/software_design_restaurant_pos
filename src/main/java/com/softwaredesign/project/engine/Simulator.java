package com.softwaredesign.project.engine;

/**
 * Simple simulator that can run the engine step by step.
 */
public class Simulator {
    private final GameEngine engine;

    public Simulator(GameEngine engine) {
        this.engine = engine;
    }

    /**
     * Starts the engine.
     */
    public void start() {
        engine.start();
    }

    /**
     * Stops the engine.
     */
    public void stop() {
        engine.stop();
    }

    /**
     * Performs a single step of the simulation.
     * This method can be called at any interval required by the application.
     */
    public void step() {
        engine.step();
    }

    /**
     * Performs multiple steps of the simulation.
     * @param steps number of steps to perform
     */
    public void steps(int steps) {
        for (int i = 0; i < steps; i++) {
            step();
        }
    }
}
