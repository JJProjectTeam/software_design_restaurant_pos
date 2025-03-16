package com.softwaredesign.project;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.softwaredesign.project.controller.*;
import com.softwaredesign.project.customer.DineInCustomer;
import com.softwaredesign.project.inventory.Inventory;
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
import com.softwaredesign.project.staff.staffspeeds.BaseSpeed;
import com.softwaredesign.project.staff.staffspeeds.StimulantAddictDecorator;
import com.softwaredesign.project.staff.staffspeeds.ISpeedComponent;
import com.softwaredesign.project.order.Meal;
import com.softwaredesign.project.order.Recipe;

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
    private List<Order> createdOrders = new ArrayList<>();
    
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
            logger.error("[RestaurantDriver] Fatal error running application: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void start() {
        try {
            logger.info("[RestaurantDriver] Starting application...");
            
            initializeConfiguration();
            app.showView(ViewType.END_OF_GAME);

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

                            // Demo sequence handling
                            if (!hasStartedDemo) {
                                tickCount++;
                                if (tickCount >= DEMO_DELAY) {
                                    hasStartedDemo = true;
                                    logger.info("\n=== STARTING DEMO SEQUENCE ===\n");
                                }
                            } else {
                                handleDemoStep();
                            }

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
                        logger.error("[RestaurantDriver] Error in game loop: " + e.getMessage());
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
            logger.info("[RestaurantDriver] Application terminated");

        } catch (Exception e) {
            logger.error("[RestaurantDriver] Fatal error running application: " + e.getMessage());
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
        }
        
        // Create kitchen with null OrderManager (will be set later)
        this.kitchen = new Kitchen(null, collectionPoint, stationManager);
        
        // Create ChefManager and register with GameEngine
        this.chefManager = new ChefManager();
        gameEngine.registerEntity(chefManager);
        
        // Create OrderManager with StationManager from kitchen
        this.orderManager = new OrderManager(collectionPoint, kitchen.getStationManager());
        
        // Set OrderManager in kitchen
        kitchen.setOrderManager(orderManager);
        
        // If no stations exist yet, create default stations
        if (stationManager.getAllStations().isEmpty()) {
            logger.info("[RestaurantDriver] No stations found, creating default stations");
            createDefaultStations(stationManager);
        }
        
        // Set kitchen reference in all stations
        for (Station station : stationManager.getAllStations()) {
            station.setKitchen(kitchen);
            logger.info("[RestaurantDriver] Set kitchen reference for station: " + station.getType());
        }
        
        // Create and assign chefs with strategies
        setupChefs();
        
        // Register entities with GameEngine
        registerEntitiesWithGameEngine();
        
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
        
        // Create strategies
        ChefStrategy dynamicStrategy = new DynamicChefStrategy(stationManager);
        ChefStrategy simpleStrategy = new SimpleChefStrategy();
        
        // Create chefs from configuration
        List<Chef> configChefs = configController.getChefs();
        
        // If no chefs from configuration, create default chefs
        if (configChefs == null || configChefs.isEmpty()) {
            logger.info("[RestaurantDriver] No chefs found in configuration, creating default chefs");
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
                    logger.info("[RestaurantDriver] Assigned chef " + chef.getName() + 
                        " to station type " + stationType);
                } else {
                    logger.info("[RestaurantDriver] Warning: No stations of type " + 
                        stationType + " found for chef " + chef.getName());
                }
            }
            
            // Don't choose initial station - let the chef's strategy decide where to go based on work
            logger.info("Chef " + chef.getName() + " initialized with " + 
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
        
        // Create two speeds for both chefs
        ISpeedComponent baseSpeed = new BaseSpeed();
        ISpeedComponent stimulantSpeed = new StimulantAddictDecorator(baseSpeed);

        Chef chef1 = new Chef("Default Chef 1", 15.0, baseSpeed, dynamicStrategy, stationManager);
        Chef chef2 = new Chef("Default Chef 2", 18.0, stimulantSpeed, simpleStrategy, stationManager);
        
        defaultChefs.add(chef1);
        defaultChefs.add(chef2);
        
        logger.info("[RestaurantDriver] Created default chefs: " + chef1.getName() + ", " + chef2.getName());
        
        return defaultChefs;
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
        
        // Register all stations
        StationManager stationManager = kitchen.getStationManager();
        if (stationManager != null) {
            for (Station station : stationManager.getAllStations()) {
                gameEngine.registerEntity(station);
                logger.info("[RestaurantDriver] Registered Station " + station.getType() + " with GameEngine");
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

        //TODO populate inventory
        inventoryController = new InventoryController(inventory);

        // Register gameplay controllers with mediator
        mediator.registerController("DiningRoom", diningRoomController);
        mediator.registerController("Kitchen", kitchenController);
        mediator.registerController("Inventory", inventoryController);
        
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
            logger.error("[RestaurantDriver] Error updating views: " + e.getMessage());
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
        // Create different chef strategies like in KitchenSimulator
        ChefStrategy dynamicStrategy = new DynamicChefStrategy(kitchen.getStationManager());
        ChefStrategy simpleStrategy = new SimpleChefStrategy();
        
        // Assign strategies to existing chefs
        for (int i = 0; i < chefs.size(); i++) {
            Chef chef = chefs.get(i);
            //TODO wtf this should be set in the configuration controller
            chef.setWorkStrategy(i % 2 == 0 ? dynamicStrategy : simpleStrategy);
            logger.info("Assigned " + (i % 2 == 0 ? "dynamic" : "simple") + 
                " strategy to chef " + chef.getName());
        }
    }

    private void handleDemoStep() {
        switch (demoStep) {
            case 0:
                logger.info("\n=== SEATING CUSTOMERS ===");
                // Add customers to tables
                for (Table table : seatingPlan.getAllTables()) {
                    if (table.getCustomers().isEmpty()) {
                        DineInCustomer customer = new DineInCustomer();
                        table.addCustomer(customer);
                        customer.finishBrowsing(); // Ready to order
                        logger.info("Seated customer at table " + table.getTableNumber());
                        if (table.getTableNumber() >= 3) break; // Limit to 3 tables for demo
                    }
                }
                demoStep++;
                break;

            case 1:
                logger.info("\n=== CREATING ORDERS ===");
                // Create orders for seated customers
                for (Table table : getOccupiedTables()) {
                    if (!table.isOrderPlaced()) {
                        Order order = table.getTableNumber() % 2 == 0 ? 
                            createBurgerOrder() : createKebabOrder();
                        // Table reference not needed in Order
                        orderManager.addOrder(order);
                        // Register order with CollectionPoint (1 meal per order in demo)
                        collectionPoint.registerOrder(order.getOrderId(), 1);
                        createdOrders.add(order);
                        logger.info("Created " + 
                            (table.getTableNumber() % 2 == 0 ? "burger" : "kebab") + 
                            " order for table " + table.getTableNumber());
                    }
                }
                demoStep++;
                break;

            case 2:
                logger.info("\n=== KITCHEN PROCESSING ===");
                // Let the game engine handle order processing through its tick system
                // The following methods are already called in Kitchen.writeState():
                // - getRecipes()
                // - assignRecipesToStations()
                // - updateTaskAvailability()
                
                // Give the game engine time to process orders (3 ticks)
                for (int i = 0; i < 3; i++) {
                    gameEngine.step();
                    
                    // Have chefs check for work after each step
                    for (Chef chef : chefManager.getAllChefs()) {
                        if (!chef.isWorking()) {
                            logger.info("Chef " + chef.getName() + " checking for work...");
                            // Unregister from current station if not working
                            if (chef.getCurrentStation() != null) {
                                chef.getCurrentStation().unregisterChef();
                            }
                            chef.checkForWork();
                        }
                    }
                    
                    try {
                        Thread.sleep(100); // Small delay between steps
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                
                demoStep++;
                break;
                
            case 3:
                logger.info("\n=== CHECKING STATION STATUS ===");
                // Log the status of all stations
                StationManager stationManager = kitchen.getStationManager();
                for (Station station : stationManager.getAllStations()) {
                    logger.info("Station " + station.getType() + " status:");
                    logger.info("  - Has chef: " + (station.getAssignedChef() != null));
                    if (station.getAssignedChef() != null) {
                        logger.info("  - Chef: " + station.getAssignedChef().getName());
                    }
                    logger.info("  - Is busy: " + station.isBusy());
                    logger.info("  - Current recipe: " + 
                        (station.getCurrentRecipe() != null ? station.getCurrentRecipe().getName() : "None"));
                    logger.info("  - Current task: " + 
                        (station.getCurrentTask() != null ? station.getCurrentTask().getName() : "None"));
                    logger.info("  - Backlog size: " + station.getBacklog().size());
                }
                
                // Process tasks at stations - this happens automatically in the writeState method
                // of the Station class when the GameEngine steps
                for (Station station : stationManager.getAllStations()) {
                    if (station.getCurrentTask() != null && station.getAssignedChef() != null) {
                        logger.info("Station " + station.getType() + " has task: " + 
                            station.getCurrentTask().getName() + " (processing will happen in tick)");
                    }
                }
                
                // Update task availability again - this is now handled in Kitchen.writeState()
                // but we'll call it explicitly here for clarity
                kitchen.updateTaskAvailability();
                
                demoStep++;
                break;

            default:
                // Increment tick count for ongoing operations
                tickCount++;
                
                // Existing code for collecting already complete orders…
                if (collectionPoint.hasReadyOrders()) {
                    logger.info("\n=== ORDER COMPLETED ===");
                    logger.info("Order ready for pickup!");
                    collectionPoint.collectNextOrder();
                }

                // Ensure kitchen is processing orders
                kitchen.getRecipes();
                kitchen.updateTaskAvailability();

                // NEW: Force completion manually from the createdOrders list
                for (Iterator<Order> it = createdOrders.iterator(); it.hasNext();) {
                    Order order = it.next();
                    if (!order.isComplete()) { // only process if not complete
                        Meal completedMeal = createCompletedMealForOrder(order);
                        if (completedMeal != null) {
                            collectionPoint.addCompletedMeal(completedMeal);
                            logger.info("Manually forced completion of order " + order.getOrderId());
                        }
                        // Remove the order from the list once forced completed,
                        // because its registration will be cleared upon collection.
                        it.remove();
                    }
                }
                
                // Collect any orders that are now ready
                if (collectionPoint.hasReadyOrders()) {
                    logger.info("\n=== FORCING ORDER COMPLETION MANUALLY ===");
                    collectionPoint.collectNextOrder();
                }
                
                // (Station status logging remains unchanged.)
                if (tickCount % 5 == 0) {
                    logger.info("\n=== STATION STATUS UPDATE ===");
                    StationManager sm = kitchen.getStationManager();
                    for (Station station : sm.getAllStations()) {
                        if (station.getCurrentTask() != null || station.getAssignedChef() != null) {
                            logger.info("Station " + station.getType() + ":");
                            logger.info("  - Chef: " + 
                                (station.getAssignedChef() != null ? station.getAssignedChef().getName() : "None"));
                            logger.info("  - Task: " + 
                                (station.getCurrentTask() != null ? station.getCurrentTask().getName() : "None"));
                            logger.info("  - Progress: " + station.getCookingProgress() + 
                                (station.getCurrentTask() != null ? "/" + station.getCurrentTask().getCookingWorkRequired() : ""));
                        }
                    }
                }
                break;
        }
    }

    private List<Table> getOccupiedTables() {
        List<Table> occupiedTables = new ArrayList<>();
        for (Table table : seatingPlan.getAllTables()) {
            if (!table.getCustomers().isEmpty()) {
                occupiedTables.add(table);
            }
        }
        return occupiedTables;
    }

    private Order createBurgerOrder() {
        Order order = new Order(orderManager.generateOrderId());
        order.addRecipes(new BurgerRecipe(inventory));
        return order;
    }

    private Order createKebabOrder() {
        Order order = new Order(orderManager.generateOrderId());
        order.addRecipes(new KebabRecipe(inventory));
        return order;
    }

    private Meal createCompletedMealForOrder(Order order) {
        if (order == null) return null;
        
        // Get the first recipe from the order
        if (!order.getRecipes().isEmpty()) {
            Recipe recipe = order.getRecipes().get(0); // Get first recipe for demo
            // Create a new meal using the proper constructor:
            return new Meal(recipe.getName(), recipe.getIngredients(), inventory, order.getOrderId());
        }
        
        return null;
    }

    public static void main(String[] args) {
        RestaurantDriver driver = new RestaurantDriver();
        driver.start();
    }
}
//TODOS:
/*
 * 
 * Money system, both during game and at configuraiton 1
 * 
 * Help menu 4
 * 
 * restart 3
 * 
 * speed multiplier 2.5
 * 
 * expand entities 2
 * 
 *
 */