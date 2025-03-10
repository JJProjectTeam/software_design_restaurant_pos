package com.softwaredesign.project.engine;

import com.softwaredesign.project.inventory.InventoryService;
import com.softwaredesign.project.kitchen.Kitchen;
import com.softwaredesign.project.kitchen.StationManager;
import com.softwaredesign.project.kitchen.StationType;
import com.softwaredesign.project.order.OrderManager;
import com.softwaredesign.project.orderfulfillment.CollectionPoint;
import com.softwaredesign.project.staff.Chef;
import com.softwaredesign.project.staff.ChefManager;
import com.softwaredesign.project.staff.chefstrategies.ChefStrategy;
import com.softwaredesign.project.staff.chefstrategies.DynamicChefStrategy;
import com.softwaredesign.project.staff.chefstrategies.SimpleChefStrategy;
import com.softwaredesign.project.staff.chefstrategies.LongestQueueFirstStrategy;
import com.softwaredesign.project.staff.chefstrategies.OldestOrderFirstStrategy;

/**
 * A simulator class that sets up and runs the kitchen simulation.
 */
public class KitchenSimulator {
    private GameEngine gameEngine;
    private Kitchen kitchen;
    private OrderManager orderManager;
    private CollectionPoint collectionPoint;
    private InventoryService inventoryService;
    private ChefManager chefManager;
    private int stepCount;
    
    public KitchenSimulator() {
        this(null);
    }
    
    public KitchenSimulator(InventoryService inventoryService) {
        // Get the game engine instance
        gameEngine = GameEngine.getInstance();
        
        // Store inventory service
        this.inventoryService = inventoryService;
        
        // Initialize step counter
        this.stepCount = 0;
        
        // Initialize components
        collectionPoint = new CollectionPoint();
        kitchen = new Kitchen(null, collectionPoint, null); // Temporarily set orderManager to null
        
        // Create the chef manager
        chefManager = new ChefManager();
        gameEngine.registerEntity(chefManager);
        
        // Now create OrderManager with the StationManager from the kitchen
        orderManager = new OrderManager(collectionPoint, kitchen.getStationManager());
        
        // Set the orderManager in the kitchen
        kitchen.setOrderManager(orderManager);
        
        // Create and assign chefs
        createAndAssignChefs();
    }
    
    private void createAndAssignChefs() {
        // Get the station manager
        StationManager stationManager = kitchen.getStationManager();
        
        // Create different chef strategies to use in simulation
        ChefStrategy dynamicStrategy = new DynamicChefStrategy(stationManager);
        ChefStrategy simpleStrategy = new SimpleChefStrategy();
        ChefStrategy longestQueueStrategy = new LongestQueueFirstStrategy();
        ChefStrategy oldestOrderStrategy = new OldestOrderFirstStrategy();
        
        // Create chefs with different strategies and meaningful names
        Chef chef1 = new Chef("Mario", 15.0, 1.0, dynamicStrategy, stationManager); // Dynamic strategy
        Chef chef2 = new Chef("Luigi", 18.0, 0.8, simpleStrategy, stationManager); // Simple strategy
        Chef chef3 = new Chef("Peach", 17.0, 0.9, longestQueueStrategy, stationManager); // Longest queue strategy
        Chef chef4 = new Chef("Toad", 16.0, 1.1, oldestOrderStrategy, stationManager); // Oldest order strategy
        
        // Add the chefs to the chef manager
        chefManager.addChef(chef1);
        chefManager.addChef(chef2);
        chefManager.addChef(chef3);
        chefManager.addChef(chef4);
        
        // Assign chefs to all station types so they can work anywhere
        for (StationType stationType : StationType.values()) {
            kitchen.assignChefToStation(chef1, stationType);
            kitchen.assignChefToStation(chef2, stationType);
            kitchen.assignChefToStation(chef3, stationType);
            kitchen.assignChefToStation(chef4, stationType);
        }
        
        // Have chefs choose their initial stations
        chef1.chooseNextStation();
        chef2.chooseNextStation();
        chef3.chooseNextStation();
        chef4.chooseNextStation();
        
        System.out.println("Chefs assigned to kitchen with different strategies:");
        System.out.println("- Mario: Dynamic Strategy - prioritizes stations based on task urgency and dependencies");
        System.out.println("- Luigi: Simple Strategy - prioritizes stations with assigned recipes");
        System.out.println("- Peach: Longest Queue Strategy - prioritizes stations with the most pending orders");
        System.out.println("- Toad: Oldest Order Strategy - prioritizes stations with the oldest pending orders");
    }
    
    public void start() {
        // Start the game engine
        gameEngine.start();
    }
    
    public void stop() {
        // Stop the game engine
        gameEngine.stop();
    }
    
    /**
     * Get the kitchen instance
     * @return the kitchen
     */
    public Kitchen getKitchen() {
        return kitchen;
    }
    
    /**
     * Get the order manager
     * @return the order manager
     */
    public OrderManager getOrderManager() {
        return orderManager;
    }
    
    /**
     * Get the collection point
     * @return the collection point
     */
    public CollectionPoint getCollectionPoint() {
        return collectionPoint;
    }
    
    /**
     * Get the chef manager
     * @return the chef manager
     */
    public ChefManager getChefManager() {
        return chefManager;
    }
    
    public void step() {
        // Step the game engine
        gameEngine.step();
        
        // Check for completed orders
        checkCompletedOrders();
        
        // Increment step counter
        stepCount++;
    }
    
    /**
     * Get the current step count
     * @return the current step count
     */
    public int getStepCount() {
        return stepCount;
    }
    
    private void checkCompletedOrders() {
        while (collectionPoint.hasReadyOrders()) {
            System.out.println("Order completed and ready for pickup!");
            collectionPoint.collectNextOrder();
        }
    }
    
    public InventoryService getInventoryService() {
        return inventoryService;
    }
}
