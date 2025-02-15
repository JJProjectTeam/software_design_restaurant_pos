package com.softwaredesign.project.kitchen;

import com.softwaredesign.project.engine.GameEngine;

public class KitchenExample {
    public static void main(String[] args) {
        // Create the game engine
        GameEngine engine = new GameEngine();

        // Create kitchen stations
        PrepStation prepStation = new PrepStation();
        CookStation cookStation = new CookStation();

        // Create a recipe and define its workflow
        Recipe burger = new Recipe("Burger");
        burger.addWorkflowStep(PrepStation.class);  // First goes to prep
        burger.addWorkflowStep(CookStation.class);  // Then goes to cook

        // Register all entities with the engine
        engine.registerEntity(prepStation);
        engine.registerEntity(cookStation);
        engine.registerEntity(burger);

        // Start the engine
        engine.start();

        System.out.println("Initial burger workflow: needs " + burger.getNextRequiredStation().getSimpleName());

        // Assign burger to prep station
        prepStation.assignRecipe(burger);

        // First tick - burger will be prepped
        System.out.println("Tick 1 - Prepping burger");
        engine.step();

        // Second tick - try to assign to both stations, only cook station will accept
        System.out.println("Tick 2 - Attempting to assign to stations");
        prepStation.assignRecipe(burger);  // Will fail because burger needs cook station
        cookStation.assignRecipe(burger);  // Will succeed because it's the next required station
        engine.step();

        // Third tick - burger will be cooked
        System.out.println("Tick 3 - Cooking burger");
        engine.step();

        System.out.println("Burger preparation complete: " + burger.isComplete());
    }
}
