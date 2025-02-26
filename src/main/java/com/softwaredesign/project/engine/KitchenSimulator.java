package com.softwaredesign.project.engine;

import com.softwaredesign.project.inventory.InventoryService;
import com.softwaredesign.project.kitchen.Kitchen;
import com.softwaredesign.project.kitchen.StationManager;
import com.softwaredesign.project.kitchen.StationType;
import com.softwaredesign.project.order.OrderManager;
import com.softwaredesign.project.orderfulfillment.CollectionPoint;
import com.softwaredesign.project.staff.Chef;
import com.softwaredesign.project.staff.chefstrategies.ChefStrategy;
import com.softwaredesign.project.staff.chefstrategies.SimpleChefStrategy;

/**
 * A simulator class that sets up and runs the kitchen simulation.
 */
public class KitchenSimulator {
    private GameEngine gameEngine;
    private Kitchen kitchen;
    private OrderManager orderManager;
    private CollectionPoint collectionPoint;
    private InventoryService inventoryService;
    
    public KitchenSimulator() {
        this(null);
    }
    
    public KitchenSimulator(InventoryService inventoryService) {
        // Get the game engine instance
        gameEngine = GameEngine.getInstance();
        
        // Store inventory service
        this.inventoryService = inventoryService;
        
        // Initialize components
        collectionPoint = new CollectionPoint();
        kitchen = new Kitchen(null, collectionPoint); // Temporarily set orderManager to null
        
        // Now create OrderManager with the StationManager from the kitchen
        orderManager = new OrderManager(collectionPoint, kitchen.getStationManager());
        
        // Set the orderManager in the kitchen
        kitchen.setOrderManager(orderManager);
        
        // Create and assign chefs
        createAndAssignChefs();
    }
    
    private void createAndAssignChefs() {
        // Create a simple chef strategy
        ChefStrategy simpleStrategy = new SimpleChefStrategy();
        
        // Create chefs and assign them to stations
        Chef chef1 = new Chef(15.0, 1.0, simpleStrategy, kitchen.getStationManager());
        Chef chef2 = new Chef(18.0, 1.2, simpleStrategy, kitchen.getStationManager());
        
        // Assign chefs to stations
        kitchen.assignChefToStation(chef1, StationType.PREP);
        kitchen.assignChefToStation(chef2, StationType.GRILL);
        
        // Have chefs choose their stations
        chef1.chooseNextStation();
        chef2.chooseNextStation();
    }
    
    public void start() {
        // Start the game engine
        gameEngine.start();
    }
    
    public void stop() {
        // Stop the game engine
        gameEngine.stop();
    }
    
    public void step() {
        // Step the game engine
        gameEngine.step();
        
        // Check for completed orders
        checkCompletedOrders();
    }
    
    private void checkCompletedOrders() {
        while (collectionPoint.hasReadyOrders()) {
            System.out.println("Order completed and ready for pickup!");
            collectionPoint.collectNextOrder();
        }
    }
    
    public Kitchen getKitchen() {
        return kitchen;
    }
    
    public OrderManager getOrderManager() {
        return orderManager;
    }
    
    public CollectionPoint getCollectionPoint() {
        return collectionPoint;
    }
    
    public InventoryService getInventoryService() {
        return inventoryService;
    }
}
