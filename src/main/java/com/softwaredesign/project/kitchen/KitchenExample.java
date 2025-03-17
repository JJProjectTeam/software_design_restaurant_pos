// package com.softwaredesign.project.kitchen;

// import com.softwaredesign.project.engine.GameEngine;
// import com.softwaredesign.project.engine.Simulator;

// public class KitchenExample {
//     public static void main(String[] args) {
//         // Get the game engine instance
//         GameEngine engine = GameEngine.getInstance();
        
//         // Create and register the kitchen
//         Kitchen kitchen = new Kitchen();
//         engine.registerEntity(kitchen);
        
//         // Create a simulator to control the engine
//         Simulator simulator = new Simulator(engine);
        
//         // Submit three orders
//         kitchen.submitOrder(); // Order 1
//         kitchen.submitOrder(); // Order 2
//         kitchen.submitOrder(); // Order 3
        
//         logger.info("Starting kitchen simulation...");
//         logger.info("Each order needs 5 ticks for grilling and 3 ticks for plating");
        
//         // Run simulation for 20 ticks
//         for (int tick = 1; tick <= 20; tick++) {
//             logger.info("\nTick " + tick);
//             simulator.step();
//             kitchen.processOrders(); // Move orders between stations
//         }
        
//         logger.info("\nKitchen simulation complete!");
//     }
// }
