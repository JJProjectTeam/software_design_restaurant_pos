package com.softwaredesign.project;

import java.util.HashMap;
import java.util.List;

import com.softwaredesign.project.controller.*;
import com.softwaredesign.project.inventory.Inventory;
import com.softwaredesign.project.inventory.InventoryService;
import com.softwaredesign.project.kitchen.Kitchen;
import com.softwaredesign.project.view.*;
import com.softwaredesign.project.mediator.RestaurantViewMediator;
import com.softwaredesign.project.menu.Menu;
import com.softwaredesign.project.order.OrderManager;
import com.softwaredesign.project.orderfulfillment.SeatingPlan;
import com.softwaredesign.project.orderfulfillment.Table;
import com.softwaredesign.project.staff.Chef;
import com.softwaredesign.project.staff.Waiter;
import com.softwaredesign.project.engine.GameEngine;
import com.softwaredesign.project.engine.Entity;
import com.softwaredesign.project.staff.ChefManager;

/**
 * Main driver class for the Restaurant POS system.
 * 
 * TICK SYSTEM IMPLEMENTATION:
 * --------------------------
 * This class integrates a tick-based game loop using the GameEngine class.
 * The tick system works as follows:
 * 
 * 1. The GameEngine maintains a list of Entity objects.
 * 2. Each tick, the GameEngine calls readState() and then writeState() on all registered entities.
 * 3. The readState() phase is for reading state and performing calculations without modifying state.
 * 4. The writeState() phase is for updating state based on calculations from the read phase.
 * 
 * Currently, the following classes implement Entity and can be registered with the GameEngine:
 * - Kitchen: Processes orders, assigns tasks to stations, and manages recipe flow
 * - Station: Handles cooking tasks at specific stations (PREP, GRILL, PLATE)
 * - ChefManager: Manages chefs and their work assignments
 * 
 * To extend the tick system to other components:
 * 1. Make the class extend Entity
 * 2. Implement readState() and writeState() methods
 * 3. Register the object with the GameEngine using gameEngine.registerEntity()
 * 
 * For example, to make OrderManager tick-based:
 * - Modify OrderManager to extend Entity
 * - Implement readState() to check for new orders
 * - Implement writeState() to process orders
 * - Register it with the GameEngine
 */
public class RestaurantDriver {
    private RestaurantApplication app;
    private RestaurantViewMediator mediator;
    private ConfigurationController configController;
    private DiningRoomController diningRoomController;
    private KitchenController kitchenController;
    private InventoryController inventoryController;

    private List<Waiter> waiters;
    private List<Chef> chefs;
    private Kitchen kitchen;
    private Menu menu;
    private OrderManager orderManager;
    private List<Table> tables;
    private Inventory inventory;
    private SeatingPlan seatingPlan;
    
    // Add GameEngine instance
    private GameEngine gameEngine;
    
    // Add ChefManager for managing chefs
    private ChefManager chefManager;
    
    public RestaurantDriver() {
        try{
            this.app = new RestaurantApplication();
            this.mediator = RestaurantViewMediator.getInstance();
            
            // Get the GameEngine instance
            this.gameEngine = GameEngine.getInstance();
            
            // Set this driver instance in the application for restart functionality
            this.app.setDriver(this);
        }
        catch (Exception e){
            System.err.println("[RestaurantDriver] Fatal error running application: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void start() {
        try {
            System.out.println("[RestaurantDriver] Starting application...");
            
            initializeConfiguration();
            app.showView(ViewType.WELCOME);

            // Create a timer for the game loop
            java.util.Timer gameTimer = new java.util.Timer();
            gameTimer.scheduleAtFixedRate(new java.util.TimerTask() {
                @Override
                public void run() {
                    try {
                        if (configController.isConfigurationComplete()) {
                            // Only run once when configuration is complete
                            if (kitchen == null) {
                                System.out.println("[RestaurantDriver] Configuration complete, initializing game");
                                createEntitiesFromConfiguration();
                                initializeOperation();
                                
                                // Start the game engine
                                gameEngine.start();
                                
                                // Show dining room view and do initial update
                                app.showView(ViewType.DINING_ROOM);
                                Thread.sleep(100); // Small delay for view initialization
                            }

                            // Step the game engine to update all entities
                            gameEngine.step();
                            
                            // Update views one at a time to avoid concurrent modification
                            synchronized(mediator) {
                                diningRoomController.updateView();
                                Thread.sleep(50);
                                kitchenController.updateView();
                                Thread.sleep(50);
                                inventoryController.updateView();
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("[RestaurantDriver] Error in game loop: " + e.getMessage());
                        e.printStackTrace();
                        gameTimer.cancel();
                    }
                }
            }, 0, 1000); // Check every second

            // This will block until the window is closed
            app.run();
            
            // Cleanup when window closes
            gameTimer.cancel();
            gameEngine.stop();
            System.out.println("[RestaurantDriver] Application terminated");

        } catch (Exception e) {
            System.err.println("[RestaurantDriver] Fatal error running application: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void initializeConfiguration() {
        configController = new ConfigurationController();
        mediator.registerController("Configuration", configController);
    }

    private void waitForConfiguration() {
        // This could be improved with proper synchronization
        while (!configController.isConfigurationComplete()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public void createEntitiesFromConfiguration(){
        this.waiters = configController.getWaiters();
        this.chefs = configController.getChefs();
        this.kitchen = configController.getKitchen();
        this.menu = configController.getMenu();
        this.orderManager = configController.getOrderManager();
        this.inventory = configController.getInventory();
        this.seatingPlan = configController.getSeatingPlan();
        System.out.println("SEATINGPLAN: "+ seatingPlan.getAllTables().size());
        
        // Create a ChefManager to manage chefs
        this.chefManager = new ChefManager();
        for (Chef chef : chefs) {
            chefManager.addChef(chef);
        }
        
        // Register entities with the GameEngine
        registerEntitiesWithGameEngine();
    }
    
    /**
     * Register all entities with the GameEngine for the tick system.
     * Note: Only classes that extend Entity can be registered.
     * To register other classes, they would need to be modified to extend Entity
     * and implement readState() and writeState() methods.
     */
    private void registerEntitiesWithGameEngine() {
        System.out.println("[RestaurantDriver] Registering entities with GameEngine");
        
        // Register Kitchen if it extends Entity
        if (kitchen instanceof Entity) {
            gameEngine.registerEntity((Entity) kitchen);
            System.out.println("[RestaurantDriver] Registered Kitchen with GameEngine");
        } else {
            System.out.println("[RestaurantDriver] Kitchen does not extend Entity, cannot register");
        }
        
        // Register ChefManager (which extends Entity)
        gameEngine.registerEntity(chefManager);
        System.out.println("[RestaurantDriver] Registered ChefManager with GameEngine");
        
        // Note: The following classes would need to be modified to extend Entity
        // before they can be registered with the GameEngine:
        // - OrderManager
        // - Chef (individual chefs)
        // - Waiter
        // - Table
        // - Inventory
        
        System.out.println("[RestaurantDriver] Entity registration complete");
    }

    private void initializeOperation() {        
        // Create gameplay controllers with configured components
        diningRoomController = new DiningRoomController(
            configController.getSeatingPlan()
        );

        kitchenController = new KitchenController(
            kitchen
        );

        //TODO populate inventory
        inventoryController = new InventoryController(inventory);

        // Register gameplay controllers with mediator
        mediator.registerController("DiningRoom", diningRoomController);
        mediator.registerController("Kitchen", kitchenController);
        mediator.registerController("Inventory", inventoryController);
        
        System.out.println("[RestaurantDriver] Restaurant initialized and ready for operation");
    }
    
    public synchronized void passEntitiesToGamePlay(){
        // This method is now replaced by the GameEngine's step() method
        // which calls readState() and writeState() on all registered entities
        
        // We still update the views after the entities have been updated
        try {
            if (diningRoomController != null) {
                diningRoomController.updateView();
            }
            
            if (kitchenController != null) {
                kitchenController.updateView();
            }
            
            if (inventoryController != null) {
                inventoryController.updateView();
            }
        } catch (Exception e) {
            System.err.println("[RestaurantDriver] Error updating views: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Completely restarts the application by resetting all components and reinitializing
     */
    public synchronized void restart() {
        System.out.println("[RestaurantDriver] Performing full application restart");
        
        // Stop the game engine before restarting
        gameEngine.stop();
        
        // Stop any ongoing operations
        try {
            // Reset all entity references
            this.waiters = null;
            this.chefs = null;
            this.kitchen = null;
            this.menu = null;
            this.orderManager = null;
            this.tables = null;
            this.inventory = null;
            this.seatingPlan = null;
            
            // Reset controllers
            this.diningRoomController = null;
            this.kitchenController = null;
            this.inventoryController = null;
            
            // Reset configuration controller but keep the mediator
            this.configController = null;
            
            // Reinitialize configuration
            initializeConfiguration();
            
            // Tell the application to show the welcome view
            // Use SwingUtilities.invokeLater to ensure UI updates happen on the EDT
            javax.swing.SwingUtilities.invokeLater(() -> {
                app.showView(ViewType.WELCOME);
            });
            
            System.out.println("[RestaurantDriver] Application restart complete");
        } catch (Exception e) {
            System.err.println("[RestaurantDriver] Error during restart: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        RestaurantDriver driver = new RestaurantDriver();
        driver.start();
    }
}
//TODOS:
/*
 * make inventory + pass to controller and view
 * When im not sleepy - are we just updating views with what has changed, or repainting each time? 
 * Test updating funcionality in driver
 * 
 * Money system, both during game and at configuraiton
 * 
 * Some kind of recipeTypes, and an inventory config file that can be set up
 * 
 * In config file - set things like restuarant seats, initialbudget, chef/waiter charge strategy etc. 
 * 
 * Help menu
 *
 */