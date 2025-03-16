package com.softwaredesign.project;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import com.softwaredesign.project.controller.*;
import com.softwaredesign.project.customer.DineInCustomer;
import com.softwaredesign.project.inventory.Inventory;
import com.softwaredesign.project.inventory.InventoryService;
import com.softwaredesign.project.kitchen.Kitchen;
import com.softwaredesign.project.kitchen.Station;
import com.softwaredesign.project.kitchen.StationManager;
import com.softwaredesign.project.kitchen.StationType;
import com.softwaredesign.project.view.*;
import com.softwaredesign.project.mediator.RestaurantViewMediator;
import com.softwaredesign.project.menu.BurgerRecipe;
import com.softwaredesign.project.menu.KebabRecipe;
import com.softwaredesign.project.menu.Menu;
import com.softwaredesign.project.order.Order;
import com.softwaredesign.project.order.OrderManager;
import com.softwaredesign.project.orderfulfillment.CollectionPoint;
import com.softwaredesign.project.orderfulfillment.SeatingPlan;
import com.softwaredesign.project.orderfulfillment.Table;
import com.softwaredesign.project.staff.Chef;
import com.softwaredesign.project.staff.Waiter;
import com.softwaredesign.project.engine.GameEngine;
import com.softwaredesign.project.engine.Entity;
import com.softwaredesign.project.staff.ChefManager;
import com.softwaredesign.project.staff.chefstrategies.ChefStrategy;
import com.softwaredesign.project.staff.chefstrategies.DynamicChefStrategy;
import com.softwaredesign.project.staff.chefstrategies.SimpleChefStrategy;
import com.softwaredesign.project.orderfulfillment.FloorManager;
import com.softwaredesign.project.demo.DemoHelper;

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
    // Core components
    private GameEngine gameEngine;
    private Kitchen kitchen;
    private OrderManager orderManager;
    private CollectionPoint collectionPoint;
    private Inventory inventory;
    private ChefManager chefManager;
    private SeatingPlan seatingPlan;
    private Menu menu;
    private List<Chef> chefs = new ArrayList<>();
    private List<Waiter> waiters = new ArrayList<>();
    private List<Table> tables = new ArrayList<>();
    private FloorManager floorManager;
    private DemoHelper demoHelper;
    
    // Controllers and UI
    private RestaurantApplication app;
    private RestaurantViewMediator mediator;
    private ConfigurationController configController;
    private DiningRoomController diningRoomController;
    private KitchenController kitchenController;
    private InventoryController inventoryController;
    
    // Demo state
    private boolean hasStartedDemo = false;
    private int demoStep = 0;
    private final int DEMO_DELAY = 5;
    private int tickCount = 0;
    
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
                                setupDemoScenario();
                                
                                // Start the game engine
                                gameEngine.start();
                                
                                // Show dining room view and do initial update
                                app.showView(ViewType.DINING_ROOM);
                                Thread.sleep(100);
                            }

                            // Update demo instead of using hasStartedDemo and handleDemoStep
                            updateDemo();

                            // Step the game engine to update all entities
                            gameEngine.step();
                            
                            // Update views
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

    private void createEntitiesFromConfiguration() {
        System.out.println("[RestaurantDriver] Creating entities from configuration...");
        
        // Create core components
        this.collectionPoint = new CollectionPoint();
        this.inventory = configController.getInventory();
        this.seatingPlan = configController.getSeatingPlan();
        
        // Get StationManager from configuration controller if available, otherwise create a new one
        StationManager stationManager = configController.getStationManager();
        if (stationManager == null) {
            System.out.println("[RestaurantDriver] Creating new StationManager");
            stationManager = new StationManager(collectionPoint);
        } else {
            System.out.println("[RestaurantDriver] Using StationManager from configuration with " + 
                stationManager.getAllStations().size() + " stations");
            // Ensure it's using the same CollectionPoint
            System.out.println("[RestaurantDriver] Setting common CollectionPoint for all stations");
            for (Station station : stationManager.getAllStations()) {
                station.setCollectionPoint(collectionPoint);
            }
        }
        
        // Create kitchen with null OrderManager (will be set later)
        this.kitchen = new Kitchen(null, collectionPoint, stationManager);
        
        // Create ChefManager and register with GameEngine
        this.chefManager = new ChefManager();
        gameEngine.registerEntity(chefManager);
        
        // Create OrderManager with StationManager from kitchen and the common CollectionPoint
        this.orderManager = new OrderManager(collectionPoint, kitchen.getStationManager());
        
        // Set OrderManager in kitchen
        kitchen.setOrderManager(orderManager);
        
        // If no stations exist yet, create default stations
        if (stationManager.getAllStations().isEmpty()) {
            System.out.println("[RestaurantDriver] No stations found, creating default stations");
            createDefaultStations(stationManager);
        }
        
        // Ensure all stations have proper references
        for (Station station : stationManager.getAllStations()) {
            station.setKitchen(kitchen);
            // Double-check the CollectionPoint is set correctly
            station.setCollectionPoint(collectionPoint);
            System.out.println("[RestaurantDriver] Set kitchen reference for station: " + station.getType());
        }
        
        // Create and assign chefs with strategies
        setupChefs();
        
        // Create and assign waiters
        setupWaiters();
        
        // Create the FloorManager (new entity)
        this.floorManager = new FloorManager(seatingPlan, waiters, orderManager);
        
        // Set the CollectionPoint in the FloorManager to enable order delivery
        floorManager.setCollectionPoint(collectionPoint);
        System.out.println("[RestaurantDriver] Set CollectionPoint in FloorManager for order delivery");
        
        // Register entities with GameEngine
        registerEntitiesWithGameEngine();
        
        // Create DemoHelper
        this.demoHelper = new DemoHelper(floorManager, kitchen, orderManager, seatingPlan, chefManager, inventory);
        
        System.out.println("[RestaurantDriver] Entity creation complete");
    }
    
    /**
     * Creates default stations if none are provided by the configuration
     */
    private void createDefaultStations(StationManager stationManager) {
        System.out.println("[RestaurantDriver] Creating default stations...");
        
        // Create at least one station of each type
        Station prepStation = new Station(StationType.PREP, collectionPoint);
        prepStation.setKitchen(kitchen);
        System.out.println("[RestaurantDriver] Created PREP station");
        
        Station grillStation = new Station(StationType.GRILL, collectionPoint);
        grillStation.setKitchen(kitchen);
        System.out.println("[RestaurantDriver] Created GRILL station");
        
        Station plateStation = new Station(StationType.PLATE, collectionPoint);
        plateStation.setKitchen(kitchen);
        System.out.println("[RestaurantDriver] Created PLATE station");
        
        // Add stations to station manager
        stationManager.addStation(prepStation);
        System.out.println("[RestaurantDriver] Added PREP station to StationManager");
        
        stationManager.addStation(grillStation);
        System.out.println("[RestaurantDriver] Added GRILL station to StationManager");
        
        stationManager.addStation(plateStation);
        System.out.println("[RestaurantDriver] Added PLATE station to StationManager");
        
        // Verify stations were added
        System.out.println("[RestaurantDriver] Verifying stations...");
        for (StationType type : StationType.values()) {
            List<Station> stations = stationManager.getStationsByType(type);
            System.out.println("[RestaurantDriver] " + type + " stations: " + stations.size());
        }
        
        System.out.println("[RestaurantDriver] Created default stations: PREP, GRILL, PLATE");
    }

    private void setupChefs() {
        // Get station manager from kitchen
        StationManager stationManager = kitchen.getStationManager();
        
        // Create strategies
        ChefStrategy dynamicStrategy = new DynamicChefStrategy(stationManager);
        ChefStrategy simpleStrategy = new SimpleChefStrategy();
        
        // Create chefs from configuration
        List<Chef> configChefs = configController.getChefs();
        
        // If no chefs from configuration, create default chefs
        if (configChefs == null || configChefs.isEmpty()) {
            System.out.println("[RestaurantDriver] No chefs found in configuration, creating default chefs");
            configChefs = createDefaultChefs(stationManager);
        }
        
        for (int i = 0; i < configChefs.size(); i++) {
            Chef chef = configChefs.get(i);
            // Assign alternating strategies
            chef.setWorkStrategy(i % 2 == 0 ? dynamicStrategy : simpleStrategy);
            
            // Add to chef manager
            chefManager.addChef(chef);
            
            // Add to local list
            chefs.add(chef);
            
            // Assign to all station types
            for (StationType stationType : StationType.values()) {
                // Check if there are stations of this type
                List<Station> stationsOfType = stationManager.getStationsByType(stationType);
                if (stationsOfType != null && !stationsOfType.isEmpty()) {
                    // Assign chef to this station type
                    chef.assignToStation(stationType);
                    System.out.println("[RestaurantDriver] Assigned chef " + chef.getName() + 
                        " to station type " + stationType);
                } else {
                    System.out.println("[RestaurantDriver] Warning: No stations of type " + 
                        stationType + " found for chef " + chef.getName());
                }
            }
            
            // Don't choose initial station - let the chef's strategy decide where to go based on work
            System.out.println("Chef " + chef.getName() + " initialized with " + 
                (i % 2 == 0 ? "dynamic" : "simple") + " strategy");
        }
    }
    
    /**
     * Creates default chefs if none are provided by the configuration
     */
    private List<Chef> createDefaultChefs(StationManager stationManager) {
        List<Chef> defaultChefs = new ArrayList<>();
        
        // Create two chefs with different strategies
        ChefStrategy dynamicStrategy = new DynamicChefStrategy(stationManager);
        ChefStrategy simpleStrategy = new SimpleChefStrategy();
        
        Chef chef1 = new Chef("Default Chef 1", 15.0, 1.0, dynamicStrategy, stationManager);
        Chef chef2 = new Chef("Default Chef 2", 18.0, 0.8, simpleStrategy, stationManager);
        
        defaultChefs.add(chef1);
        defaultChefs.add(chef2);
        
        System.out.println("[RestaurantDriver] Created default chefs: " + chef1.getName() + ", " + chef2.getName());
        
        return defaultChefs;
    }

    /**
     * Sets up waiters for the restaurant
     * Creates default waiters and assigns them to the waiters list
     */
    private void setupWaiters() {
        System.out.println("[RestaurantDriver] Setting up waiters...");
        
        // Get menu from configuration controller or create a new one
        Menu menu = configController.getMenu();
        if (menu == null) {
            System.out.println("[RestaurantDriver] Warning: No menu found for waiters");
            // Create a simple menu if needed
            if (inventory != null) {
                menu = new Menu(inventory);
                System.out.println("[RestaurantDriver] Created default menu for waiters");
            }
        }
        
        // Create default waiters
        Waiter waiter1 = new Waiter(12.0, 1.0, orderManager, menu);
        waiters.add(waiter1);
        
        Waiter waiter2 = new Waiter(14.0, 0.9, orderManager, menu);
        waiters.add(waiter2);
        
        Waiter waiter3 = new Waiter(10.0, 1.2, orderManager, menu);
        waiters.add(waiter3);
        
        System.out.println("[RestaurantDriver] Created " + waiters.size() + " waiters");
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
        
        // Register FloorManager
        gameEngine.registerEntity(floorManager);
        System.out.println("[RestaurantDriver] Registered FloorManager with GameEngine");
        
        // Register all stations
        StationManager stationManager = kitchen.getStationManager();
        if (stationManager != null) {
            for (Station station : stationManager.getAllStations()) {
                gameEngine.registerEntity(station);
                System.out.println("[RestaurantDriver] Registered Station " + station.getType() + " with GameEngine");
            }
        }
        
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

    private void setupDemoScenario() {
        // Initialize the demo through our DemoHelper
        demoHelper.setupChefStrategies();
    }

    // Replace the handleDemoStep method with this method that delegates to DemoHelper
    private void updateDemo() {
        // Update tickCount
        tickCount++;
        
        // Use DemoHelper to update the demo
        if (demoHelper != null) {
            demoHelper.update(tickCount);
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
 * restart
 * 
 * speed multiplier
 * 
 * 5 tick delay
 * default chef goes on holiday sometimes
 *
 */