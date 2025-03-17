package com.softwaredesign.project;

import java.util.List;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.softwaredesign.project.controller.*;
import com.softwaredesign.project.inventory.Inventory;
import com.softwaredesign.project.kitchen.Kitchen;
import com.softwaredesign.project.kitchen.Station;
import com.softwaredesign.project.kitchen.StationManager;
import com.softwaredesign.project.kitchen.StationType;
import com.softwaredesign.project.view.*;
import com.softwaredesign.project.mediator.RestaurantViewMediator;
import com.softwaredesign.project.menu.Menu;
import com.softwaredesign.project.order.OrderManager;
import com.softwaredesign.project.orderfulfillment.CollectionPoint;
import com.softwaredesign.project.orderfulfillment.SeatingPlan;
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
import com.softwaredesign.project.staff.staffspeeds.BaseSpeed;
import com.softwaredesign.project.staff.staffspeeds.StimulantAddictDecorator;
import com.softwaredesign.project.staff.staffspeeds.ISpeedComponent;
import com.softwaredesign.project.inventory.InventoryStockTracker;

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
    private List<Chef> chefs = new ArrayList<>();
    private List<Waiter> waiters = new ArrayList<>();
    private FloorManager floorManager;
    private DemoHelper demoHelper;
    
    // Controllers and UI
    private RestaurantApplication app;
    private RestaurantViewMediator mediator;
    private ConfigurationController configController;
    private DiningRoomController diningRoomController;
    private KitchenController kitchenController;
    private InventoryController inventoryController;
    private EndOfGameController endOfGameController;
    
    private int tickCount = 0;
    
    private static final Logger logger = LoggerFactory.getLogger(RestaurantDriver.class);

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
            logger.error("[RestaurantDriver] Fatal error running application: {}", e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void start() {
        try {
            logger.info("[RestaurantDriver] Starting application...");
            
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
                                logger.info("[RestaurantDriver] Configuration complete, initializing game");
                                createEntitiesFromConfiguration();
                                initializeOperation();
                                setupDemoScenario();
                                
                                // Start the game engine
                                gameEngine.start();
                                
                                // Show dining room view and do initial update
                                app.showView(ViewType.DINING_ROOM);
                                
                                // Ensure all gameplay views get an initial update
                                diningRoomController.updateView();
                                kitchenController.updateView();
                                inventoryController.updateView();
                                
                                Thread.sleep(100);
                            }
                            if (demoHelper.isGameOver()) {
                                app.showView(ViewType.END_OF_GAME);
                                endOfGameController.updateView();
                                gameEngine.stop();
                                gameTimer.cancel();

                            } else {
                                // Update demo using DemoHelper
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
                        }
                    } catch (Exception e) {
                        logger.error("[RestaurantDriver] Error in game loop: {}", e.getMessage());
                        e.printStackTrace();
                        gameTimer.cancel();
                    }
                }
            }, 0, 250); // Check every second

            // This will block until the window is closed
            app.run();
            
            // Cleanup when window closes
            gameTimer.cancel();
            gameEngine.stop();
            logger.info("[RestaurantDriver] Application terminated");

        } catch (Exception e) {
            logger.error("[RestaurantDriver] Fatal error running application: {}", e.getMessage());
            e.printStackTrace();
        }
    }

    private void initializeConfiguration() {
        configController = new ConfigurationController();
        mediator.registerController("Configuration", configController);
    }

    private void createEntitiesFromConfiguration() {
        logger.info("[RestaurantDriver] Creating entities from configuration...");
        
        // Create core components
        this.collectionPoint = new CollectionPoint();
        this.inventory = configController.getInventory();
        this.seatingPlan = configController.getSeatingPlan();
        
        // Get StationManager from configuration controller if available, otherwise create a new one
        StationManager stationManager = configController.getStationManager();
        if (stationManager == null) {
            logger.info("[RestaurantDriver] Creating new StationManager");
            stationManager = new StationManager(collectionPoint);
        } else {
            logger.info("[RestaurantDriver] Using StationManager from configuration with " + 
                stationManager.getAllStations().size() + " stations");
            // Ensure it's using the same CollectionPoint
            logger.info("[RestaurantDriver] Setting common CollectionPoint for all stations");
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
            logger.info("[RestaurantDriver] No stations found, creating default stations");
            createDefaultStations(stationManager);
        }
        
        // Ensure all stations have proper references
        for (Station station : stationManager.getAllStations()) {
            station.setKitchen(kitchen);
            // Double-check the CollectionPoint is set correctly
            station.setCollectionPoint(collectionPoint);
            logger.info("[RestaurantDriver] Set kitchen reference for station: " + station.getType());
        }
        
        // Create and assign chefs with strategies
        setupChefs();
        
        // Create and assign waiters
        setupWaiters();
        
        // Create the FloorManager (new entity)
        this.floorManager = new FloorManager(seatingPlan, waiters, orderManager);
        
        // Set the CollectionPoint in the FloorManager to enable order delivery
        floorManager.setCollectionPoint(collectionPoint);
        logger.info("[RestaurantDriver] Set CollectionPoint in FloorManager for order delivery");
        
        // Register entities with GameEngine
        registerEntitiesWithGameEngine();
        
        // Create DemoHelper
        this.demoHelper = new DemoHelper(floorManager, kitchen, orderManager, seatingPlan, chefManager, inventory);        
        logger.info("[RestaurantDriver] Entity creation complete");
    }
    
    /**
     * Creates default stations if none are provided by the configuration
     */
    private void createDefaultStations(StationManager stationManager) {
        logger.info("[RestaurantDriver] Creating default stations...");
        
        // Create at least one station of each type
        Station prepStation = new Station(StationType.PREP, collectionPoint);
        prepStation.setKitchen(kitchen);
        logger.info("[RestaurantDriver] Created PREP station");
        
        Station grillStation = new Station(StationType.GRILL, collectionPoint);
        grillStation.setKitchen(kitchen);
        logger.info("[RestaurantDriver] Created GRILL station");
        
        Station plateStation = new Station(StationType.PLATE, collectionPoint);
        plateStation.setKitchen(kitchen);
        logger.info("[RestaurantDriver] Created PLATE station");
        
        // Add stations to station manager
        stationManager.addStation(prepStation);
        logger.info("[RestaurantDriver] Added PREP station to StationManager");
        
        stationManager.addStation(grillStation);
        logger.info("[RestaurantDriver] Added GRILL station to StationManager");
        
        stationManager.addStation(plateStation);
        logger.info("[RestaurantDriver] Added PLATE station to StationManager");
        
        // Verify stations were added
        logger.info("[RestaurantDriver] Verifying stations...");
        for (StationType type : StationType.values()) {
            List<Station> stations = stationManager.getStationsByType(type);
            logger.info("[RestaurantDriver] " + type + " stations: " + stations.size());
        }
        
        logger.info("[RestaurantDriver] Created default stations: PREP, GRILL, PLATE");
    }

    private void setupChefs() {
        // Get station manager from kitchen
        StationManager stationManager = kitchen.getStationManager();
        
        // Create chefs from configuration
        List<Chef> configChefs = configController.getChefs();
        
        // If no chefs from configuration, create default chefs
        if (configChefs == null || configChefs.isEmpty()) {
            logger.info("[RestaurantDriver] No chefs found in configuration, creating default chefs");
            configChefs = createDefaultChefs(stationManager);
        }
        
        for (Chef chef : configChefs) {
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
                    logger.info("[RestaurantDriver] Assigned chef {} to station type {}", chef.getName(), stationType);
                } else {
                    logger.info("[RestaurantDriver] Warning: No stations of type {} found for chef {}", stationType, chef.getName());
                }
            }
            
            // Don't choose initial station - let the chef's strategy decide where to go based on work
            logger.info("Chef {} initialized with strategy: {}", chef.getName(), chef.getWorkStrategy().getClass().getSimpleName());
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
        
        // Create two speeds for both chefs
        ISpeedComponent baseSpeed = new BaseSpeed();
        ISpeedComponent stimulantSpeed = new StimulantAddictDecorator(baseSpeed);

        Chef chef1 = new Chef("Default Chef 1", 15.0, baseSpeed, dynamicStrategy, stationManager);
        Chef chef2 = new Chef("Default Chef 2", 18.0, stimulantSpeed, simpleStrategy, stationManager);
        
        defaultChefs.add(chef1);
        defaultChefs.add(chef2);
        
        logger.info("[RestaurantDriver] Created default chefs: {} and {}", chef1.getName(), chef2.getName());
        
        return defaultChefs;
    }

    /**
     * Sets up waiters for the restaurant
     * Creates default waiters and assigns them to the waiters list
     */
    private void setupWaiters() {
        logger.info("[RestaurantDriver] Setting up waiters...");
        
        // Get menu from configuration controller or create a new one
        Menu menu = configController.getMenu();
        if (menu == null) {
            logger.info("[RestaurantDriver] Warning: No menu found for waiters");
            // Create a simple menu if needed
            if (inventory != null) {
                menu = new Menu(inventory);
                logger.info("[RestaurantDriver] Created default menu for waiters");
            }
        }
        
        // Create InventoryStockTracker for waiters
        InventoryStockTracker stockTracker = new InventoryStockTracker();
        
        // Register the stock tracker as an observer with the inventory
        if (inventory != null) {
            // Add the stock tracker as a global observer
            inventory.attach(stockTracker);
            
            // Update with current stock for all ingredients
            for (String ingredientName : inventory.getAllIngredients()) {
                stockTracker.update(ingredientName, inventory.getStock(ingredientName));
            }
            
            logger.info("[RestaurantDriver] Registered InventoryStockTracker with inventory");
        }
        
        
        // Create default waiters
        Waiter waiter1 = new Waiter(12.0, orderManager, menu, stockTracker);
        waiters.add(waiter1);
        
        Waiter waiter2 = new Waiter(14.0, orderManager, menu, stockTracker);
        waiters.add(waiter2);
        
        Waiter waiter3 = new Waiter(10.0, orderManager, menu, stockTracker);
        waiters.add(waiter3);
        
        logger.info("[RestaurantDriver] Created {} waiters", waiters.size());
    }

    /**
     * Register all entities with the GameEngine for the tick system.
     * Note: Only classes that extend Entity can be registered.
     * To register other classes, they would need to be modified to extend Entity
     * and implement readState() and writeState() methods.
     */
    private void registerEntitiesWithGameEngine() {
        logger.info("[RestaurantDriver] Registering entities with GameEngine");
        
        // Register Kitchen if it extends Entity
        if (kitchen instanceof Entity) {
            gameEngine.registerEntity((Entity) kitchen);
            logger.info("[RestaurantDriver] Registered Kitchen with GameEngine");
        } else {
            logger.info("[RestaurantDriver] Kitchen does not extend Entity, cannot register");
        }
        
        // Register ChefManager (which extends Entity)
        gameEngine.registerEntity(chefManager);
        logger.info("[RestaurantDriver] Registered ChefManager with GameEngine");
        
        // Register FloorManager
        gameEngine.registerEntity(floorManager);
        logger.info("[RestaurantDriver] Registered FloorManager with GameEngine");
        
        // Register all stations
        StationManager stationManager = kitchen.getStationManager();
        if (stationManager != null) {
            for (Station station : stationManager.getAllStations()) {
                gameEngine.registerEntity(station);
                logger.info("[RestaurantDriver] Registered Station {} with GameEngine", station.getType());
            }
        }
        
        // Note: The following classes would need to be modified to extend Entity
        // before they can be registered with the GameEngine:
        // - OrderManager
        // - Chef (individual chefs)
        // - Waiter
        // - Table
        // - Inventory
        
        logger.info("[RestaurantDriver] Entity registration complete");
    }

    private void initializeOperation() {        
        // Create gameplay controllers with configured components
        diningRoomController = new DiningRoomController(
            configController.getSeatingPlan()
        );

        kitchenController = new KitchenController(
            kitchen
        );

        inventoryController = new InventoryController(inventory);
        endOfGameController = new EndOfGameController();

        // Register gameplay controllers with mediator
        mediator.registerController("DiningRoom", diningRoomController);
        mediator.registerController("Kitchen", kitchenController);
        mediator.registerController("Inventory", inventoryController);
        mediator.registerController("EndOfGame", endOfGameController);
        
        logger.info("[RestaurantDriver] Restaurant initialized and ready for operation");
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
            logger.error("[RestaurantDriver] Error updating views: {}", e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Completely restarts the application by resetting all components and reinitializing
     */
    public synchronized void restartGame() {
        logger.info("[RestaurantDriver] Performing full application restart");
        if (gameEngine != null) {
            gameEngine.stop();
        }
        // Delete the current RestaurantApplication instance
        this.app = null;
        // Create a new RestaurantDriver to reinitialize all components
        RestaurantDriver newDriver = new RestaurantDriver();
        newDriver.start();
    }

    private void setupDemoScenario() {
        // Initialize the demo through our DemoHelper
        demoHelper.setupChefStrategies();
    }

    // Merged updateDemo method that uses DemoHelper
    private void updateDemo() {
        // Update tickCount
        tickCount++;
        
        // Use DemoHelper to update the demo
        if (demoHelper != null) {
            // Update the demo sequence
            demoHelper.update(tickCount);
            
            // Spawn random customers every tick
            demoHelper.seatCustomers();
        }
    }



    public static void main(String[] args) {
        RestaurantDriver driver = new RestaurantDriver();
        driver.start();
    }
}
//TODOS:
/*
 * 
 * remove waiter speed
 * make chef strategy in the right place
 * game loop to end of game then show end of game screen
 * 
 * order validation error handling
 * 
 *
 */